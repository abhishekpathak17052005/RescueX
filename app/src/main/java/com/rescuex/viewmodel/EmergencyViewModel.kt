package com.rescuex.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuex.data.model.*
import com.rescuex.data.repository.IncidentRepository
import com.rescuex.data.repository.VoiceAssistantRepository
import com.rescuex.data.repository.AssistantState
import com.rescuex.location.LocationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EmergencyViewModel(
    private val incidentRepository: IncidentRepository,
    private val locationManager: LocationManager,
    private val voiceAssistantRepository: VoiceAssistantRepository
) : ViewModel() {
    private val TAG = "EmergencyVM"

    private val _activeIncident = MutableStateFlow<Incident?>(null)
    val activeIncident: StateFlow<Incident?> = _activeIncident

    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation

    private val _assistantState = MutableStateFlow(AssistantState.READY)
    val assistantState: StateFlow<AssistantState> = _assistantState

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private var timerJob: Job? = null

    init {
        Log.i(TAG, "Initializing EmergencyViewModel")
        
        viewModelScope.launch {
            voiceAssistantRepository.assistantState.collect {
                _assistantState.value = it
                if (it == AssistantState.ENDED) {
                    updateIncidentWithSummary()
                }
            }
        }
        
        viewModelScope.launch {
            voiceAssistantRepository.isMuted.collect {
                _isMuted.value = it
            }
        }

        // Observe repository for backend-triggered updates (like Tool Calls)
        viewModelScope.launch {
            incidentRepository.incidentUpdates.collect { updatedIncident ->
                if (_activeIncident.value?.id == updatedIncident.id) {
                    Log.i(TAG, "Received incident update: status=${updatedIncident.status}, ambulance=${updatedIncident.ambulance != null}")
                    _activeIncident.value = updatedIncident
                }
            }
        }
    }

    fun activateSOS() {
        viewModelScope.launch {
            Log.i(TAG, "Activating SOS")
            val location = locationManager.getCurrentLocation()
            _currentLocation.value = location

            val incident = incidentRepository.createIncident(EmergencyType.NOT_SPECIFIED)
            _activeIncident.value = incident.copy(location = location?.address)
            Log.i(TAG, "Incident created: ${incident.id}")
            
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _elapsedTime.value = 0L
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedTime.value += 1
            }
        }
    }

    fun resolveEmergency() {
        Log.i(TAG, "Resolving emergency")
        timerJob?.cancel()
        _activeIncident.value = null
        _elapsedTime.value = 0L
        voiceAssistantRepository.stopAssistant()
    }

    fun startAIAssistant() {
        val incident = _activeIncident.value
        if (incident == null) {
            Log.e(TAG, "Cannot start AI Assistant: No active incident found. State might have been lost due to process restart.")
            return
        }
        
        val location = _currentLocation.value
        Log.i(TAG, "Starting AI Assistant for incident: ${incident.id}")
        voiceAssistantRepository.startAssistant(incident.id, location?.latitude, location?.longitude)
    }

    fun stopAIAssistant() {
        Log.i(TAG, "Stopping AI Assistant")
        voiceAssistantRepository.stopAssistant()
    }

    fun toggleMute() {
        voiceAssistantRepository.toggleMute()
    }

    private fun updateIncidentWithSummary() {
        val current = _activeIncident.value ?: return
        val summary = voiceAssistantRepository.getIncidentSummary()
        val updated = current.copy(
            structuredAiSummary = summary,
            aiSummary = summary?.description,
            type = summary?.incidentType ?: EmergencyType.NOT_SPECIFIED,
            severity = summary?.severity ?: Severity.PENDING
        )
        _activeIncident.value = updated
        incidentRepository.updateIncident(updated)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "EmergencyViewModel cleared")
        timerJob?.cancel()
    }
}

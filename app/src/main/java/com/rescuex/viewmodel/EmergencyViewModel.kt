package com.rescuex.viewmodel

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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class EmergencyViewModel(
    private val incidentRepository: IncidentRepository,
    private val locationManager: LocationManager,
    private val voiceAssistantRepository: VoiceAssistantRepository
) : ViewModel() {

    private val _activeIncident = MutableStateFlow<Incident?>(null)
    val activeIncident: StateFlow<Incident?> = _activeIncident

    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation

    private val _assistantState = MutableStateFlow(AssistantState.READY)
    val assistantState: StateFlow<AssistantState> = _assistantState

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            voiceAssistantRepository.assistantState.collect {
                _assistantState.value = it
                if (it == AssistantState.ENDED) {
                    updateIncidentWithSummary()
                }
            }
        }
    }

    fun activateSOS() {
        viewModelScope.launch {
            val location = locationManager.getCurrentLocation()
            _currentLocation.value = location

            val incident = incidentRepository.createIncident(EmergencyType.NOT_SPECIFIED)
            _activeIncident.value = incident.copy(location = location?.address)
            
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
        timerJob?.cancel()
        _activeIncident.value = null
        _elapsedTime.value = 0L
        voiceAssistantRepository.stopAssistant()
    }

    fun startAIAssistant() {
        val incident = _activeIncident.value ?: return
        voiceAssistantRepository.startAssistant(incident.id, _currentLocation.value?.address)
    }

    fun stopAIAssistant() {
        voiceAssistantRepository.stopAssistant()
    }

    private fun updateIncidentWithSummary() {
        val summary = voiceAssistantRepository.getIncidentSummary()
        _activeIncident.value = _activeIncident.value?.copy(
            structuredAiSummary = summary,
            aiSummary = summary?.description,
            type = summary?.incidentType ?: EmergencyType.NOT_SPECIFIED,
            severity = summary?.severity ?: Severity.PENDING
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

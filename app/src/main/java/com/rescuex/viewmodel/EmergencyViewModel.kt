package com.rescuex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuex.data.model.EmergencyType
import com.rescuex.data.model.Incident
import com.rescuex.data.model.IncidentStatus
import com.rescuex.data.model.LocationData
import com.rescuex.data.repository.IncidentRepository
import com.rescuex.location.LocationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EmergencyViewModel(
    private val incidentRepository: IncidentRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _activeIncident = MutableStateFlow<Incident?>(null)
    val activeIncident: StateFlow<Incident?> = _activeIncident

    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation

    private val _aiAssistantState = MutableStateFlow(AIAssistantState.IDLE)
    val aiAssistantState: StateFlow<AIAssistantState> = _aiAssistantState

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private var timerJob: Job? = null

    fun activateSOS() {
        viewModelScope.launch {
            // 1. Fetch Location
            val location = locationManager.getCurrentLocation()
            _currentLocation.value = location

            // 2. Create Incident
            val incident = incidentRepository.createIncident(EmergencyType.NOT_SPECIFIED)
            // In a real app, we'd update the incident with the location here
            _activeIncident.value = incident.copy(location = location?.address)
            
            // 3. Start Timer
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
    }

    fun startAIAssistant() {
        _aiAssistantState.value = AIAssistantState.LISTENING
    }

    fun setAIAssistantState(state: AIAssistantState) {
        _aiAssistantState.value = state
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

enum class AIAssistantState {
    IDLE, LISTENING, PROCESSING, CONNECTED
}

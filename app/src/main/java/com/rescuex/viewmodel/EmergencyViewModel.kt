package com.rescuex.viewmodel

import androidx.lifecycle.ViewModel
import com.rescuex.data.model.EmergencyType
import com.rescuex.data.model.Incident
import com.rescuex.data.model.IncidentStatus
import com.rescuex.data.repository.IncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EmergencyViewModel(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _activeIncident = MutableStateFlow<Incident?>(null)
    val activeIncident: StateFlow<Incident?> = _activeIncident

    private val _aiAssistantState = MutableStateFlow(AIAssistantState.IDLE)
    val aiAssistantState: StateFlow<AIAssistantState> = _aiAssistantState

    fun activateSOS() {
        _activeIncident.value = incidentRepository.createIncident(EmergencyType.NOT_SPECIFIED)
    }

    fun startAIAssistant() {
        // Mock progression
        _aiAssistantState.value = AIAssistantState.LISTENING
        // Simulate processing after some time could be done with a delay in a real scenario
    }

    fun setAIAssistantState(state: AIAssistantState) {
        _aiAssistantState.value = state
    }
}

enum class AIAssistantState {
    IDLE, LISTENING, PROCESSING, CONNECTED
}

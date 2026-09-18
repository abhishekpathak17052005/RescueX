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
import java.util.*

enum class AiSessionState {
    NOT_STARTED, CONNECTING, ACTIVE, COMPLETED
}

class EmergencyViewModel(
    private val incidentRepository: IncidentRepository,
    private val locationManager: LocationManager,
    private val voiceAssistantRepository: VoiceAssistantRepository
) : ViewModel() {
    private val TAG = "EmergencyVM"

    private val _activeIncident = MutableStateFlow<Incident?>(null)
    val activeIncident: StateFlow<Incident?> = _activeIncident

    private val _aiSessionState = MutableStateFlow(AiSessionState.NOT_STARTED)
    val aiSessionState: StateFlow<AiSessionState> = _aiSessionState

    private val _isActivating = MutableStateFlow(false)
    val isActivating: StateFlow<Boolean> = _isActivating

    private val _activationError = MutableStateFlow<String?>(null)
    val activationError: StateFlow<String?> = _activationError

    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation

    private val _assistantState = MutableStateFlow(AssistantState.READY)
    val assistantState: StateFlow<AssistantState> = _assistantState

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private var timerJob: Job? = null

    init {
        Log.i(TAG, "Initializing EmergencyViewModel")
        
        viewModelScope.launch {
            voiceAssistantRepository.assistantState.collect { state ->
                _assistantState.value = state
                val currentIncident = _activeIncident.value
                val incidentId = currentIncident?.incidentId ?: "none"
                
                when (state) {
                    AssistantState.CONNECTING -> {
                        Log.i("AI STATE", "[AI STATE] incidentId=$incidentId state=CONNECTING")
                        _aiSessionState.value = AiSessionState.CONNECTING
                    }
                    AssistantState.CONNECTED, AssistantState.LISTENING, AssistantState.THINKING, AssistantState.SPEAKING -> {
                        if (_aiSessionState.value != AiSessionState.ACTIVE) {
                            Log.i("AI STATE", "[AI STATE] incidentId=$incidentId state=ACTIVE")
                            _aiSessionState.value = AiSessionState.ACTIVE
                        }
                    }
                    AssistantState.ENDED -> {
                        val summary = voiceAssistantRepository.getIncidentSummary()
                        if (isSummaryValid(summary)) {
                            Log.i("AI STATE", "[AI STATE] incidentId=$incidentId state=COMPLETED")
                            _aiSessionState.value = AiSessionState.COMPLETED
                            updateIncidentWithSummary()
                        } else {
                            Log.i("AI STATE", "[AI STATE] incidentId=$incidentId state=NOT_STARTED (Summary invalid or incomplete)")
                            _aiSessionState.value = AiSessionState.NOT_STARTED
                        }
                    }
                    AssistantState.READY -> {
                        // Do not reset to NOT_STARTED if it was already COMPLETED for this incident
                        if (_aiSessionState.value != AiSessionState.COMPLETED) {
                            _aiSessionState.value = AiSessionState.NOT_STARTED
                        }
                    }
                    AssistantState.ERROR -> {
                         Log.e("AI STATE", "[AI STATE] incidentId=$incidentId state=ERROR (Service unavailable)")
                    }
                }
            }
        }
        
        viewModelScope.launch {
            voiceAssistantRepository.transcript.collect {
                _transcript.value = it
            }
        }

        viewModelScope.launch {
            voiceAssistantRepository.isMuted.collect {
                _isMuted.value = it
            }
        }

        // Observe repository for backend-triggered updates (like Tool Calls from Gemini/Vapi)
        viewModelScope.launch {
            incidentRepository.incidentUpdates.collect { updatedIncident ->
                val current = _activeIncident.value
                if (current != null && current.incidentId == updatedIncident.incidentId) {
                    Log.i("INCIDENT FLOW", "[INCIDENT FLOW] Received repository update for active incident: ${updatedIncident.incidentId}")
                    _activeIncident.value = updatedIncident
                    
                    // If incident was loaded from repository and has a summary, 
                    // should we mark it as COMPLETED? 
                    // User says: "Completion state must be explicitly tied to the current Vapi session and current incident."
                    // But if we load an incident that already has a valid summary, 
                    // maybe we should show "View Summary"?
                    // Actually, let's keep track of if the CURRENT session has completed.
                } else if (current == null) {
                    Log.d("INCIDENT FLOW", "[INCIDENT FLOW] Active incident = null, ignoring update for ${updatedIncident.incidentId}")
                }
            }
        }
    }

    private fun isSummaryValid(summary: AIIncidentSummary?): Boolean {
        val currentIncident = _activeIncident.value
        val incidentId = currentIncident?.incidentId ?: "none"
        
        if (summary == null) {
            Log.i("AI SUMMARY", "[AI SUMMARY]\nincidentId=$incidentId\nvalid=false\nemergencyType=null\nseverity=null")
            return false
        }
        
        val valid = summary.incidentType != EmergencyType.NOT_SPECIFIED && 
                    summary.severity != Severity.PENDING
        
        Log.i("AI SUMMARY", "[AI SUMMARY]\nincidentId=$incidentId\nvalid=$valid\nemergencyType=${summary.incidentType}\nseverity=${summary.severity}")
        return valid
    }

    fun loadIncident(incidentId: String) {
        if (incidentId.isEmpty() || incidentId == "pending" || incidentId == "none") {
            Log.d("INCIDENT FLOW", "[INCIDENT FLOW] loadIncident ignored for special ID: $incidentId")
            return
        }
        if (_activeIncident.value?.incidentId == incidentId) return
        
        viewModelScope.launch {
            val incident = incidentRepository.getIncidentById(incidentId)
            if (incident != null) {
                Log.i("INCIDENT FLOW", "[INCIDENT FLOW] Loaded incident from repository: $incidentId")
                _activeIncident.value = incident
                
                // When loading an incident, check if it already has a valid summary
                if (isSummaryValid(incident.structuredAiSummary)) {
                    Log.i("AI STATE", "[AI STATE] incidentId=$incidentId state=COMPLETED")
                    _aiSessionState.value = AiSessionState.COMPLETED
                } else {
                    Log.i("AI STATE", "[AI STATE] incidentId=$incidentId state=NOT_STARTED")
                    _aiSessionState.value = AiSessionState.NOT_STARTED
                }
            } else {
                Log.e("INCIDENT FLOW", "[INCIDENT FLOW] Failed to load incident: $incidentId")
            }
        }
    }

    fun activateSOS(userId: String, userName: String, phone: String) {
        viewModelScope.launch {
            Log.i("INCIDENT FLOW", "[INCIDENT FLOW] SOS pressed")
            _isActivating.value = true
            _activationError.value = null
            
            val location = locationManager.getCurrentLocation()
            _currentLocation.value = location

            val newIncident = Incident(
                patientId = userId,
                patientName = userName,
                patientPhone = phone,
                pickupAddress = location?.address ?: "Unknown",
                pickupLatitude = location?.latitude ?: 0.0,
                pickupLongitude = location?.longitude ?: 0.0,
                status = IncidentStatus.SOS_ACTIVE,
                createdAt = Date(),
                updatedAt = Date()
            )

            val result = incidentRepository.createIncident(newIncident)
            _isActivating.value = false
            
            if (result.isSuccess) {
                val created = result.getOrNull()
                if (created != null) {
                    Log.i("INCIDENT FLOW", "[INCIDENT FLOW] Incident created: ${created.incidentId}")
                    _activeIncident.value = created
                    Log.i("INCIDENT FLOW", "[INCIDENT FLOW] Active incident set: ${created.incidentId}")
                    
                    Log.i("AI STATE", "[AI STATE] incidentId=${created.incidentId} state=NOT_STARTED")
                    _aiSessionState.value = AiSessionState.NOT_STARTED

                    startTimer()
                } else {
                    Log.e("INCIDENT FLOW", "[INCIDENT FLOW] Incident created but returned null")
                    _activationError.value = "Failed to initialize incident data."
                }
            } else {
                val error = result.exceptionOrNull()
                Log.e("INCIDENT FLOW", "[INCIDENT FLOW] Failed to create incident", error)
                _activationError.value = "Could not reach RescueX services. Please check your connection."
            }
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
        val current = _activeIncident.value
        if (current != null) {
            viewModelScope.launch {
                incidentRepository.updateIncident(current.copy(status = IncidentStatus.RESOLVED, updatedAt = Date()))
            }
        }
        timerJob?.cancel()
        _activeIncident.value = null
        _elapsedTime.value = 0L
        voiceAssistantRepository.stopAssistant()
        _aiSessionState.value = AiSessionState.NOT_STARTED
    }

    fun startAIAssistant() {
        val incident = _activeIncident.value
        if (incident == null) {
            Log.e(TAG, "Cannot start AI Assistant: No active incident found.")
            return
        }
        
        Log.i("AI SESSION", "[AI SESSION] Starting Vapi session for incident=${incident.incidentId}")
        _aiSessionState.value = AiSessionState.CONNECTING
        voiceAssistantRepository.startAssistant(incident.incidentId, incident.pickupLatitude, incident.pickupLongitude)
    }

    fun startNewAIAssistantSession() {
        val incident = _activeIncident.value
        if (incident == null) {
            Log.e(TAG, "Cannot restart AI Assistant: No active incident found.")
            return
        }

        Log.i("AI SESSION", "[AI SESSION] Starting NEW Vapi session for incident=${incident.incidentId}")
        
        // 1. Stop any old session
        voiceAssistantRepository.stopAssistant()
        
        // 2. Clear current state (repository should handle internal clear, but we reset ViewModel state)
        _aiSessionState.value = AiSessionState.CONNECTING
        
        // 3. Start new session
        voiceAssistantRepository.startAssistant(incident.incidentId, incident.pickupLatitude, incident.pickupLongitude)
    }

    fun stopAIAssistant() {
        voiceAssistantRepository.stopAssistant()
    }

    fun toggleMute() {
        voiceAssistantRepository.toggleMute()
    }

    fun triggerQuickTriage(type: EmergencyType, severity: Severity, description: String) {
        val incident = _activeIncident.value
        if (incident == null) {
            Log.e(TAG, "Cannot trigger triage: No active incident found.")
            return
        }
        _aiSessionState.value = AiSessionState.CONNECTING
        voiceAssistantRepository.triggerQuickTriage(type, severity, description)
    }

    fun sendUserMessage(text: String) {
        voiceAssistantRepository.sendUserMessage(text)
    }

    private fun updateIncidentWithSummary() {
        val current = _activeIncident.value ?: return
        val summary = voiceAssistantRepository.getIncidentSummary()
        val updated = current.copy(
            structuredAiSummary = summary,
            reportedSymptoms = summary?.description,
            emergencyType = summary?.incidentType ?: EmergencyType.NOT_SPECIFIED,
            severity = summary?.severity ?: Severity.PENDING,
            updatedAt = Date()
        )
        viewModelScope.launch {
            incidentRepository.updateIncident(updated)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

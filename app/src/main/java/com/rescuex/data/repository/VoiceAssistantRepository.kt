package com.rescuex.data.repository

import ai.vapi.Vapi
import android.content.Context
import com.rescuex.BuildConfig
import com.rescuex.data.model.AIIncidentSummary
import com.rescuex.data.model.EmergencyType
import com.rescuex.data.model.Severity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

interface VoiceAssistantRepository {
    val assistantState: Flow<AssistantState>
    fun startAssistant(incidentId: String, locationContext: String?)
    fun stopAssistant()
    fun getIncidentSummary(): AIIncidentSummary?
}

enum class AssistantState {
    READY, LISTENING, THINKING, SPEAKING, CONNECTED, ENDED, ERROR
}

class VapiVoiceAssistantRepository(
    private val context: Context
) : VoiceAssistantRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(AssistantState.READY)
    override val assistantState: Flow<AssistantState> = _state

    private var vapi: Vapi? = null
    private var lastSummary: AIIncidentSummary? = null

    init {
        val publicKey = BuildConfig.VAPI_PUBLIC_KEY
        if (publicKey.isNotEmpty() && publicKey != "YOUR_PUBLIC_KEY") {
            vapi = Vapi(publicKey)
            observeEvents()
        }
    }

    private fun observeEvents() {
        scope.launch {
            vapi?.eventFlow?.collect { event ->
                when (event) {
                    is Vapi.Event.CallDidStart -> _state.value = AssistantState.CONNECTED
                    is Vapi.Event.CallDidEnd -> _state.value = AssistantState.ENDED
                    is Vapi.Event.SpeechUpdate -> {
                        if (event.role == Vapi.Event.SpeechUpdate.Role.ASSISTANT) {
                            _state.value = AssistantState.SPEAKING
                        } else {
                            _state.value = AssistantState.LISTENING
                        }
                    }
                    is Vapi.Event.Error -> {
                        _state.value = AssistantState.ERROR
                    }
                    // Handle transcripts or structured data if available
                    else -> {}
                }
            }
        }
    }

    override fun startAssistant(incidentId: String, locationContext: String?) {
        val assistantId = BuildConfig.VAPI_ASSISTANT_ID
        if (vapi != null && assistantId.isNotEmpty() && assistantId != "YOUR_ASSISTANT_ID") {
            vapi?.start(
                assistantId = assistantId,
                assistantOverrides = mapOf(
                    "variableValues" to mapOf(
                        "incidentId" to incidentId,
                        "location" to (locationContext ?: "Unknown")
                    )
                )
            )
        } else {
            // Demo Mode fallback
            runDemoMode()
        }
    }

    private fun runDemoMode() {
        scope.launch {
            _state.value = AssistantState.LISTENING
            kotlinx.coroutines.delay(2000)
            _state.value = AssistantState.THINKING
            kotlinx.coroutines.delay(1000)
            _state.value = AssistantState.SPEAKING
            kotlinx.coroutines.delay(2000)
            _state.value = AssistantState.CONNECTED
            // Stay connected in demo mode until stopped
        }
    }

    override fun stopAssistant() {
        if (vapi != null) {
            vapi?.stop()
        } else {
            _state.value = AssistantState.ENDED
        }
        
        // Mock a summary generation
        lastSummary = AIIncidentSummary(
            incidentType = EmergencyType.MEDICAL,
            severity = Severity.HIGH,
            description = "Demo: User reported a medical emergency and requested assistance.",
            immediateDanger = true,
            medicalAssistanceRequired = true,
            userSafe = false
        )
    }

    override fun getIncidentSummary(): AIIncidentSummary? = lastSummary
}

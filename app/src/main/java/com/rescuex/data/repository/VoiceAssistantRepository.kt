package com.rescuex.data.repository

import android.content.Context
import android.util.Log
import ai.vapi.android.Vapi
import ai.vapi.android.VapiMessage
import ai.vapi.android.VapiMessageContent
import com.rescuex.BuildConfig
import com.rescuex.data.model.*
import com.rescuex.data.remote.EmergencyBackendService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

interface VoiceAssistantRepository {
    val assistantState: Flow<AssistantState>
    val isMuted: Flow<Boolean>
    fun startAssistant(incidentId: String, lat: Double?, lon: Double?)
    fun stopAssistant()
    fun toggleMute()
    fun getIncidentSummary(): AIIncidentSummary?
}

enum class AssistantState {
    READY, CONNECTING, CONNECTED, LISTENING, THINKING, SPEAKING, ENDED, ERROR
}

class VapiVoiceAssistantRepository(
    private val context: Context,
    private val incidentRepository: IncidentRepository,
    private val backendService: EmergencyBackendService
) : VoiceAssistantRepository {
    private val TAG = "VapiAssistant"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(AssistantState.READY)
    override val assistantState: Flow<AssistantState> = _state

    private val _isMuted = MutableStateFlow(false)
    override val isMuted: Flow<Boolean> = _isMuted

    private var vapi: Vapi? = null
    private var lastSummary: AIIncidentSummary? = null
    private var currentIncidentId: String? = null

    init {
        Log.i(TAG, "[VAPI LIFECYCLE] Repository created")
        initializeVapi()
    }

    private fun initializeVapi() {
        val publicKey = BuildConfig.VAPI_PUBLIC_KEY
        Log.i(TAG, "[VAPI DEBUG] Initializing Vapi Client")
        
        if (publicKey.isNotEmpty() && publicKey != "YOUR_PUBLIC_KEY") {
            try {
                val configuration = Vapi.Configuration(publicKey = publicKey)
                vapi = Vapi(context, configuration)
                observeEvents()
                Log.i(TAG, "[VAPI DEBUG] Vapi SDK initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "[VAPI ERROR] type=Initialization message=${e.message}", e)
            }
        }
    }

    private fun observeEvents() {
        scope.launch {
            vapi?.eventFlow?.collect { event ->
                Log.i(TAG, "[VAPI RAW EVENT] type=${event.javaClass.simpleName}")
                
                when (event) {
                    is Vapi.Event.CallDidStart -> {
                        Log.i(TAG, "[VAPI CONNECTION] CONNECTED")
                        _state.value = AssistantState.CONNECTED
                    }
                    is Vapi.Event.CallDidEnd -> {
                        Log.i(TAG, "[VAPI CONNECTION] DISCONNECTED")
                        _state.value = AssistantState.ENDED
                    }
                    is Vapi.Event.SpeechUpdate -> {
                        if (event.role == "assistant") {
                            if (event.status == "started") {
                                _state.value = AssistantState.SPEAKING
                            } else if (event.status == "stopped") {
                                _state.value = AssistantState.LISTENING
                            }
                        } else if (event.role == "user") {
                            if (event.status == "started") {
                                _state.value = AssistantState.LISTENING
                            }
                        }
                    }
                    is Vapi.Event.Transcript -> {
                        Log.i(TAG, "[VAPI SPEECH] role=user, transcript=${event.text}")
                        _state.value = AssistantState.LISTENING
                    }
                    is Vapi.Event.FunctionCall -> {
                        Log.i(TAG, "[VAPI DEBUG] TOOL CALL: ${event.name}")
                        handleFunctionCall(event.name, event.parameters)
                    }
                    is Vapi.Event.Error -> {
                        Log.e(TAG, "[VAPI ERROR] type=SDK_Event message=${event.error}")
                        _state.value = AssistantState.ERROR
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleFunctionCall(name: String, parameters: Map<String, Any?>) {
        val incidentId = currentIncidentId ?: return
        when (name) {
            "dispatchAmbulance" -> {
                Log.i("RescueX", "Executing dispatchAmbulance for $incidentId")
                val ambulance = backendService.dispatchAmbulance(incidentId, "user1")
                updateIncidentAmbulance(incidentId, ambulance)
                
                sendToolResult("Ambulance dispatched. ETA: ${ambulance.etaMinutes} mins.")
            }
            "findAndSelectHospital" -> {
                val typeStr = parameters["emergencyType"] as? String ?: "OTHER"
                val type = try { EmergencyType.valueOf(typeStr) } catch (e: Exception) { EmergencyType.OTHER }
                
                val hospitals = backendService.findSuitableHospitals(type, null, null)
                val best = backendService.selectBestHospital(hospitals, type)
                
                if (best != null) {
                    updateIncidentHospital(incidentId, best)
                    val incident = incidentRepository.getIncidentById(incidentId)
                    if (incident?.ambulance != null) {
                        backendService.updateAmbulanceDestination(incidentId, incident.ambulance.id, best.id)
                    }
                    sendToolResult("Hospital selected: ${best.name}")
                }
            }
        }
    }

    private fun sendToolResult(result: String) {
        scope.launch {
            try {
                Log.d(TAG, "[VAPI DEBUG] Sending tool result: $result")
                val msgContent = VapiMessageContent(role = "tool", content = result)
                val vapiMsg = VapiMessage(type = "tool-output", message = msgContent)
                vapi?.send(vapiMsg)
            } catch (e: Exception) {
                Log.e(TAG, "[VAPI ERROR] Failed to send tool result: ${e.message}")
            }
        }
    }

    private fun updateIncidentAmbulance(id: String, ambulance: Ambulance) {
        val incident = incidentRepository.getIncidentById(id) ?: return
        incidentRepository.updateIncident(incident.copy(ambulance = ambulance, status = IncidentStatus.RESPONDER_ASSIGNED))
    }

    private fun updateIncidentHospital(id: String, hospital: Hospital) {
        val incident = incidentRepository.getIncidentById(id) ?: return
        incidentRepository.updateIncident(incident.copy(selectedHospital = hospital))
    }

    override fun startAssistant(incidentId: String, lat: Double?, lon: Double?) {
        val assistantId = BuildConfig.VAPI_ASSISTANT_ID
        currentIncidentId = incidentId
        
        Log.i(TAG, "[VAPI CONNECTION] START_REQUESTED")
        
        if (vapi != null && assistantId.isNotEmpty() && assistantId != "YOUR_ASSISTANT_ID") {
            _state.value = AssistantState.CONNECTING
            
            // Simplified overrides to avoid HTTP 400
            val overrides = mapOf(
                "variableValues" to mapOf(
                    "incidentId" to incidentId,
                    "latitude" to (lat ?: 0.0),
                    "longitude" to (lon ?: 0.0)
                )
            )

            scope.launch {
                try {
                    Log.d(TAG, "[VAPI DEBUG] Starting with Assistant ID: $assistantId")
                    val result = vapi?.start(assistantId = assistantId, assistantOverrides = overrides)
                    
                    if (result?.isFailure == true) {
                        val error = result.exceptionOrNull()
                        Log.e(TAG, "[VAPI ERROR] vapi.start failed: ${error?.message}")
                        
                        // Retry once without overrides if it was a 400 error
                        if (error?.message?.contains("400") == true) {
                            Log.i(TAG, "[VAPI DEBUG] Retrying without overrides...")
                            val retryResult = vapi?.start(assistantId = assistantId)
                            if (retryResult?.isFailure == true) {
                                _state.value = AssistantState.ERROR
                            }
                        } else {
                            _state.value = AssistantState.ERROR
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[VAPI ERROR] vapi.start exception: ${e.message}")
                    _state.value = AssistantState.ERROR
                }
            }
        } else {
            runDemoMode()
        }
    }

    private fun runDemoMode() {
        scope.launch {
            _state.value = AssistantState.CONNECTING
            delay(1000)
            _state.value = AssistantState.LISTENING
            delay(2000)
            currentIncidentId?.let { id ->
                val ambulance = backendService.dispatchAmbulance(id, "user1")
                updateIncidentAmbulance(id, ambulance)
            }
            delay(1000)
            _state.value = AssistantState.THINKING
            delay(1500)
            _state.value = AssistantState.SPEAKING
            delay(3000)
            _state.value = AssistantState.CONNECTED
        }
    }

    override fun stopAssistant() {
        scope.launch {
            try { vapi?.stop() } catch (e: Exception) {}
            _state.value = AssistantState.ENDED
        }
    }

    override fun toggleMute() {
        scope.launch {
            try {
                vapi?.toggleMute()
                _isMuted.value = !_isMuted.value
            } catch (e: Exception) {}
        }
    }

    override fun getIncidentSummary(): AIIncidentSummary? = lastSummary
}

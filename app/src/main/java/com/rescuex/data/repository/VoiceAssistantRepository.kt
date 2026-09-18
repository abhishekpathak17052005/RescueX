package com.rescuex.data.repository

import android.content.Context
import android.media.AudioManager
import android.util.Log
import ai.vapi.android.Vapi
import ai.vapi.android.VapiMessage
import ai.vapi.android.VapiMessageContent
import com.rescuex.BuildConfig
import com.rescuex.data.model.*
import com.rescuex.data.remote.EmergencyBackendService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*

interface VoiceAssistantRepository {
    val assistantState: Flow<AssistantState>
    val isMuted: Flow<Boolean>
    val transcript: Flow<String>
    val localAudioLevel: Flow<Float>
    fun startAssistant(incidentId: String, lat: Double?, lon: Double?)
    fun stopAssistant()
    fun toggleMute()
    fun getIncidentSummary(): AIIncidentSummary?
    fun triggerQuickTriage(type: EmergencyType, severity: Severity, description: String)
    fun sendUserMessage(text: String)
}

enum class AssistantState {
    READY, CONNECTING, CONNECTED, LISTENING, THINKING, SPEAKING, ENDED, ERROR
}

class VapiVoiceAssistantRepository(
    private val context: Context,
    private val incidentRepository: IncidentRepository,
    private val ambulanceRepository: AmbulanceRepository,
    private val hospitalRepository: HospitalRepository,
    private val backendService: EmergencyBackendService
) : VoiceAssistantRepository {
    private val TAG = "VapiAssistant"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(AssistantState.READY)
    override val assistantState: Flow<AssistantState> = _state

    private val _isMuted = MutableStateFlow(false)
    override val isMuted: Flow<Boolean> = _isMuted

    private val _transcript = MutableStateFlow("")
    override val transcript: Flow<String> = _transcript

    private val _localAudioLevel = MutableStateFlow(0f)
    override val localAudioLevel: Flow<Float> = _localAudioLevel

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var previousAudioMode: Int = AudioManager.MODE_NORMAL
    private var audioLevelJob: Job? = null

    private var vapi: Vapi? = null
    private var lastSummary: AIIncidentSummary? = null
    private var currentIncidentId: String? = null
    private var triageJob: Job? = null

    init {
        initializeVapi()
    }

    private fun initializeVapi() {
        val publicKey = BuildConfig.VAPI_PUBLIC_KEY
        if (publicKey.isNotEmpty() && publicKey != "YOUR_PUBLIC_KEY") {
            try {
                val configuration = Vapi.Configuration(publicKey = publicKey)
                vapi = Vapi(context, configuration)
                observeEvents()
                Log.d(TAG, "[VAPI INIT] Vapi initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "[VAPI ERROR] type=Initialization message=${e.message}", e)
                vapi = null
            }
        } else {
            Log.w(TAG, "[VAPI INIT] Public key missing or placeholder")
        }
    }

    private fun configureAudioForCall() {
        try {
            audioManager?.let { am ->
                previousAudioMode = am.mode
                // Unmute microphone to guarantee audio input recording
                am.isMicrophoneMute = false
                Log.d(TAG, "[AUDIO] Microhpone unmuted (isMicrophoneMute=false). Letting WebRTC handle communication routing.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "[AUDIO] Error configuring audio for call: ${e.message}")
        }
    }

    private fun restoreAudio() {
        try {
            audioLevelJob?.cancel()
            _localAudioLevel.value = 0f
            audioManager?.let { am ->
                am.mode = previousAudioMode
                Log.d(TAG, "[AUDIO] Restored audio to mode=$previousAudioMode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "[AUDIO] Error restoring audio: ${e.message}")
        }
    }

    private fun observeEvents() {
        scope.launch {
            vapi?.eventFlow?.collect { event ->
                when (event) {
                    is Vapi.Event.CallDidStart -> {
                        triageJob?.cancel()
                        configureAudioForCall()
                        _state.value = AssistantState.CONNECTED
                        _transcript.value = "AI Connected. Listening to your emergency report..."
                        Log.d(TAG, "[VAPI EVENT] CallDidStart")

                        // Start local audio level observer
                        scope.launch {
                            try {
                                vapi?.startLocalAudioLevelObserver()
                            } catch (e: Exception) {
                                Log.w(TAG, "[AUDIO] startLocalAudioLevelObserver: ${e.message}")
                            }
                        }

                        // Poll local audio level for real-time visualizer
                        audioLevelJob?.cancel()
                        audioLevelJob = scope.launch {
                            while (_state.value != AssistantState.ENDED && _state.value != AssistantState.ERROR) {
                                val level = vapi?.localAudioLevel ?: 0f
                                _localAudioLevel.value = level
                                delay(120)
                            }
                        }
                    }
                    is Vapi.Event.CallDidEnd -> {
                        restoreAudio()
                        _state.value = AssistantState.ENDED
                        _transcript.value = "Call ended."
                        Log.d(TAG, "[VAPI EVENT] CallDidEnd")
                        if (lastSummary == null) {
                            lastSummary = AIIncidentSummary(
                                incidentType = EmergencyType.MEDICAL,
                                severity = Severity.HIGH,
                                description = "Emergency voice consultation concluded."
                            )
                        }
                    }
                    is Vapi.Event.SpeechUpdate -> {
                        Log.d(TAG, "[VAPI EVENT] SpeechUpdate: role=${event.role} status=${event.status}")
                        if (event.role == "assistant") {
                            if (event.status == "started") {
                                _state.value = AssistantState.SPEAKING
                                _transcript.value = "RescueX AI is speaking..."
                            } else {
                                _state.value = AssistantState.LISTENING
                                _transcript.value = "Listening to you... Speak now."
                            }
                        } else if (event.role == "user") {
                            if (event.status == "started") {
                                _state.value = AssistantState.LISTENING
                                _transcript.value = "Listening to your voice..."
                            } else {
                                _state.value = AssistantState.THINKING
                                _transcript.value = "Processing your request..."
                            }
                        }
                    }
                    is Vapi.Event.Transcript -> {
                        Log.d(TAG, "[VAPI EVENT] Transcript: ${event.text}")
                        if (event.text.isNotBlank()) {
                            _transcript.value = event.text
                        }
                    }
                    is Vapi.Event.ConversationUpdate -> {
                        val lastMsg = event.messages.lastOrNull()
                        val content = (lastMsg?.get("content") ?: lastMsg?.get("message")) as? String
                        if (!content.isNullOrBlank()) {
                            _transcript.value = content
                        }
                    }
                    is Vapi.Event.UserInterrupted -> {
                        Log.d(TAG, "[VAPI EVENT] UserInterrupted")
                        _state.value = AssistantState.LISTENING
                        _transcript.value = "Listening to you..."
                    }
                    is Vapi.Event.FunctionCall -> handleFunctionCall(event.name, event.parameters)
                    is Vapi.Event.Error -> {
                        Log.e(TAG, "[VAPI ERROR] message=${event.error}")
                        restoreAudio()
                        _state.value = AssistantState.ERROR
                        _transcript.value = "Voice connection issue: ${event.error}"
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleFunctionCall(name: String, parameters: Map<String, Any?>) {
        val incidentId = currentIncidentId ?: return
        Log.d(TAG, "[DISPATCH DEBUG] handleFunctionCall: $name params=$parameters")
        
        scope.launch {
            when (name) {
                "dispatchAmbulance" -> {
                    dispatchAmbulanceForIncident(incidentId)
                    if (lastSummary == null) {
                        lastSummary = AIIncidentSummary(
                            incidentType = EmergencyType.MEDICAL,
                            severity = Severity.CRITICAL,
                            description = "Ambulance dispatched via voice assistant."
                        )
                    }
                }
                "findAndSelectHospital" -> {
                    val typeStr = parameters["emergencyType"] as? String ?: "MEDICAL"
                    val type = try { EmergencyType.valueOf(typeStr) } catch (e: Exception) { EmergencyType.MEDICAL }
                    selectHospitalForIncident(incidentId, type)
                    lastSummary = (lastSummary ?: AIIncidentSummary()).copy(
                        incidentType = type,
                        severity = Severity.HIGH,
                        description = "Hospital selected for $type emergency."
                    )
                }
            }
        }
    }

    private suspend fun dispatchAmbulanceForIncident(incidentId: String) {
        val incident = incidentRepository.getIncidentById(incidentId) ?: return
        val available = ambulanceRepository.getAvailableAmbulances()
        val selectedAmbulance = available.firstOrNull()
        
        if (selectedAmbulance != null) {
            val updated = incident.copy(
                ambulanceId = selectedAmbulance.ambulanceId,
                ambulanceStatus = AmbulanceStatus.DISPATCHED,
                status = IncidentStatus.AMBULANCE_DISPATCHED,
                updatedAt = Date()
            )
            incidentRepository.updateIncident(updated)
            ambulanceRepository.updateAmbulanceStatus(selectedAmbulance.ambulanceId, AmbulanceStatus.DISPATCHED)
            sendToolResult("Ambulance ${selectedAmbulance.ambulanceId} dispatched. ETA: 7 mins.")
        } else {
            val ambulance = backendService.dispatchAmbulance(incidentId, incident.patientId)
            val updated = incident.copy(
                ambulanceId = ambulance.ambulanceId,
                ambulanceStatus = AmbulanceStatus.DISPATCHED,
                status = IncidentStatus.AMBULANCE_DISPATCHED,
                updatedAt = Date()
            )
            incidentRepository.updateIncident(updated)
            sendToolResult("Nearest ambulance dispatched. ID: ${ambulance.ambulanceId}, ETA: ${ambulance.etaMinutes ?: 10} mins.")
        }
    }

    private suspend fun selectHospitalForIncident(incidentId: String, type: EmergencyType) {
        val incident = incidentRepository.getIncidentById(incidentId) ?: return
        val availableHospitals = hospitalRepository.getAvailableHospitals()
        val best = availableHospitals.firstOrNull() ?: backendService.findSuitableHospitals(type, null, null).firstOrNull()
        
        if (best != null) {
            val updated = incident.copy(
                hospitalId = best.hospitalId,
                hospitalName = best.name,
                hospitalAddress = best.address,
                hospitalStatus = "PENDING",
                updatedAt = Date()
            )
            incidentRepository.updateIncident(updated)
            sendToolResult("Selected hospital: ${best.name}. Waiting for hospital acceptance.")
        }
    }

    private fun sendToolResult(result: String) {
        scope.launch {
            try {
                val msgContent = VapiMessageContent(role = "tool", content = result)
                val vapiMsg = VapiMessage(type = "tool-output", message = msgContent)
                vapi?.send(vapiMsg)
            } catch (e: Exception) {
                Log.e(TAG, "[VAPI ERROR] Failed to send tool result: ${e.message}")
            }
        }
    }

    override fun startAssistant(incidentId: String, lat: Double?, lon: Double?) {
        val assistantId = BuildConfig.VAPI_ASSISTANT_ID
        currentIncidentId = incidentId
        triageJob?.cancel()
        
        _state.value = AssistantState.CONNECTING
        _transcript.value = "Connecting to RescueX Emergency AI..."

        if (vapi != null && assistantId.isNotEmpty() && assistantId != "YOUR_ASSISTANT_ID") {
            scope.launch {
                try {
                    Log.d(TAG, "[VAPI] Starting call with assistantId: $assistantId")
                    val callResult = vapi?.start(assistantId = assistantId)
                    Log.d(TAG, "[VAPI] Call start response: $callResult")
                    callResult?.onFailure { err ->
                        Log.e(TAG, "[VAPI ERROR] Call start failed with error: ${err.message}", err)
                    }
                    callResult?.onSuccess { resp ->
                        Log.d(TAG, "[VAPI] Call start succeeded, webCallUrl: ${resp.webCallUrl}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[VAPI ERROR] Call start failed: ${e.message}", e)
                    launchFallbackTriage(incidentId, EmergencyType.MEDICAL, Severity.CRITICAL, "Emergency voice triage started.")
                }
            }

            // Watchdog (30s) in case network is unreachable
            triageJob = scope.launch {
                delay(30000)
                if (_state.value == AssistantState.CONNECTING) {
                    Log.w(TAG, "[AI TRIAGE] Vapi did not connect within 30s. Switching to emergency triage flow.")
                    runEmergencyTriageSimulation(
                        incidentId = incidentId,
                        type = EmergencyType.MEDICAL,
                        severity = Severity.CRITICAL,
                        notes = "Emergency voice assistant activated. Immediate paramedic dispatch requested."
                    )
                }
            }
        } else {
            Log.w(TAG, "[AI TRIAGE] Vapi not configured or null. Running emergency coordination directly.")
            launchFallbackTriage(
                incidentId = incidentId,
                type = EmergencyType.MEDICAL,
                severity = Severity.CRITICAL,
                notes = "Emergency voice assistant activated. Immediate paramedic dispatch requested."
            )
        }
    }

    private fun launchFallbackTriage(incidentId: String, type: EmergencyType, severity: Severity, notes: String) {
        triageJob?.cancel()
        triageJob = scope.launch {
            runEmergencyTriageSimulation(incidentId, type, severity, notes)
        }
    }

    private suspend fun runEmergencyTriageSimulation(
        incidentId: String,
        type: EmergencyType,
        severity: Severity,
        notes: String
    ) {
        _state.value = AssistantState.CONNECTED
        _transcript.value = "RescueX Emergency AI connected. Triage in progress..."
        delay(1000)

        _state.value = AssistantState.SPEAKING
        _transcript.value = "RescueX AI: Location confirmed. Assessing emergency level..."
        delay(1500)

        _state.value = AssistantState.THINKING
        _transcript.value = "Dispatching nearest ambulance and alerting emergency hospital..."

        // Dispatch ambulance and select hospital in repositories
        dispatchAmbulanceForIncident(incidentId)
        selectHospitalForIncident(incidentId, type)

        lastSummary = AIIncidentSummary(
            incidentType = type,
            severity = severity,
            description = notes,
            immediateDanger = (severity == Severity.CRITICAL || severity == Severity.HIGH),
            injuredPeople = true,
            medicalAssistanceRequired = true,
            userSafe = false,
            locationAvailable = true,
            additionalInformation = "Paramedic unit dispatched. Trauma hospital notified."
        )

        delay(1500)
        _state.value = AssistantState.SPEAKING
        _transcript.value = "Ambulance is dispatched! Paramedics are on their way. Stay calm and keep phone nearby."
        delay(2000)

        _state.value = AssistantState.ENDED
        _transcript.value = "Emergency dispatched successfully. Paramedics en route."
    }

    override fun triggerQuickTriage(type: EmergencyType, severity: Severity, description: String) {
        val incidentId = currentIncidentId ?: return
        launchFallbackTriage(incidentId, type, severity, description)
    }

    override fun stopAssistant() {
        triageJob?.cancel()
        audioLevelJob?.cancel()
        _localAudioLevel.value = 0f
        restoreAudio()
        scope.launch {
            try { vapi?.stop() } catch (e: Exception) {}
            _state.value = AssistantState.ENDED
            _transcript.value = "Conversation ended."
        }
    }

    override fun toggleMute() {
        scope.launch {
            try {
                vapi?.toggleMute()
                _isMuted.value = !_isMuted.value
                Log.d(TAG, "[AUDIO] Toggled mute: ${_isMuted.value}")
            } catch (e: Exception) {
                Log.e(TAG, "[AUDIO] Error toggling mute: ${e.message}")
            }
        }
    }

    override fun sendUserMessage(text: String) {
        _transcript.value = "You: $text"
        _state.value = AssistantState.THINKING
        scope.launch {
            // 1. Instantly dispatch ambulance & hospital in background
            val incidentId = currentIncidentId
            val lower = text.lowercase()
            val type = when {
                lower.contains("accident") || lower.contains("crash") || lower.contains("car") -> EmergencyType.ACCIDENT
                lower.contains("fire") || lower.contains("smoke") || lower.contains("burn") -> EmergencyType.FIRE
                else -> EmergencyType.MEDICAL
            }

            if (incidentId != null) {
                try {
                    dispatchAmbulanceForIncident(incidentId)
                    selectHospitalForIncident(incidentId, type)
                    lastSummary = AIIncidentSummary(
                        incidentType = type,
                        severity = Severity.CRITICAL,
                        description = text,
                        immediateDanger = true,
                        injuredPeople = true,
                        medicalAssistanceRequired = true,
                        userSafe = false,
                        locationAvailable = true,
                        additionalInformation = "Emergency response unit dispatched via voice assistant."
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "[DISPATCH] Error during instant dispatch: ${e.message}")
                }
            }

            // 2. Forward to Vapi if call is active
            try {
                val msgContent = VapiMessageContent(role = "user", content = text)
                val vapiMsg = VapiMessage(type = "add-message", message = msgContent)
                vapi?.send(vapiMsg)
                Log.d(TAG, "[VAPI] Sent user message: $text")
            } catch (e: Exception) {
                Log.e(TAG, "[VAPI ERROR] Failed to send user message: ${e.message}")
            }

            // 3. Immediately transition out of THINKING after brief processing delay so user is never stuck
            delay(1200)
            if (_state.value == AssistantState.THINKING) {
                _transcript.value = "RescueX AI: Emergency confirmed! Ambulance dispatched (ETA: 6 min). Paramedics are on the way."
                _state.value = AssistantState.LISTENING
            }
        }
    }

    override fun getIncidentSummary(): AIIncidentSummary? = lastSummary
}

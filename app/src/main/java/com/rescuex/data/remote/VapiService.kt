package com.rescuex.data.remote

import ai.vapi.Vapi
import android.content.Context
import com.rescuex.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class VapiEvent {
    object CallStarted : VapiEvent()
    object CallEnded : VapiEvent()
    data class SpeechUpdate(val text: String, val isFinal: Boolean) : VapiEvent()
    data class Error(val message: String) : VapiEvent()
    data class StructuredData(val data: Map<String, Any>) : VapiEvent()
}

class VapiService(context: Context) {
    private val vapi = Vapi(publicKey = BuildConfig.VAPI_PUBLIC_KEY)
    
    private val _events = MutableSharedFlow<VapiEvent>()
    val events: Flow<VapiEvent> = _events.asSharedFlow()

    init {
        // In a real implementation with the Vapi SDK 2.0.3, 
        // we would collect from vapi.eventFlow
        /*
        vapi.eventFlow.collect { event ->
            when (event) {
                is Vapi.Event.CallDidStart -> _events.emit(VapiEvent.CallStarted)
                is Vapi.Event.CallDidEnd -> _events.emit(VapiEvent.CallEnded)
                is Vapi.Event.SpeechUpdate -> _events.emit(VapiEvent.SpeechUpdate(event.text, true))
                is Vapi.Event.Error -> _events.emit(VapiEvent.Error(event.message))
                // Handle structured data if supported by SDK or via transcript events
            }
        }
        */
    }

    fun startCall(assistantId: String, contextData: Map<String, Any>) {
        if (BuildConfig.VAPI_PUBLIC_KEY.isEmpty() || assistantId.isEmpty()) {
            // Fallback to demo mode behavior or report error
            return
        }
        // Pass context data like incidentId and location
        // vapi.start(assistantId = assistantId, assistantOverrides = mapOf("variableValues" to contextData))
    }

    fun stopCall() {
        // vapi.stop()
    }
}

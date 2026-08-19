package com.rescuex.data.remote

import android.content.Context
import ai.vapi.android.Vapi
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
    // This is a placeholder or legacy service. 
    // Real logic is in VapiVoiceAssistantRepository.
    // Fixed imports and constructor to avoid build errors.
}

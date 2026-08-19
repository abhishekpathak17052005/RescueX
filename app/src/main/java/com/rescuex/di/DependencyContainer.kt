package com.rescuex.di

import android.content.Context
import com.rescuex.data.remote.EmergencyBackendService
import com.rescuex.data.repository.*
import com.rescuex.location.LocationManager

/**
 * Simple Dependency Container to ensure singletons are shared across the app.
 * In a production app, use Hilt or Koin.
 */
class DependencyContainer(context: Context) {
    val incidentRepository: IncidentRepository = MockIncidentRepository()
    val authRepository: AuthRepository = MockAuthRepository()
    val contactRepository: ContactRepository = MockContactRepository()
    val backendService = EmergencyBackendService()
    val locationManager = LocationManager(context)
    val voiceAssistantRepository: VoiceAssistantRepository = VapiVoiceAssistantRepository(
        context, 
        incidentRepository, 
        backendService
    )
}

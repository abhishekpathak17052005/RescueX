package com.rescuex.di

import android.content.Context
import com.rescuex.data.remote.EmergencyBackendService
import com.rescuex.data.repository.*
import com.rescuex.location.LocationManager

/**
 * Simple Dependency Container to ensure singletons are shared across the app.
 */
class DependencyContainer(context: Context) {
    val authRepository: AuthRepository = InMemoryAuthRepository()
    val incidentRepository: IncidentRepository = InMemoryIncidentRepository()
    val contactRepository: ContactRepository = MockContactRepository()
    val backendService = EmergencyBackendService()
    val locationManager = LocationManager(context)
    val ambulanceRepository: AmbulanceRepository = InMemoryAmbulanceRepository()
    val hospitalRepository: HospitalRepository = InMemoryHospitalRepository()
    val voiceAssistantRepository: VoiceAssistantRepository = VapiVoiceAssistantRepository(
        context, 
        incidentRepository, 
        ambulanceRepository,
        hospitalRepository,
        backendService
    )
}

package com.rescuex.data.remote

import com.rescuex.data.model.*
import java.util.*

/**
 * Service representing the RescueX backend tools and coordination.
 */
class EmergencyBackendService {

    private val mockHospitals = listOf(
        Hospital(
            hospitalId = "H-001",
            name = "City Central Hospital",
            address = "123 Medical Dr, Downtown",
            latitude = 40.7128,
            longitude = -74.0060,
            emergencyCapabilities = listOf("CARDIOLOGY", "TRAUMA"),
            beds = mapOf("emergency" to 10, "icu" to 5),
            specialists = mapOf("cardiologist" to true),
            status = "ACTIVE",
            updatedAt = Date()
        ),
        Hospital(
            hospitalId = "H-002",
            name = "Northside Community Clinic",
            address = "456 North St, Uptown",
            latitude = 40.7589,
            longitude = -73.9851,
            emergencyCapabilities = listOf("GENERAL"),
            beds = mapOf("emergency" to 5, "icu" to 0),
            specialists = mapOf("cardiologist" to false),
            status = "ACTIVE",
            updatedAt = Date()
        )
    )

    fun getPatientEmergencyProfile(userId: String): User? {
        return User(userId, "Demo User", "demo@rescuex.app", "+91 XXXXX XXXXX")
    }

    fun dispatchAmbulance(incidentId: String, patientId: String): Ambulance {
        return Ambulance(
            ambulanceId = "AMB-DEMO-001", // Standardized for demo fallback
            status = AmbulanceStatus.DISPATCHED,
            etaMinutes = 7,
            updatedAt = Date()
        )
    }

    fun findSuitableHospitals(
        type: EmergencyType,
        patientLat: Double?,
        patientLon: Double?
    ): List<Hospital> {
        return mockHospitals
    }

    fun selectBestHospital(hospitals: List<Hospital>, type: EmergencyType): Hospital? {
        return hospitals.firstOrNull()
    }

    fun updateAmbulanceDestination(
        incidentId: String,
        ambulanceId: String,
        hospitalId: String
    ): Boolean {
        return true
    }
}

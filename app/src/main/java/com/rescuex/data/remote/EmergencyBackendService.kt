package com.rescuex.data.remote

import com.rescuex.data.model.*
import java.util.*

/**
 * Service representing the RescueX backend tools and coordination.
 * For Phase 4, this implements mock logic for ambulance dispatch and hospital selection.
 */
class EmergencyBackendService {

    private val mockHospitals = listOf(
        Hospital(
            id = "H-001",
            name = "City Central Hospital",
            address = "123 Medical Dr, Downtown",
            latitude = 40.7128,
            longitude = -74.0060,
            emergencyDepartmentAvailable = true,
            requiredCapabilityAvailable = true,
            bedAvailable = true,
            specialistAvailable = true,
            estimatedTravelTimeMinutes = 8,
            distanceKm = 4.2,
            lastVerifiedAt = Date()
        ),
        Hospital(
            id = "H-002",
            name = "Northside Community Clinic",
            address = "456 North St, Uptown",
            latitude = 40.7589,
            longitude = -73.9851,
            emergencyDepartmentAvailable = true,
            requiredCapabilityAvailable = false, // No cardiac capability for example
            bedAvailable = true,
            specialistAvailable = false,
            estimatedTravelTimeMinutes = 5,
            distanceKm = 2.1,
            lastVerifiedAt = Date()
        ),
        Hospital(
            id = "H-003",
            name = "St. Mary's General",
            address = "789 Health Ave, Westside",
            latitude = 40.7306,
            longitude = -73.9352,
            emergencyDepartmentAvailable = true,
            requiredCapabilityAvailable = true,
            bedAvailable = false, // Full
            specialistAvailable = true,
            estimatedTravelTimeMinutes = 12,
            distanceKm = 6.5,
            lastVerifiedAt = Date()
        )
    )

    fun getPatientEmergencyProfile(userId: String): User? {
        // Mock profile retrieval
        return User(userId, "Demo User", "demo@rescuex.app", "+91 XXXXX XXXXX")
    }

    fun dispatchAmbulance(incidentId: String, patientId: String): Ambulance {
        // Mock dispatch logic
        return Ambulance(
            id = "AMB-" + (100 + Random().nextInt(900)),
            status = AmbulanceStatus.DISPATCHED,
            etaMinutes = 7
        )
    }

    fun findSuitableHospitals(
        type: EmergencyType,
        patientLat: Double?,
        patientLon: Double?
    ): List<Hospital> {
        // Mock search logic
        return mockHospitals.filter { it.emergencyDepartmentAvailable }
    }

    fun selectBestHospital(hospitals: List<Hospital>, type: EmergencyType): Hospital? {
        // Logic: Capability -> Bed -> Travel Time
        return hospitals
            .filter { it.requiredCapabilityAvailable }
            .filter { it.bedAvailable }
            .minByOrNull { it.estimatedTravelTimeMinutes }
            ?: hospitals.minByOrNull { it.distanceKm }
    }

    fun updateAmbulanceDestination(
        incidentId: String,
        ambulanceId: String,
        hospitalId: String
    ): Boolean {
        // Mock update
        return true
    }
}

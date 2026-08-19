package com.rescuex.data.model

import java.util.Date

enum class IncidentStatus { SOS_ACTIVATED, INFO_COLLECTION, RESPONDER_ASSIGNED, RESPONDER_EN_ROUTE, RESPONDER_ARRIVED, RESOLVED }
enum class EmergencyType { MEDICAL, ACCIDENT, PERSONAL_SAFETY, FIRE, NATURAL_DISASTER, MISSING_PERSON, SECURITY_THREAT, OTHER, NOT_SPECIFIED }
enum class Severity { PENDING, LOW, MEDIUM, HIGH, CRITICAL }
enum class AmbulanceStatus { NOT_REQUESTED, REQUESTED, DISPATCHED, EN_ROUTE, ARRIVED, COMPLETED }

data class Hospital(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val emergencyDepartmentAvailable: Boolean,
    val requiredCapabilityAvailable: Boolean,
    val bedAvailable: Boolean,
    val specialistAvailable: Boolean,
    val estimatedTravelTimeMinutes: Int,
    val distanceKm: Double,
    val lastVerifiedAt: Date
)

data class Ambulance(
    val id: String,
    val status: AmbulanceStatus,
    val etaMinutes: Int? = null,
    val destinationHospitalId: String? = null
)

data class AIIncidentSummary(
    val incidentType: EmergencyType = EmergencyType.NOT_SPECIFIED,
    val severity: Severity = Severity.PENDING,
    val description: String? = null,
    val immediateDanger: Boolean? = null,
    val injuredPeople: Boolean? = null,
    val medicalAssistanceRequired: Boolean? = null,
    val numberOfPeople: Int? = null,
    val additionalInformation: String? = null,
    val userSafe: Boolean? = null,
    val locationAvailable: Boolean? = null
)

data class Incident(
    val id: String, 
    val type: EmergencyType, 
    val severity: Severity, 
    val status: IncidentStatus, 
    val timestamp: Date, 
    val aiSummary: String? = null, 
    val location: String? = null, 
    val userId: String,
    val structuredAiSummary: AIIncidentSummary? = null,
    val ambulance: Ambulance? = null,
    val selectedHospital: Hospital? = null
)

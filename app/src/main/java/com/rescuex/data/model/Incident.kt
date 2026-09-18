package com.rescuex.data.model

import java.util.Date

enum class IncidentStatus { 
    SOS_ACTIVE, INFO_COLLECTION, AMBULANCE_DISPATCHED, RESPONDER_ASSIGNED, 
    RESPONDER_EN_ROUTE, RESPONDER_ARRIVED, RESOLVED 
}
enum class EmergencyType { MEDICAL, ACCIDENT, PERSONAL_SAFETY, FIRE, NATURAL_DISASTER, MISSING_PERSON, SECURITY_THREAT, OTHER, NOT_SPECIFIED }
enum class Severity { PENDING, LOW, MEDIUM, HIGH, CRITICAL }

enum class AmbulanceStatus { 
    NOT_REQUESTED, DISPATCHED, ACCEPTED, EN_ROUTE_TO_PATIENT, 
    ARRIVED_AT_PATIENT, PATIENT_PICKED_UP, EN_ROUTE_TO_HOSPITAL, 
    ARRIVED_AT_HOSPITAL, COMPLETED, REJECTED 
}

data class Hospital(
    val hospitalId: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val emergencyCapabilities: List<String> = emptyList(),
    val beds: Map<String, Int> = emptyMap(),
    val specialists: Map<String, Boolean> = emptyMap(),
    val status: String = "ACTIVE",
    val updatedAt: Date = Date()
)

data class Ambulance(
    val ambulanceId: String = "",
    val driverUid: String = "",
    val status: AmbulanceStatus = AmbulanceStatus.NOT_REQUESTED,
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val activeIncidentId: String? = null,
    val etaMinutes: Int? = null,
    val updatedAt: Date = Date()
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
    val incidentId: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val patientPhone: String = "",
    val pickupAddress: String = "",
    val pickupLatitude: Double = 0.0,
    val pickupLongitude: Double = 0.0,
    val emergencyType: EmergencyType = EmergencyType.NOT_SPECIFIED,
    val severity: Severity = Severity.PENDING,
    val reportedSymptoms: String? = null,
    
    val ambulanceId: String? = null,
    val ambulanceStatus: AmbulanceStatus = AmbulanceStatus.NOT_REQUESTED,
    val etaMinutes: Int? = null,
    
    val hospitalId: String? = null,
    val hospitalName: String? = null,
    val hospitalAddress: String? = null,
    val hospitalLatitude: Double? = null,
    val hospitalLongitude: Double? = null,
    val hospitalStatus: String? = null,
    
    val status: IncidentStatus = IncidentStatus.SOS_ACTIVE,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    
    val structuredAiSummary: AIIncidentSummary? = null
)

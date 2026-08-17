package com.rescuex.data.model

import java.util.Date

enum class IncidentStatus { SOS_ACTIVATED, INFO_COLLECTION, RESPONDER_ASSIGNED, RESPONDER_EN_ROUTE, RESPONDER_ARRIVED, RESOLVED }
enum class EmergencyType { MEDICAL, ACCIDENT, PERSONAL_SAFETY, FIRE, NATURAL_DISASTER, MISSING_PERSON, SECURITY_THREAT, OTHER, NOT_SPECIFIED }
enum class Severity { PENDING, LOW, MEDIUM, HIGH, CRITICAL }

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

data class AIConversation(
    val conversationId: String,
    val incidentId: String,
    val status: String, // STARTED, ENDED, FAILED
    val startedAt: Date,
    val endedAt: Date? = null,
    val transcript: String? = null,
    val summary: AIIncidentSummary? = null
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
    val structuredAiSummary: AIIncidentSummary? = null
)

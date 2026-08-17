package com.rescuex.data.model

import java.util.Date

enum class IncidentStatus { SOS_ACTIVATED, INFO_COLLECTION, RESPONDER_ASSIGNED, RESPONDER_EN_ROUTE, RESPONDER_ARRIVED, RESOLVED }
enum class EmergencyType { MEDICAL, ACCIDENT, PERSONAL_SAFETY, FIRE, OTHER, NOT_SPECIFIED }
enum class Severity { PENDING, LOW, MEDIUM, HIGH, CRITICAL }

data class Incident(val id: String, val type: EmergencyType, val severity: Severity, val status: IncidentStatus, val timestamp: Date, val aiSummary: String? = null, val location: String? = null, val userId: String)

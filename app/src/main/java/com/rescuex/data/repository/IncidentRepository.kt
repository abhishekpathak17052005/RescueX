package com.rescuex.data.repository

import com.rescuex.data.model.Incident
import com.rescuex.data.model.IncidentStatus
import com.rescuex.data.model.EmergencyType
import com.rescuex.data.model.Severity
import java.util.*

interface IncidentRepository {
    fun getIncidents(): List<Incident>
    fun getIncidentById(id: String): Incident?
    fun createIncident(type: EmergencyType): Incident
}

class MockIncidentRepository : IncidentRepository {
    private val incidents = mutableListOf(
        Incident("RX-1001", EmergencyType.MEDICAL, Severity.HIGH, IncidentStatus.RESOLVED, Date(), "User reported a possible medical emergency and requested immediate assistance.", "Downtown, City A", "user1"),
        Incident("RX-1002", EmergencyType.ACCIDENT, Severity.CRITICAL, IncidentStatus.RESOLVED, Date(), "Vehicle collision reported.", "Highway 1, Mile 42", "user1"),
        Incident("RX-1003", EmergencyType.PERSONAL_SAFETY, Severity.MEDIUM, IncidentStatus.RESOLVED, Date(), "User felt unsafe and requested monitoring.", "Park Ave, City B", "user1")
    )

    override fun getIncidents(): List<Incident> = incidents

    override fun getIncidentById(id: String): Incident? = incidents.find { it.id == id }

    override fun createIncident(type: EmergencyType): Incident {
        val newIncident = Incident(
            "RX-DEMO-" + (1000 + incidents.size),
            type,
            Severity.PENDING,
            IncidentStatus.SOS_ACTIVATED,
            Date(),
            "",
            "Pending...",
            "user1"
        )
        incidents.add(0, newIncident)
        return newIncident
    }
}

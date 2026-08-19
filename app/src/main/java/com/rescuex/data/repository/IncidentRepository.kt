package com.rescuex.data.repository

import com.rescuex.data.model.Incident
import com.rescuex.data.model.IncidentStatus
import com.rescuex.data.model.EmergencyType
import com.rescuex.data.model.Severity
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*

interface IncidentRepository {
    val incidentUpdates: Flow<Incident>
    fun getIncidents(): List<Incident>
    fun getIncidentById(id: String): Incident?
    fun createIncident(type: EmergencyType): Incident
    fun updateIncident(incident: Incident)
}

class MockIncidentRepository : IncidentRepository {
    private val incidents = mutableListOf(
        Incident("RX-1001", EmergencyType.MEDICAL, Severity.HIGH, IncidentStatus.RESOLVED, Date(), "User reported a possible medical emergency and requested immediate assistance.", "Downtown, City A", "user1"),
        Incident("RX-1002", EmergencyType.ACCIDENT, Severity.CRITICAL, IncidentStatus.RESOLVED, Date(), "Vehicle collision reported.", "Highway 1, Mile 42", "user1"),
        Incident("RX-1003", EmergencyType.PERSONAL_SAFETY, Severity.MEDIUM, IncidentStatus.RESOLVED, Date(), "User felt unsafe and requested monitoring.", "Park Ave, City B", "user1")
    )

    private val _updates = MutableSharedFlow<Incident>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val incidentUpdates: Flow<Incident> = _updates.asSharedFlow()

    override fun getIncidents(): List<Incident> = ArrayList(incidents)

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
        _updates.tryEmit(newIncident)
        return newIncident
    }

    override fun updateIncident(incident: Incident) {
        val index = incidents.indexOfFirst { it.id == incident.id }
        if (index != -1) {
            incidents[index] = incident
            _updates.tryEmit(incident)
        }
    }
}

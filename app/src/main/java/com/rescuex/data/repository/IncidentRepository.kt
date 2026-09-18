package com.rescuex.data.repository

import android.util.Log
import com.rescuex.data.model.EmergencyType
import com.rescuex.data.model.Incident
import com.rescuex.data.model.IncidentStatus
import com.rescuex.data.model.Severity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import java.util.*

interface IncidentRepository {
    val incidentUpdates: Flow<Incident>
    suspend fun getIncidents(): List<Incident>
    suspend fun getIncidentById(id: String): Incident?
    suspend fun createIncident(incident: Incident): Result<Incident>
    suspend fun updateIncident(incident: Incident): Result<Unit>
    fun listenToIncidents(patientId: String? = null, ambulanceId: String? = null, hospitalId: String? = null): Flow<List<Incident>>
}

class InMemoryIncidentRepository : IncidentRepository {
    private val TAG = "INCIDENT_REPO"

    private val incidentsState = MutableStateFlow<List<Incident>>(emptyList())
    private val _incidentUpdates = MutableSharedFlow<Incident>(extraBufferCapacity = 64)
    override val incidentUpdates: Flow<Incident> = _incidentUpdates.asSharedFlow()

    init {
        // Pre-seed an active demo incident
        val demoIncident = Incident(
            incidentId = "INC-DEMO-001",
            patientId = "user-patient-01",
            patientName = "Demo Patient",
            patientPhone = "+91 98765 43210",
            emergencyType = EmergencyType.MEDICAL,
            severity = Severity.CRITICAL,
            status = IncidentStatus.AMBULANCE_DISPATCHED,
            pickupLatitude = 40.7128,
            pickupLongitude = -74.0060,
            pickupAddress = "123 Main St, Downtown",
            ambulanceId = "AMB-DEMO-001",
            hospitalId = "H-001",
            reportedSymptoms = "Patient experiencing severe chest pain and shortness of breath.",
            createdAt = Date()
        )
        incidentsState.value = listOf(demoIncident)
    }

    override suspend fun getIncidents(): List<Incident> = incidentsState.value

    override suspend fun getIncidentById(id: String): Incident? =
        incidentsState.value.find { it.incidentId == id }

    override suspend fun createIncident(incident: Incident): Result<Incident> {
        val id = if (incident.incidentId.isBlank()) "INC-${UUID.randomUUID().toString().take(8).uppercase()}" else incident.incidentId
        val newIncident = incident.copy(incidentId = id, createdAt = incident.createdAt ?: Date())
        val current = incidentsState.value.toMutableList()
        current.add(0, newIncident)
        incidentsState.value = current
        _incidentUpdates.tryEmit(newIncident)
        Log.i(TAG, "[INCIDENT] Created incident: $id")
        return Result.success(newIncident)
    }

    override suspend fun updateIncident(incident: Incident): Result<Unit> {
        val current = incidentsState.value.toMutableList()
        val index = current.indexOfFirst { it.incidentId == incident.incidentId }
        if (index != -1) {
            current[index] = incident
            incidentsState.value = current
            _incidentUpdates.tryEmit(incident)
            Log.i(TAG, "[INCIDENT] Updated incident: ${incident.incidentId}")
        } else {
            current.add(0, incident)
            incidentsState.value = current
            _incidentUpdates.tryEmit(incident)
        }
        return Result.success(Unit)
    }

    override fun listenToIncidents(
        patientId: String?,
        ambulanceId: String?,
        hospitalId: String?
    ): Flow<List<Incident>> {
        return incidentsState.map { list ->
            list.filter { item ->
                (patientId == null || item.patientId == patientId) &&
                (ambulanceId == null || item.ambulanceId == ambulanceId) &&
                (hospitalId == null || item.hospitalId == hospitalId)
            }
        }
    }
}

typealias MockIncidentRepository = InMemoryIncidentRepository


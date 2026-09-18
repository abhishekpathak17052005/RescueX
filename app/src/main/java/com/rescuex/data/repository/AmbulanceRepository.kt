package com.rescuex.data.repository

import com.rescuex.data.model.Ambulance
import com.rescuex.data.model.AmbulanceStatus
import java.util.*

interface AmbulanceRepository {
    suspend fun registerAmbulance(ambulance: Ambulance): Result<Unit>
    suspend fun updateAmbulanceStatus(ambulanceId: String, status: AmbulanceStatus): Result<Unit>
    suspend fun getAvailableAmbulances(): List<Ambulance>
    suspend fun getAmbulanceByDriverUid(uid: String): Ambulance?
}

class InMemoryAmbulanceRepository : AmbulanceRepository {
    private val ambulances = mutableListOf<Ambulance>()

    init {
        // Pre-seed ambulances
        ambulances.add(
            Ambulance(
                ambulanceId = "AMB-DEMO-001",
                driverUid = "user-driver-01",
                status = AmbulanceStatus.NOT_REQUESTED,
                currentLatitude = 40.7128,
                currentLongitude = -74.0060,
                etaMinutes = 5,
                updatedAt = Date()
            )
        )
        ambulances.add(
            Ambulance(
                ambulanceId = "AMB-DEMO-002",
                driverUid = "user-driver-02",
                status = AmbulanceStatus.NOT_REQUESTED,
                currentLatitude = 40.7306,
                currentLongitude = -73.9352,
                etaMinutes = 8,
                updatedAt = Date()
            )
        )
    }

    override suspend fun registerAmbulance(ambulance: Ambulance): Result<Unit> {
        val existingIndex = ambulances.indexOfFirst { it.ambulanceId == ambulance.ambulanceId }
        if (existingIndex != -1) {
            ambulances[existingIndex] = ambulance
        } else {
            ambulances.add(ambulance)
        }
        return Result.success(Unit)
    }

    override suspend fun updateAmbulanceStatus(ambulanceId: String, status: AmbulanceStatus): Result<Unit> {
        val index = ambulances.indexOfFirst { it.ambulanceId == ambulanceId }
        if (index != -1) {
            val existing = ambulances[index]
            ambulances[index] = existing.copy(status = status, updatedAt = Date())
            return Result.success(Unit)
        }
        return Result.failure(Exception("Ambulance not found"))
    }

    override suspend fun getAvailableAmbulances(): List<Ambulance> {
        return ambulances.filter { it.status == AmbulanceStatus.NOT_REQUESTED }
    }

    override suspend fun getAmbulanceByDriverUid(uid: String): Ambulance? {
        return ambulances.firstOrNull { it.driverUid == uid }
    }
}

typealias MockAmbulanceRepository = InMemoryAmbulanceRepository


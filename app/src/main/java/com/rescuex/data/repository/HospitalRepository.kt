package com.rescuex.data.repository

import com.rescuex.data.model.Hospital
import java.util.*

interface HospitalRepository {
    suspend fun registerHospital(hospital: Hospital): Result<Unit>
    suspend fun getAvailableHospitals(): List<Hospital>
}

class InMemoryHospitalRepository : HospitalRepository {
    private val hospitals = mutableListOf<Hospital>()

    init {
        hospitals.addAll(
            listOf(
                Hospital(
                    hospitalId = "H-001",
                    name = "City Central Hospital",
                    address = "123 Medical Dr, Downtown",
                    latitude = 40.7128,
                    longitude = -74.0060,
                    emergencyCapabilities = listOf("CARDIOLOGY", "TRAUMA", "STROKE", "GENERAL"),
                    beds = mapOf("emergency" to 12, "icu" to 6),
                    specialists = mapOf("cardiologist" to true, "traumaSurgeon" to true),
                    status = "ACTIVE",
                    updatedAt = Date()
                ),
                Hospital(
                    hospitalId = "H-002",
                    name = "Northside Community Clinic",
                    address = "456 North St, Uptown",
                    latitude = 40.7589,
                    longitude = -73.9851,
                    emergencyCapabilities = listOf("GENERAL", "PEDIATRIC"),
                    beds = mapOf("emergency" to 5, "icu" to 1),
                    specialists = mapOf("cardiologist" to false),
                    status = "ACTIVE",
                    updatedAt = Date()
                )
            )
        )
    }

    override suspend fun registerHospital(hospital: Hospital): Result<Unit> {
        val existingIndex = hospitals.indexOfFirst { it.hospitalId == hospital.hospitalId }
        if (existingIndex != -1) {
            hospitals[existingIndex] = hospital
        } else {
            hospitals.add(hospital)
        }
        return Result.success(Unit)
    }

    override suspend fun getAvailableHospitals(): List<Hospital> {
        return hospitals.filter { it.status == "ACTIVE" }
    }
}

typealias MockHospitalRepository = InMemoryHospitalRepository


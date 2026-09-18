package com.rescuex.data.model

import java.util.Date

enum class UserRole { PATIENT, AMBULANCE, HOSPITAL, ADMIN }

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole? = null,
    val createdAt: Date = Date()
)

data class PatientProfile(
    val uid: String = "",
    val address: String = "",
    val emergencyContacts: List<String> = emptyList(),
    val medicalInfo: String = ""
)

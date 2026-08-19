package com.rescuex.data.model

enum class UserRole { PATIENT, AMBULANCE_DRIVER, HOSPITAL_ER }

data class User(
    val id: String, 
    val name: String, 
    val email: String, 
    val phone: String, 
    val role: UserRole = UserRole.PATIENT,
    val hospitalId: String? = null // For hospital staff
)

package com.rescuex.data.repository

import com.rescuex.data.model.User
import com.rescuex.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    fun getCurrentUser(): User?
    fun switchRole(role: UserRole)
}

class MockAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(
        User("user1", "Demo User", "demo@rescuex.app", "+91 XXXXX XXXXX", UserRole.PATIENT)
    )
    override val currentUser: StateFlow<User?> = _currentUser

    override fun getCurrentUser(): User? = _currentUser.value

    override fun switchRole(role: UserRole) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(
            role = role,
            hospitalId = if (role == UserRole.HOSPITAL_ER) "H-001" else null
        )
    }
}

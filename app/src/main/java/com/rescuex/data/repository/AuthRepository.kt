package com.rescuex.data.repository

import com.rescuex.data.model.User

interface AuthRepository {
    fun getCurrentUser(): User?
}

class MockAuthRepository : AuthRepository {
    override fun getCurrentUser(): User = User("user1", "Demo User", "demo@rescuex.app", "+91 XXXXX XXXXX")
}

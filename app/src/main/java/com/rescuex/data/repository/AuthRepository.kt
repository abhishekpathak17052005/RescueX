package com.rescuex.data.repository

import android.util.Log
import com.rescuex.data.model.User
import com.rescuex.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

interface AuthRepository {
    val currentUser: StateFlow<User?>
    val lastError: StateFlow<String?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, phone: String, role: UserRole, password: String): Result<User>
    fun logout()
    suspend fun fetchCurrentUserRole(): UserRole?
}

class InMemoryAuthRepository : AuthRepository {
    private val TAG = "AUTH"

    // In-memory user store
    private val usersByEmail = mutableMapOf<String, Pair<User, String>>()

    private val defaultUser = User("user-patient-01", "Demo Patient", "patient@rescuex.app", "+91 98765 43210", UserRole.PATIENT)
    private val _currentUser = MutableStateFlow<User?>(defaultUser)
    override val currentUser: StateFlow<User?> = _currentUser

    private val _lastError = MutableStateFlow<String?>(null)
    override val lastError: StateFlow<String?> = _lastError

    init {
        // Pre-seed demo users for each role
        val demoUsers = listOf(
            Triple(
                User("user-patient-01", "Demo Patient", "patient@rescuex.app", "+91 98765 43210", UserRole.PATIENT),
                "patient@rescuex.app",
                "password"
            ),
            Triple(
                User("user-dispatcher-01", "Demo Dispatcher", "dispatcher@rescuex.app", "+91 98765 43211", UserRole.ADMIN),
                "dispatcher@rescuex.app",
                "password"
            ),
            Triple(
                User("user-driver-01", "Demo Ambulance Driver", "driver@rescuex.app", "+91 98765 43212", UserRole.AMBULANCE),
                "driver@rescuex.app",
                "password"
            ),
            Triple(
                User("user-hospital-01", "Demo Hospital Staff", "hospital@rescuex.app", "+91 98765 43213", UserRole.HOSPITAL),
                "hospital@rescuex.app",
                "password"
            ),
            Triple(
                User("user-demo", "Demo User", "demo@rescuex.app", "+91 99999 99999", UserRole.PATIENT),
                "demo@rescuex.app",
                "password"
            )
        )

        demoUsers.forEach { (user, email, pwd) ->
            usersByEmail[email.lowercase()] = Pair(user, pwd)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        Log.i(TAG, "[AUTH] Attempting login for: $normalizedEmail")
        _lastError.value = null

        val record = usersByEmail[normalizedEmail]
        if (record == null) {
            val err = "USER_NOT_FOUND"
            _lastError.value = err
            return Result.failure(Exception(err))
        }

        val (user, storedPassword) = record
        if (storedPassword != password) {
            val err = "WRONG_PASSWORD"
            _lastError.value = err
            return Result.failure(Exception(err))
        }

        Log.i(TAG, "[AUTH] Login successful for UID: ${user.uid}, Role: ${user.role}")
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun register(
        name: String,
        email: String,
        phone: String,
        role: UserRole,
        password: String
    ): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        Log.i(TAG, "[AUTH] Attempting registration for: $normalizedEmail")
        _lastError.value = null

        if (usersByEmail.containsKey(normalizedEmail)) {
            val err = "EMAIL_ALREADY_IN_USE"
            _lastError.value = err
            return Result.failure(Exception(err))
        }

        val uid = "user-" + UUID.randomUUID().toString().take(8)
        val user = User(
            uid = uid,
            name = name.trim(),
            email = normalizedEmail,
            phone = phone.trim(),
            role = role,
            createdAt = Date()
        )

        usersByEmail[normalizedEmail] = Pair(user, password)
        Log.i(TAG, "[AUTH] User registered successfully: UID: $uid, Role: $role")
        _currentUser.value = user
        return Result.success(user)
    }

    override fun logout() {
        Log.i(TAG, "[AUTH] Logging out")
        _currentUser.value = null
        _lastError.value = null
    }

    override suspend fun fetchCurrentUserRole(): UserRole? = _currentUser.value?.role
}

class MockAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(
        User("user1", "Demo User", "demo@rescuex.app", "+91 XXXXX XXXXX", UserRole.PATIENT)
    )
    override val currentUser: StateFlow<User?> = _currentUser
    override val lastError: StateFlow<String?> = MutableStateFlow(null)

    override suspend fun login(email: String, password: String): Result<User> {
        return Result.success(_currentUser.value!!)
    }

    override suspend fun register(name: String, email: String, phone: String, role: UserRole, password: String): Result<User> {
        val user = User("mock-uid", name, email, phone, role)
        _currentUser.value = user
        return Result.success(user)
    }

    override fun logout() {
        _currentUser.value = null
    }

    override suspend fun fetchCurrentUserRole(): UserRole? = _currentUser.value?.role
}

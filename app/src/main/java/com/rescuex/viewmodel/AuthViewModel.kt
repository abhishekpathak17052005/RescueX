package com.rescuex.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuex.data.model.UserRole
import com.rescuex.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    object LoggedOut : AuthState()
    data class Authenticated(
        val uid: String,
        val role: UserRole
    ) : AuthState()
    data class Error(
        val message: String
    ) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val TAG = "AUTH DEBUG"

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state

    init {
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                if (user != null) {
                    if (user.role == null) {
                        if (user.uid.isNotEmpty()) {
                            // Waiting for lastError to trigger or success
                            Log.d(TAG, "[AUTH DEBUG] User session detected, profile pending...")
                        } else {
                            _state.value = AuthState.Loading
                        }
                    } else {
                        Log.i(TAG, "[AUTH DEBUG] Loading user profile")
                        Log.i(TAG, "[AUTH DEBUG] UID = ${user.uid}")
                        Log.i(TAG, "[AUTH DEBUG] User role = ${user.role}")
                        _state.value = AuthState.Authenticated(user.uid, user.role)
                    }
                } else {
                    _state.value = AuthState.LoggedOut
                }
            }
        }

        viewModelScope.launch {
            repository.lastError.collect { error ->
                if (error != null) {
                    val userFriendlyMessage = when (error) {
                        "OFFLINE", "UNAVAILABLE" -> "Unable to connect to RescueX services. Check your internet connection."
                        "PROFILE_NOT_CONFIGURED" -> "User profile not configured."
                        "ROLE_NOT_CONFIGURED" -> "User role not configured."
                        "PERMISSION_DENIED" -> "Access denied. Please contact support."
                        "INVALID_EMAIL" -> "Please enter a valid email address."
                        "WRONG_PASSWORD" -> "Incorrect password. Please try again."
                        "USER_NOT_FOUND" -> "No account found with this email."
                        "USER_DISABLED" -> "This account has been disabled."
                        "EMAIL_ALREADY_IN_USE" -> "This email is already registered."
                        "WEAK_PASSWORD" -> "Password is too weak. Please use a stronger password."
                        else -> "Authentication error: $error"
                    }
                    Log.e(TAG, "[AUTH DEBUG] Background error detected: $error -> $userFriendlyMessage")
                    _state.value = AuthState.Error(userFriendlyMessage)
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = repository.login(email, password)
            if (result.isFailure) {
                // repository.login failure will update lastError or return it
                val error = result.exceptionOrNull()?.message ?: "AUTH_FAILED"
                Log.e(TAG, "[AUTH DEBUG] Login error: $error")
                // repository.login returns Result.failure(Exception(errorMsg))
            }
        }
    }

    fun register(name: String, email: String, phone: String, role: UserRole, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = repository.register(name, email, phone, role, password)
            if (result.isFailure) {
                val error = result.exceptionOrNull()?.message ?: "REGISTRATION_FAILED"
                Log.e(TAG, "[AUTH DEBUG] Registration error: $error")
            }
        }
    }

    fun logout() {
        Log.i(TAG, "[AUTH DEBUG] User logging out")
        repository.logout()
        _state.value = AuthState.LoggedOut
    }
}

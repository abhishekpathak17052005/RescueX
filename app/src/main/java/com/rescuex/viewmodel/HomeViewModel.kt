package com.rescuex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuex.data.model.Incident
import com.rescuex.data.model.User
import com.rescuex.data.repository.AuthRepository
import com.rescuex.data.repository.IncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _recentIncident = MutableStateFlow<Incident?>(null)
    val recentIncident: StateFlow<Incident?> = _recentIncident

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect {
                _user.value = it
                if (it != null) {
                    loadRecentIncident(it.uid)
                }
            }
        }
    }

    private fun loadRecentIncident(userId: String) {
        viewModelScope.launch {
            val incidents = incidentRepository.getIncidents()
            _recentIncident.value = incidents.find { it.patientId == userId }
        }
    }
}

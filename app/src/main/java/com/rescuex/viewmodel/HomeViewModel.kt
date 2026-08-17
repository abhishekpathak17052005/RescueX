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
        loadData()
    }

    private fun loadData() {
        _user.value = authRepository.getCurrentUser()
        _recentIncident.value = incidentRepository.getIncidents().firstOrNull()
    }
}

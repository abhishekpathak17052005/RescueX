package com.rescuex.viewmodel

import androidx.lifecycle.ViewModel
import com.rescuex.data.model.Incident
import com.rescuex.data.repository.IncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ResponderViewModel(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _activeIncidents = MutableStateFlow<List<Incident>>(emptyList())
    val activeIncidents: StateFlow<List<Incident>> = _activeIncidents

    init {
        // Filter for demo: all incidents with non-resolved status
        _activeIncidents.value = incidentRepository.getIncidents().filter { it.status != com.rescuex.data.model.IncidentStatus.RESOLVED }
    }
}

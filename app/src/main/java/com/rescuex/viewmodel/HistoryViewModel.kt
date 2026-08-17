package com.rescuex.viewmodel

import androidx.lifecycle.ViewModel
import com.rescuex.data.model.Incident
import com.rescuex.data.repository.IncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HistoryViewModel(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents

    init {
        _incidents.value = incidentRepository.getIncidents()
    }
}

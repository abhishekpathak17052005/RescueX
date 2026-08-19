package com.rescuex.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuex.data.model.Incident
import com.rescuex.data.repository.IncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HospitalViewModel(
    private val incidentRepository: IncidentRepository,
    private val hospitalId: String
) : ViewModel() {

    private val _incomingIncidents = MutableStateFlow<List<Incident>>(emptyList())
    val incomingIncidents: StateFlow<List<Incident>> = _incomingIncidents

    init {
        viewModelScope.launch {
            incidentRepository.incidentUpdates.collect {
                refreshIncidents()
            }
        }
        refreshIncidents()
    }

    private fun refreshIncidents() {
        _incomingIncidents.value = incidentRepository.getIncidents().filter { 
            it.selectedHospital?.id == hospitalId && it.status != com.rescuex.data.model.IncidentStatus.RESOLVED
        }
    }

    fun markSpecialistReady(incidentId: String) {
        Log.i("Hospital", "Specialist ready for incident: $incidentId")
    }
}

package com.rescuex.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuex.data.model.AmbulanceStatus
import com.rescuex.data.model.Incident
import com.rescuex.data.model.IncidentStatus
import com.rescuex.data.repository.IncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ResponderViewModel(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _assignedIncidents = MutableStateFlow<List<Incident>>(emptyList())
    val assignedIncidents: StateFlow<List<Incident>> = _assignedIncidents

    init {
        viewModelScope.launch {
            incidentRepository.incidentUpdates.collect {
                Log.d("ResponderVM", "Received incident update, refreshing list")
                refreshIncidents()
            }
        }
        refreshIncidents()
    }

    private fun refreshIncidents() {
        val all = incidentRepository.getIncidents()
        val filtered = all.filter { 
            it.status != IncidentStatus.RESOLVED && it.ambulance != null
        }
        Log.d("ResponderVM", "Refreshing Incidents. Total: ${all.size}, Filtered: ${filtered.size}")
        _assignedIncidents.value = filtered
    }

    fun updateAmbulanceStatus(incidentId: String, status: AmbulanceStatus) {
        val incident = incidentRepository.getIncidentById(incidentId) ?: return
        val currentAmbulance = incident.ambulance ?: return
        
        val updatedAmbulance = currentAmbulance.copy(status = status)
        val updatedIncident = incident.copy(
            ambulance = updatedAmbulance,
            status = when(status) {
                AmbulanceStatus.DISPATCHED -> IncidentStatus.RESPONDER_ASSIGNED
                AmbulanceStatus.EN_ROUTE -> IncidentStatus.RESPONDER_EN_ROUTE
                AmbulanceStatus.ARRIVED -> IncidentStatus.RESPONDER_ARRIVED
                else -> incident.status
            }
        )
        incidentRepository.updateIncident(updatedIncident)
    }
}

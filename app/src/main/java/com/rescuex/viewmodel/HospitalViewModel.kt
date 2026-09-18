package com.rescuex.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuex.data.model.Hospital
import com.rescuex.data.model.Incident
import com.rescuex.data.model.IncidentStatus
import com.rescuex.data.repository.HospitalRepository
import com.rescuex.data.repository.IncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class HospitalViewModel(
    private val incidentRepository: IncidentRepository,
    private val hospitalRepository: HospitalRepository
) : ViewModel() {
    private val TAG = "Hospital"

    private val _incomingIncidents = MutableStateFlow<List<Incident>>(emptyList())
    val incomingIncidents: StateFlow<List<Incident>> = _incomingIncidents

    private var currentHospitalId: String? = null

    fun initialize(hospitalId: String) {
        currentHospitalId = hospitalId
        viewModelScope.launch {
            // Register hospital as active
            hospitalRepository.registerHospital(
                Hospital(
                    hospitalId = hospitalId,
                    name = "Active Hospital Console",
                    status = "ACTIVE",
                    updatedAt = Date()
                )
            )

            incidentRepository.listenToIncidents(hospitalId = hospitalId).collect { incidents ->
                val active = incidents.filter { it.status != IncidentStatus.RESOLVED }
                Log.i(TAG, "[HOSPITAL] Incoming incident count: ${active.size}")
                _incomingIncidents.value = active
            }
        }
    }

    fun updateHospitalResponse(incidentId: String, accept: Boolean) {
        viewModelScope.launch {
            val incident = _incomingIncidents.value.find { it.incidentId == incidentId } ?: return@launch
            
            if (accept) {
                Log.i(TAG, "[HOSPITAL] Patient accepted: $incidentId")
                incidentRepository.updateIncident(incident.copy(hospitalStatus = "ACCEPTED", updatedAt = Date()))
            } else {
                Log.i(TAG, "[HOSPITAL] Patient rejected: $incidentId")
                incidentRepository.updateIncident(incident.copy(hospitalStatus = "REJECTED", updatedAt = Date()))
            }
        }
    }
}

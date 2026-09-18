package com.rescuex.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuex.data.model.Ambulance
import com.rescuex.data.model.AmbulanceStatus
import com.rescuex.data.model.Incident
import com.rescuex.data.model.IncidentStatus
import com.rescuex.data.repository.AmbulanceRepository
import com.rescuex.data.repository.AuthRepository
import com.rescuex.data.repository.IncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class ResponderViewModel(
    private val incidentRepository: IncidentRepository,
    private val ambulanceRepository: AmbulanceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val TAG = "AMBULANCE DEBUG"

    private val _assignedIncidents = MutableStateFlow<List<Incident>>(emptyList())
    val assignedIncidents: StateFlow<List<Incident>> = _assignedIncidents

    private val _ambulanceProfile = MutableStateFlow<Ambulance?>(null)
    val ambulanceProfile: StateFlow<Ambulance?> = _ambulanceProfile

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError

    private var currentAmbulanceId: String? = null

    fun initialize(uid: String) {
        Log.i(TAG, "[AMBULANCE DEBUG] authUid=$uid")
        
        viewModelScope.launch {
            val user = authRepository.currentUser.value
            Log.i(TAG, "[AMBULANCE DEBUG] userRole=${user?.role}")

            Log.i(TAG, "[AMBULANCE DEBUG] Searching ambulance by driverUid=$uid")
            val ambulance = ambulanceRepository.getAmbulanceByDriverUid(uid)
            
            if (ambulance != null) {
                Log.i(TAG, "[AMBULANCE DEBUG] Ambulance document found=true")
                Log.i(TAG, "[AMBULANCE DEBUG] ambulanceId=${ambulance.ambulanceId}")
                Log.i(TAG, "[AMBULANCE DEBUG] ambulance.driverUid=${ambulance.driverUid}")
                Log.i(TAG, "[AMBULANCE DEBUG] driverUidMatch=${ambulance.driverUid == uid}")
                
                currentAmbulanceId = ambulance.ambulanceId
                _ambulanceProfile.value = ambulance
                _profileError.value = null
                
                startListeningForIncidents(ambulance.ambulanceId)
            } else {
                Log.e(TAG, "[AMBULANCE DEBUG] Ambulance document found=false")
                _profileError.value = "Ambulance profile not configured."
                _ambulanceProfile.value = null
            }
        }
    }

    private fun startListeningForIncidents(ambulanceId: String) {
        viewModelScope.launch {
            Log.i("AMBULANCE REPO", "[AMBULANCE REPO] Listening for ambulanceId=$ambulanceId")
            incidentRepository.listenToIncidents(ambulanceId = ambulanceId).collect { incidents ->
                Log.i("AMBULANCE REPO", "[AMBULANCE REPO] Incident received. Count: ${incidents.size}")
                
                val filtered = incidents.filter { incident ->
                    val ambulanceIdMatches = incident.ambulanceId == ambulanceId
                    val statusActive = isStatusActive(incident.ambulanceStatus)
                    val included = ambulanceIdMatches && statusActive
                    
                    Log.d("AMBULANCE INCIDENT", "[AMBULANCE INCIDENT] incidentId=${incident.incidentId}")
                    Log.d("AMBULANCE INCIDENT", "[AMBULANCE INCIDENT] ambulanceId=${incident.ambulanceId}")
                    Log.d("AMBULANCE INCIDENT", "[AMBULANCE INCIDENT] currentAmbulanceId=$ambulanceId")
                    Log.d("AMBULANCE INCIDENT", "[AMBULANCE INCIDENT] status=${incident.ambulanceStatus}")
                    Log.d("AMBULANCE INCIDENT", "[AMBULANCE INCIDENT] included=$included")
                    
                    included
                }
                
                _assignedIncidents.value = filtered
            }
        }
    }

    private fun isStatusActive(status: AmbulanceStatus): Boolean {
        return status != AmbulanceStatus.NOT_REQUESTED && 
               status != AmbulanceStatus.COMPLETED && 
               status != AmbulanceStatus.REJECTED
    }

    fun updateAmbulanceStatus(incidentId: String, status: AmbulanceStatus) {
        viewModelScope.launch {
            val incident = _assignedIncidents.value.find { it.incidentId == incidentId } ?: return@launch
            
            Log.i(TAG, "[AMBULANCE] Status changed: $status for $incidentId")
            
            val updatedIncident = incident.copy(
                ambulanceStatus = status,
                status = when(status) {
                    AmbulanceStatus.ACCEPTED -> IncidentStatus.RESPONDER_ASSIGNED
                    AmbulanceStatus.EN_ROUTE_TO_PATIENT -> IncidentStatus.RESPONDER_EN_ROUTE
                    AmbulanceStatus.ARRIVED_AT_PATIENT -> IncidentStatus.RESPONDER_ARRIVED
                    AmbulanceStatus.COMPLETED -> IncidentStatus.RESOLVED
                    else -> incident.status
                },
                updatedAt = Date()
            )
            incidentRepository.updateIncident(updatedIncident)
            
            // Also sync back to ambulance record
            currentAmbulanceId?.let { 
                ambulanceRepository.updateAmbulanceStatus(it, status)
            }
        }
    }
}

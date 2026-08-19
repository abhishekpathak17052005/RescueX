package com.rescuex.ui.screens.responder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rescuex.data.model.AmbulanceStatus
import com.rescuex.data.model.Incident
import com.rescuex.ui.components.SeverityBadge
import com.rescuex.ui.navigation.Screen
import com.rescuex.ui.theme.RescueRed
import com.rescuex.ui.theme.SafetyGreen
import com.rescuex.viewmodel.ResponderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponderDashboard(
    navController: NavHostController,
    responderViewModel: ResponderViewModel
) {
    val assignedIncidents by responderViewModel.assignedIncidents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ambulance Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text(text = "Current Assignments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (assignedIncidents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active assignments.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(assignedIncidents) { incident ->
                        AmbulanceIncidentCard(
                            incident = incident,
                            onStatusUpdate = { status -> 
                                responderViewModel.updateAmbulanceStatus(incident.id, status)
                            },
                            onClick = {
                                navController.navigate(Screen.ResponderIncident.createRoute(incident.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AmbulanceIncidentCard(
    incident: Incident,
    onStatusUpdate: (AmbulanceStatus) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        border = if (incident.severity == com.rescuex.data.model.Severity.CRITICAL) 
            BorderStroke(2.dp, RescueRed) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = incident.type.name.replace("_", " "), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Patient: Demo User", style = MaterialTheme.typography.bodySmall)
                }
                SeverityBadge(incident.severity)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = incident.location ?: "Address pending...", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = "Status: ${incident.ambulance?.status?.name}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val currentStatus = incident.ambulance?.status
                
                when (currentStatus) {
                    AmbulanceStatus.DISPATCHED -> {
                        Button(onClick = { onStatusUpdate(AmbulanceStatus.EN_ROUTE) }, modifier = Modifier.weight(1f)) {
                            Text("Start Trip")
                        }
                    }
                    AmbulanceStatus.EN_ROUTE -> {
                        Button(
                            onClick = { onStatusUpdate(AmbulanceStatus.ARRIVED) }, 
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen)
                        ) {
                            Text("Arrived at Patient")
                        }
                    }
                    AmbulanceStatus.ARRIVED -> {
                        Button(onClick = { onStatusUpdate(AmbulanceStatus.COMPLETED) }, modifier = Modifier.weight(1f)) {
                            Text("Arrived at Hospital")
                        }
                    }
                    else -> {
                        OutlinedButton(onClick = onClick, modifier = Modifier.weight(1f)) {
                            Text("View Details")
                        }
                    }
                }
            }
        }
    }
}

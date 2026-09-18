package com.rescuex.ui.screens.responder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rescuex.data.model.AmbulanceStatus
import com.rescuex.data.model.Incident
import com.rescuex.ui.components.SeverityBadge
import com.rescuex.ui.theme.RescueRed
import com.rescuex.ui.theme.SafetyGreen
import com.rescuex.viewmodel.AuthViewModel
import com.rescuex.viewmodel.ResponderViewModel
import com.rescuex.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponderDashboard(
    navController: NavHostController,
    responderViewModel: ResponderViewModel,
    authViewModel: AuthViewModel
) {
    val assignedIncidents by responderViewModel.assignedIncidents.collectAsState()
    val ambulanceProfile by responderViewModel.ambulanceProfile.collectAsState()
    val profileError by responderViewModel.profileError.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ambulance Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { 
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (profileError != null) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = RescueRed, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = profileError!!, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Ambulance Status Header
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    AmbulanceStatusHeader(
                        ambulanceId = ambulanceProfile?.ambulanceId ?: "...",
                        driverName = "On Duty", // Mock or from user profile if available
                        tripCount = 0 // Mock for now
                    )
                }

                // Assignments Section
                item {
                    Text(
                        text = "Active Assignments",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (assignedIncidents.isEmpty()) {
                    item {
                        EmptyAssignmentsState()
                    }
                } else {
                    items(assignedIncidents) { incident ->
                        ActiveAssignmentCard(
                            incident = incident,
                            onStatusUpdate = { status -> 
                                responderViewModel.updateAmbulanceStatus(incident.incidentId, status)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AmbulanceStatusHeader(ambulanceId: String, driverName: String, tripCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color.White)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = driverName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "ID: $ambulanceId", style = MaterialTheme.typography.bodySmall)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "$tripCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = "Trips Today", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun ActiveAssignmentCard(
    incident: Incident,
    onStatusUpdate: (AmbulanceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Type & Severity
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(
                        text = incident.emergencyType.name.replace("_", " "),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = RescueRed
                    )
                    Text(text = "Incident #${incident.incidentId.takeLast(6).uppercase()}", style = MaterialTheme.typography.labelSmall)
                }
                SeverityBadge(incident.severity)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Patient Info
            InfoRow(icon = Icons.Default.Person, label = "Patient", value = incident.patientName)
            
            // Locations
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(icon = Icons.Default.LocationOn, label = "Pickup", value = incident.pickupAddress)
            
            if (incident.hospitalId != null) {
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(icon = Icons.Default.Business, label = "Destination", value = incident.hospitalName ?: "Unknown Hospital")
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(20.dp))
            
            // Status & ETA
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Assignment Status", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = incident.ambulanceStatus.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (incident.etaMinutes != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "ETA", style = MaterialTheme.typography.labelSmall)
                        Text(text = "${incident.etaMinutes} mins", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            LifecycleActionButtons(incident.ambulanceStatus, onStatusUpdate)
        }
    }
}

@Composable
fun LifecycleActionButtons(status: AmbulanceStatus, onStatusUpdate: (AmbulanceStatus) -> Unit) {
    val modifier = Modifier.fillMaxWidth().height(56.dp)
    
    when (status) {
        AmbulanceStatus.DISPATCHED -> {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onStatusUpdate(AmbulanceStatus.ACCEPTED) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen)
                ) {
                    Text("ACCEPT")
                }
                OutlinedButton(
                    onClick = { onStatusUpdate(AmbulanceStatus.REJECTED) },
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text("REJECT", color = RescueRed)
                }
            }
        }
        AmbulanceStatus.ACCEPTED -> {
            Button(onClick = { onStatusUpdate(AmbulanceStatus.EN_ROUTE_TO_PATIENT) }, modifier = modifier) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("EN ROUTE")
            }
        }
        AmbulanceStatus.EN_ROUTE_TO_PATIENT -> {
            Button(onClick = { onStatusUpdate(AmbulanceStatus.ARRIVED_AT_PATIENT) }, modifier = modifier) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ARRIVED AT PATIENT")
            }
        }
        AmbulanceStatus.ARRIVED_AT_PATIENT -> {
            Button(onClick = { onStatusUpdate(AmbulanceStatus.PATIENT_PICKED_UP) }, modifier = modifier) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("PATIENT PICKED UP")
            }
        }
        AmbulanceStatus.PATIENT_PICKED_UP -> {
            Button(onClick = { onStatusUpdate(AmbulanceStatus.EN_ROUTE_TO_HOSPITAL) }, modifier = modifier) {
                Icon(Icons.Default.LocalHospital, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("EN ROUTE TO HOSPITAL")
            }
        }
        AmbulanceStatus.EN_ROUTE_TO_HOSPITAL -> {
            Button(onClick = { onStatusUpdate(AmbulanceStatus.ARRIVED_AT_HOSPITAL) }, modifier = modifier) {
                Icon(Icons.Default.Business, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ARRIVED AT HOSPITAL")
            }
        }
        AmbulanceStatus.ARRIVED_AT_HOSPITAL -> {
            Button(
                onClick = { onStatusUpdate(AmbulanceStatus.COMPLETED) },
                modifier = modifier,
                colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("COMPLETE ASSIGNMENT")
            }
        }
        else -> {}
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun EmptyAssignmentsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.AssignmentTurnedIn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No active assignments.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = "You are currently marked as available.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

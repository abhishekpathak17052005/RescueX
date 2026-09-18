package com.rescuex.ui.screens.hospital

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rescuex.data.model.Incident
import com.rescuex.ui.components.SeverityBadge
import androidx.compose.material.icons.filled.Home
import com.rescuex.ui.navigation.Screen
import com.rescuex.ui.theme.RescueRed
import com.rescuex.ui.theme.SafetyGreen
import com.rescuex.viewmodel.AuthViewModel
import com.rescuex.viewmodel.HospitalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalDashboard(
    navController: NavHostController,
    hospitalViewModel: HospitalViewModel,
    authViewModel: AuthViewModel
) {
    val incomingIncidents by hospitalViewModel.incomingIncidents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hospital Emergency Console") },
                actions = {
                    IconButton(onClick = { 
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0)
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            // Stats Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Beds", "12", Modifier.weight(1f))
                StatCard("ICU", "3", Modifier.weight(1f))
                StatCard("Staff", "Active", Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "Incoming Emergencies", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (incomingIncidents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No incoming patients.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(incomingIncidents) { incident ->
                        IncomingIncidentCard(
                            incident = incident,
                            onAccept = { hospitalViewModel.updateHospitalResponse(incident.incidentId, true) },
                            onReject = { hospitalViewModel.updateHospitalResponse(incident.incidentId, false) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun IncomingIncidentCard(
    incident: Incident,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Patient: ${incident.patientName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Type: ${incident.emergencyType.name}", style = MaterialTheme.typography.bodySmall)
                }
                SeverityBadge(incident.severity)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = "Reported Symptoms:", style = MaterialTheme.typography.labelMedium)
            Text(text = incident.reportedSymptoms ?: "Initial report pending...", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Ambulance: ${incident.ambulanceId ?: "Assigning..."}", style = MaterialTheme.typography.bodySmall)
                Text(text = "ETA: ${incident.etaMinutes ?: "?"} mins", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            if (incident.hospitalStatus == "ACCEPTED") {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen)
                ) {
                    Text("ACCEPTED")
                }
            } else if (incident.hospitalStatus == "REJECTED") {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RescueRed)
                ) {
                    Text("REJECTED")
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAccept, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen)) {
                        Text("ACCEPT")
                    }
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = RescueRed)) {
                        Text("REJECT")
                    }
                }
            }
        }
    }
}

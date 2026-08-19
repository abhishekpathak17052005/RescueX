package com.rescuex.ui.screens.hospital

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Warning
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
import com.rescuex.ui.navigation.Screen
import com.rescuex.viewmodel.HospitalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalDashboard(
    navController: NavHostController,
    hospitalViewModel: HospitalViewModel
) {
    val incomingIncidents by hospitalViewModel.incomingIncidents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hospital ER Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text(text = "Incoming Patients", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (incomingIncidents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No incoming emergencies.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(incomingIncidents) { incident ->
                        HospitalIncidentCard(
                            incident = incident,
                            onSpecialistAction = { hospitalViewModel.markSpecialistReady(incident.id) },
                            onClick = {
                                navController.navigate(Screen.EmergencyDetails.createRoute(incident.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalIncidentCard(
    incident: Incident,
    onSpecialistAction: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = incident.id, style = MaterialTheme.typography.labelSmall)
                    Text(text = incident.type.name.replace("_", " "), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                SeverityBadge(incident.severity)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // AI Insights preview
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = incident.aiSummary ?: "Collecting information...", 
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ambulance ETA: ${incident.ambulance?.etaMinutes ?: "?"} min", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Button(onClick = onSpecialistAction, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("Prepare ER", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

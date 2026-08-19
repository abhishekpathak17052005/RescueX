package com.rescuex.ui.screens.emergency

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rescuex.data.repository.IncidentRepository
import com.rescuex.ui.components.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyDetailsScreen(
    navController: NavHostController,
    incidentId: String,
    incidentRepository: IncidentRepository
) {
    val incident = remember { incidentRepository.getIncidentById(incidentId) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Incident Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (incident == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Incident not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = incident.id, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    SeverityBadge(incident.severity)
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailItem("Emergency Type", incident.type.name.replace("_", " "))
                DetailItem("Status", incident.status.name.replace("_", " "))
                DetailItem("Time", dateFormat.format(incident.timestamp))
                DetailItem("Location", incident.location ?: "Unknown")

                incident.ambulance?.let { ambulance ->
                    Spacer(modifier = Modifier.height(16.dp))
                    AmbulanceStatusCard(ambulance)
                }

                incident.selectedHospital?.let { hospital ->
                    Spacer(modifier = Modifier.height(16.dp))
                    HospitalSelectionCard(hospital)
                }
                
                if (incident.structuredAiSummary != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    AIIncidentSummaryCard(incident.structuredAiSummary)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("AI Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    tonalElevation = 1.dp
                ) {
                    Text(
                        text = incident.aiSummary ?: "No summary available.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                DetailItem("Responder", "Assigned: City Emergency Services")
            }
        }
    }
}

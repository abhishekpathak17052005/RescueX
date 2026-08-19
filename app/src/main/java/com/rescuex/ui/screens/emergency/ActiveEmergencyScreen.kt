package com.rescuex.ui.screens.emergency

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rescuex.data.repository.AssistantState
import com.rescuex.ui.components.*
import com.rescuex.ui.navigation.Screen
import com.rescuex.viewmodel.EmergencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveEmergencyScreen(
    navController: NavHostController,
    emergencyViewModel: EmergencyViewModel
) {
    val activeIncident by emergencyViewModel.activeIncident.collectAsState()
    val elapsedTime by emergencyViewModel.elapsedTime.collectAsState()
    val location by emergencyViewModel.currentLocation.collectAsState()
    val assistantState by emergencyViewModel.assistantState.collectAsState()

    val minutes = elapsedTime / 60
    val seconds = elapsedTime % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Emergency Active", color = MaterialTheme.colorScheme.error)
                        Text(timeString, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Incident ID", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = activeIncident?.id ?: "RX-DEMO-001",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Status", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = activeIncident?.status?.name?.replace("_", " ") ?: "SOS Activated",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            activeIncident?.ambulance?.let { ambulance ->
                Spacer(modifier = Modifier.height(16.dp))
                AmbulanceStatusCard(ambulance)
            }

            activeIncident?.selectedHospital?.let { hospital ->
                Spacer(modifier = Modifier.height(16.dp))
                HospitalSelectionCard(hospital)
            }

            activeIncident?.structuredAiSummary?.let { summary ->
                Spacer(modifier = Modifier.height(24.dp))
                AIIncidentSummaryCard(summary)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Emergency Timeline", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            EmergencyTimeline(activeIncident?.status ?: com.rescuex.data.model.IncidentStatus.SOS_ACTIVATED)

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "AI Assistant", style = MaterialTheme.typography.titleLarge)
            Text(
                text = if (assistantState == AssistantState.ENDED) "Conversation complete. Summary generated." else "Ready to help collect emergency information.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate(Screen.AIAssistant.route) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                colors = if (assistantState == AssistantState.ENDED) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.buttonColors()
            ) {
                Icon(imageVector = Icons.Default.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (assistantState == AssistantState.ENDED) "View Assistant Again" else "Talk to AI Assistant")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Location", style = MaterialTheme.typography.titleLarge)
            Text(
                text = location?.address ?: "Fetching location...",
                style = MaterialTheme.typography.bodyMedium,
                color = if (location != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Emergency Type", style = MaterialTheme.typography.labelMedium)
                    Text(text = activeIncident?.type?.name?.replace("_", " ") ?: "Not specified", style = MaterialTheme.typography.bodyLarge)
                }
                Column {
                    Text(text = "Severity", style = MaterialTheme.typography.labelMedium)
                    SeverityBadge(activeIncident?.severity ?: com.rescuex.data.model.Severity.PENDING)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            TextButton(
                onClick = { 
                    emergencyViewModel.resolveEmergency()
                    navController.popBackStack(Screen.Home.route, false)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cancel / Resolve Emergency")
            }
        }
    }
}

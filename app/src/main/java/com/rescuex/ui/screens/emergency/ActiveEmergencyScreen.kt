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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rescuex.ui.components.EmergencyTimeline
import com.rescuex.ui.navigation.Screen
import com.rescuex.viewmodel.EmergencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveEmergencyScreen(
    navController: NavHostController,
    emergencyViewModel: EmergencyViewModel
) {
    val activeIncident by emergencyViewModel.activeIncident.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Emergency Active", color = MaterialTheme.colorScheme.error) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
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

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Emergency Timeline", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            EmergencyTimeline(activeIncident?.status ?: com.rescuex.data.model.IncidentStatus.SOS_ACTIVATED)

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "AI Assistant", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Ready to help collect emergency information.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate(Screen.AIAssistant.route) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Talk to AI Assistant")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Location", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Location will be available in the next phase.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Emergency Type", style = MaterialTheme.typography.labelMedium)
                    Text(text = activeIncident?.type?.name ?: "Not specified", style = MaterialTheme.typography.bodyLarge)
                }
                Column {
                    Text(text = "Severity", style = MaterialTheme.typography.labelMedium)
                    Text(text = activeIncident?.severity?.name ?: "Pending", style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            TextButton(
                onClick = { /* Demo Resolve */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cancel / Resolve Emergency")
            }
        }
    }
}

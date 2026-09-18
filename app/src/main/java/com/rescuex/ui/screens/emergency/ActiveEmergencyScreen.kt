package com.rescuex.ui.screens.emergency

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rescuex.data.model.AmbulanceStatus
import com.rescuex.data.model.IncidentStatus
import com.rescuex.data.repository.AssistantState
import com.rescuex.ui.components.*
import com.rescuex.ui.navigation.Screen
import com.rescuex.viewmodel.AiSessionState
import com.rescuex.viewmodel.EmergencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveEmergencyScreen(
    navController: NavHostController,
    incidentId: String,
    emergencyViewModel: EmergencyViewModel
) {
    val activeIncident by emergencyViewModel.activeIncident.collectAsState()
    val elapsedTime by emergencyViewModel.elapsedTime.collectAsState()
    val location by emergencyViewModel.currentLocation.collectAsState()
    val aiSessionState by emergencyViewModel.aiSessionState.collectAsState()
    val isActivating by emergencyViewModel.isActivating.collectAsState()
    val activationError by emergencyViewModel.activationError.collectAsState()

    LaunchedEffect(activeIncident?.incidentId, aiSessionState) {
        val id = activeIncident?.incidentId ?: "none"
        val label = when (aiSessionState) {
            AiSessionState.NOT_STARTED -> "Talk to AI Assistant"
            AiSessionState.CONNECTING -> "Connecting..."
            AiSessionState.ACTIVE -> "View Active AI Session"
            AiSessionState.COMPLETED -> "View Assistant Summary"
        }
        Log.i("AI BUTTON", "[AI BUTTON]\nincidentId=$id\nstate=$aiSessionState\nlabel=$label\nenabled=${activeIncident != null}")
    }

    LaunchedEffect(incidentId) {
        if (incidentId.isNotEmpty() && incidentId != "pending" && incidentId != "none") {
            emergencyViewModel.loadIncident(incidentId)
        }
    }

    val minutes = elapsedTime / 60
    val seconds = elapsedTime % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Emergency Active", color = MaterialTheme.colorScheme.error)
                        if (activeIncident != null) {
                            Text(timeString, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isActivating && activeIncident == null) {
                // Loading State
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Establishing Connection...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Please wait while we reach emergency services.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else if (activationError != null && activeIncident == null) {
                // Error State
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat, // Using chat as a placeholder for connection error
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Connection Failed",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activationError!!,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Return to Home")
                    }
                }
            } else {
                // Active Content State (Shown when activeIncident != null)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                                    text = activeIncident?.incidentId ?: "Initializing...",
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
                                    text = activeIncident?.status?.name?.replace("_", " ") ?: "Activating...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    if (activeIncident?.ambulanceStatus != null && activeIncident?.ambulanceStatus != AmbulanceStatus.NOT_REQUESTED) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AmbulanceStatusCard(
                            status = activeIncident!!.ambulanceStatus,
                            etaMinutes = activeIncident?.etaMinutes,
                            ambulanceId = activeIncident?.ambulanceId
                        )
                    }

                    if (activeIncident?.hospitalId != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HospitalSelectionCard(
                            name = activeIncident?.hospitalName ?: "Unknown Hospital",
                            address = activeIncident?.hospitalAddress ?: "..."
                        )
                    }

                    activeIncident?.structuredAiSummary?.let { summary ->
                        Spacer(modifier = Modifier.height(24.dp))
                        AIIncidentSummaryCard(summary)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Emergency Timeline", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    EmergencyTimeline(activeIncident?.status ?: IncidentStatus.SOS_ACTIVE)

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "AI Assistant", style = MaterialTheme.typography.titleLarge)
                    
                    val summary = activeIncident?.structuredAiSummary
                    val summaryValid = summary != null && 
                                       summary.incidentType != com.rescuex.data.model.EmergencyType.NOT_SPECIFIED && 
                                       summary.severity != com.rescuex.data.model.Severity.PENDING

                    Text(
                        text = when (aiSessionState) {
                            AiSessionState.NOT_STARTED -> "Ready to help collect emergency information."
                            AiSessionState.CONNECTING -> "Connecting to AI..."
                            AiSessionState.ACTIVE -> "AI Assistant is active. Please speak."
                            AiSessionState.COMPLETED -> if (summaryValid) "Conversation complete. Summary generated." else "AI session ended without enough emergency information."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (aiSessionState == AiSessionState.COMPLETED && !summaryValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Main Action Button
                    Button(
                        onClick = { 
                            val id = activeIncident?.incidentId ?: ""
                            if (id.isNotEmpty()) {
                                navController.navigate(Screen.AIAssistant.createRoute(id)) 
                            }
                        },
                        enabled = activeIncident != null,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        colors = if (aiSessionState == AiSessionState.COMPLETED) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.buttonColors()
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (aiSessionState) {
                                AiSessionState.NOT_STARTED -> "Talk to AI Assistant"
                                AiSessionState.CONNECTING -> "Connecting..."
                                AiSessionState.ACTIVE -> "View Active AI Session"
                                AiSessionState.COMPLETED -> if (summaryValid) "View Assistant Summary" else "Talk to AI Assistant Again"
                            }
                        )
                    }

                    // Secondary Action: Start New Conversation (Only if already completed once)
                    if (aiSessionState == AiSessionState.COMPLETED && summaryValid) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { 
                                val id = activeIncident?.incidentId ?: ""
                                if (id.isNotEmpty()) {
                                    emergencyViewModel.startNewAIAssistantSession()
                                    navController.navigate(Screen.AIAssistant.createRoute(id))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Text("Start New AI Conversation")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "Location", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = location?.address ?: activeIncident?.pickupAddress ?: "Fetching location...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Emergency Type", style = MaterialTheme.typography.labelMedium)
                            Text(text = activeIncident?.emergencyType?.name?.replace("_", " ") ?: "Not specified", style = MaterialTheme.typography.bodyLarge)
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
    }
}

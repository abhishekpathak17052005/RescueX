package com.rescuex.ui.screens.responder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rescuex.data.model.Incident
import com.rescuex.ui.components.SeverityBadge
import com.rescuex.ui.navigation.Screen
import com.rescuex.viewmodel.ResponderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponderDashboard(
    navController: NavHostController,
    responderViewModel: ResponderViewModel
) {
    val activeIncidents by responderViewModel.activeIncidents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Responder Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text(text = "Active Emergencies", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (activeIncidents.isEmpty()) {
                Text("No active emergencies.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(activeIncidents) { incident ->
                        ResponderIncidentCard(incident) {
                            navController.navigate(Screen.ResponderIncident.createRoute(incident.id))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResponderIncidentCard(incident: Incident, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = incident.type.name.replace("_", " "), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                SeverityBadge(incident.severity)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "2.4 km away", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Active for 03:24", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text("View Incident")
            }
        }
    }
}

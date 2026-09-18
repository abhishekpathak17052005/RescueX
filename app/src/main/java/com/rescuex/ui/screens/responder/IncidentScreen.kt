package com.rescuex.ui.screens.responder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rescuex.data.model.Incident
import com.rescuex.data.repository.IncidentRepository
import com.rescuex.ui.components.DetailItem
import com.rescuex.ui.components.SeverityBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentScreen(
    navController: NavHostController,
    incidentId: String,
    incidentRepository: IncidentRepository
) {
    var incident by remember { mutableStateOf<Incident?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(incidentId) {
        incident = incidentRepository.getIncidentById(incidentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Incident Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (incident == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Loading incident...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = incident!!.incidentId, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    SeverityBadge(incident!!.severity)
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailItem("Emergency Type", incident!!.emergencyType.name.replace("_", " "))
                DetailItem("User", incident!!.patientName)
                DetailItem("Phone", incident!!.patientPhone)
                DetailItem("Location", incident!!.pickupAddress)
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("AI Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    tonalElevation = 1.dp
                ) {
                    Text(
                        text = incident!!.reportedSymptoms ?: "No summary available.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                DetailItem("Ambulance Status", incident!!.ambulanceStatus.name)
                DetailItem("Hospital", incident!!.hospitalName ?: "Pending Assignment")

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

package com.rescuex.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rescuex.ui.components.IncidentCard
import com.rescuex.ui.components.RescueXBottomNavigation
import com.rescuex.ui.navigation.Screen
import com.rescuex.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavHostController,
    historyViewModel: HistoryViewModel
) {
    val incidents by historyViewModel.incidents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Emergency History", fontWeight = FontWeight.Bold) })
        },
        bottomBar = {
            RescueXBottomNavigation(navController)
        }
    ) { paddingValues ->
        if (incidents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No emergency activity yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(incidents) { incident ->
                    IncidentCard(incident) {
                        navController.navigate(Screen.EmergencyDetails.createRoute(incident.id))
                    }
                }
            }
        }
    }
}

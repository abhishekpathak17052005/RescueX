package com.rescuex.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.rescuex.data.model.UserRole
import com.rescuex.ui.components.*
import com.rescuex.ui.navigation.Screen
import com.rescuex.viewmodel.HomeViewModel
import com.rescuex.viewmodel.EmergencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    emergencyViewModel: EmergencyViewModel
) {
    val user by homeViewModel.user.collectAsState()
    val recentIncident by homeViewModel.recentIncident.collectAsState()
    val activeIncident by emergencyViewModel.activeIncident.collectAsState()
    val context = LocalContext.current
    var showRoleSwitch by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            emergencyViewModel.activateSOS()
            navController.navigate(Screen.EmergencyActive.route)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RescueX", fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = { showRoleSwitch = true }) {
                            Icon(imageVector = Icons.Default.SwitchAccount, contentDescription = "Switch Dashboard")
                        }
                        DropdownMenu(expanded = showRoleSwitch, onDismissRequest = { showRoleSwitch = false }) {
                            DropdownMenuItem(
                                text = { Text("Patient Dashboard") },
                                onClick = { showRoleSwitch = false /* Already here */ }
                            )
                            DropdownMenuItem(
                                text = { Text("Ambulance Dashboard") },
                                onClick = { 
                                    showRoleSwitch = false
                                    navController.navigate(Screen.ResponderDashboard.route) 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hospital Dashboard") },
                                onClick = { 
                                    showRoleSwitch = false
                                    navController.navigate("hospital_dashboard") 
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            RescueXBottomNavigation(navController)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Active Emergency Overview (If any)
            if (activeIncident != null) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        onClick = { navController.navigate(Screen.EmergencyActive.route) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Emergency, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Emergency Active", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Ambulance: ${activeIncident?.ambulance?.status?.name ?: "Pending"}", style = MaterialTheme.typography.bodySmall)
                            if (activeIncident?.selectedHospital != null) {
                                Text(text = "Hospital: ${activeIncident?.selectedHospital?.name}", style = MaterialTheme.typography.bodySmall)
                            }
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Are you safe?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Emergency assistance is one tap away.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(48.dp))
            }

            item {
                SOSButton(onConfirm = {
                    val permissions = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    val allGranted = permissions.all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }
                    if (allGranted) {
                        emergencyViewModel.activateSOS()
                        navController.navigate(Screen.EmergencyActive.route)
                    } else {
                        permissionLauncher.launch(permissions)
                    }
                })
                Spacer(modifier = Modifier.height(48.dp))
            }

            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        title = "AI Assistant",
                        icon = Icons.Default.Chat,
                        onClick = { navController.navigate(Screen.AIAssistant.route) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Share Location",
                        icon = Icons.Default.LocationOn,
                        onClick = { /* Demo share */ },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Contacts",
                        icon = Icons.Default.People,
                        onClick = { navController.navigate(Screen.EmergencyContacts.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                StatusCard(isSafe = activeIncident == null)
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (recentIncident != null) {
                    IncidentCard(recentIncident!!) {
                        navController.navigate(Screen.EmergencyDetails.createRoute(recentIncident!!.id))
                    }
                } else {
                    Text(
                        text = "No emergency activity yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Demo Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

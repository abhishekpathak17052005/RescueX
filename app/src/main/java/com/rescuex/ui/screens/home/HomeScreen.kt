package com.rescuex.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rescuex.ui.components.*
import com.rescuex.ui.navigation.Screen
import com.rescuex.viewmodel.HomeViewModel
import com.rescuex.viewmodel.EmergencyViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    emergencyViewModel: EmergencyViewModel
) {
    val user by homeViewModel.user.collectAsState()
    val recentIncident by homeViewModel.recentIncident.collectAsState()
    val context = LocalContext.current

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
                    IconButton(onClick = { navController.navigate(Screen.ResponderDashboard.route) }) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Responder")
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
                StatusCard(isSafe = true)
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

@Composable
fun RescueXBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = currentRoute == Screen.Home.route,
            onClick = { navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text("History") },
            selected = currentRoute == Screen.History.route,
            onClick = { navController.navigate(Screen.History.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Contacts, contentDescription = null) },
            label = { Text("Contacts") },
            selected = currentRoute == Screen.EmergencyContacts.route,
            onClick = { navController.navigate(Screen.EmergencyContacts.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile") },
            selected = currentRoute == Screen.Profile.route,
            onClick = { navController.navigate(Screen.Profile.route) }
        )
    }
}

package com.rescuex.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rescuex.RescueXApp
import com.rescuex.data.model.UserRole
import com.rescuex.ui.screens.home.HomeScreen
import com.rescuex.ui.screens.history.HistoryScreen
import com.rescuex.ui.screens.contacts.EmergencyContactsScreen
import com.rescuex.ui.screens.profile.ProfileScreen
import com.rescuex.ui.screens.profile.SettingsScreen
import com.rescuex.ui.screens.emergency.ActiveEmergencyScreen
import com.rescuex.ui.screens.emergency.AIAssistantScreen
import com.rescuex.ui.screens.emergency.EmergencyDetailsScreen
import com.rescuex.ui.screens.responder.ResponderDashboard
import com.rescuex.ui.screens.responder.IncidentScreen
import com.rescuex.ui.screens.hospital.HospitalDashboard
import com.rescuex.viewmodel.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object EmergencyActive : Screen("emergency_active/{incidentId}") {
        fun createRoute(incidentId: String) = "emergency_active/$incidentId"
    }
    object AIAssistant : Screen("ai_assistant/{incidentId}") {
        fun createRoute(incidentId: String) = "ai_assistant/$incidentId"
    }
    object EmergencyDetails : Screen("emergency_details/{incidentId}") {
        fun createRoute(incidentId: String) = "emergency_details/$incidentId"
    }
    object History : Screen("history")
    object EmergencyContacts : Screen("emergency_contacts")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object ResponderDashboard : Screen("responder_dashboard")
    object HospitalDashboard : Screen("hospital_dashboard")
    object AdminDashboard : Screen("admin_dashboard")
    object ResponderIncident : Screen("responder_incident/{incidentId}") {
        fun createRoute(incidentId: String) = "responder_incident/$incidentId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as RescueXApp
    val container = app.container
    val tag = "AUTH DEBUG"

    // Retrieve shared singletons from container
    val incidentRepo = container.incidentRepository
    val authRepo = container.authRepository
    val contactRepo = container.contactRepository
    val voiceAssistantRepo = container.voiceAssistantRepository
    val locationManager = container.locationManager
    val ambulanceRepo = container.ambulanceRepository
    val hospitalRepo = container.hospitalRepository

    // ViewModel Factories for proper instantiation
    val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(authRepo) as T
                modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(authRepo, incidentRepo) as T
                modelClass.isAssignableFrom(EmergencyViewModel::class.java) -> EmergencyViewModel(incidentRepo, locationManager, voiceAssistantRepo) as T
                modelClass.isAssignableFrom(HistoryViewModel::class.java) -> HistoryViewModel(incidentRepo) as T
                modelClass.isAssignableFrom(ContactsViewModel::class.java) -> ContactsViewModel(contactRepo) as T
                modelClass.isAssignableFrom(ResponderViewModel::class.java) -> ResponderViewModel(incidentRepo, ambulanceRepo, authRepo) as T
                modelClass.isAssignableFrom(HospitalViewModel::class.java) -> HospitalViewModel(incidentRepo, hospitalRepo) as T
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val emergencyViewModel: EmergencyViewModel = viewModel(factory = factory)
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)
    val contactsViewModel: ContactsViewModel = viewModel(factory = factory)
    val responderViewModel: ResponderViewModel = viewModel(factory = factory)
    val hospitalViewModel: HospitalViewModel = viewModel(factory = factory)

    val authState by authViewModel.state.collectAsState()

    // Loading overlay
    if (authState is AuthState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    // Critical Error overlay (Offline/Missing Profile)
    if (authState is AuthState.Error) {
        val message = (authState as AuthState.Error).message
        val isCritical = message.contains("internet") || message.contains("configured") || message.contains("denied")
        
        if (isCritical) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp), 
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = message, 
                        color = Color.Red, 
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.padding(12.dp))
                    Button(onClick = { navController.navigate(Screen.Home.route) { popUpTo(0) } }) {
                        Text("Return Home")
                    }
                }
            }
        }
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                Log.i(tag, "[AUTH DEBUG] Routing to ${state.role} dashboard")
                val destination = when (state.role) {
                    UserRole.PATIENT -> Screen.Home.route
                    UserRole.AMBULANCE -> {
                        responderViewModel.initialize(state.uid)
                        Screen.ResponderDashboard.route
                    }
                    UserRole.HOSPITAL -> {
                        hospitalViewModel.initialize(state.uid)
                        Screen.HospitalDashboard.route
                    }
                    UserRole.ADMIN -> Screen.AdminDashboard.route
                }
                if (destination != Screen.Home.route) {
                    navController.navigate(destination) {
                        launchSingleTop = true
                    }
                }
            }
            else -> {}
        }
    }

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController, homeViewModel, emergencyViewModel)
        }
        composable(Screen.AdminDashboard.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Admin Dashboard Placeholder")
            }
        }
        composable(Screen.EmergencyActive.route) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
            ActiveEmergencyScreen(navController, incidentId, emergencyViewModel)
        }
        composable(Screen.AIAssistant.route) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
            Log.i("INCIDENT FLOW", "[INCIDENT FLOW] Navigating to AI Assistant with incident: $incidentId")
            AIAssistantScreen(navController, incidentId, emergencyViewModel)
        }
        composable(Screen.EmergencyDetails.route) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
            EmergencyDetailsScreen(navController, incidentId, incidentRepo)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController, historyViewModel)
        }
        composable(Screen.EmergencyContacts.route) {
            EmergencyContactsScreen(navController, contactsViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController, authRepo)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
        composable(Screen.ResponderDashboard.route) {
            ResponderDashboard(navController, responderViewModel, authViewModel)
        }
        composable(Screen.HospitalDashboard.route) {
            HospitalDashboard(navController, hospitalViewModel, authViewModel)
        }
        composable(Screen.ResponderIncident.route) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
            IncidentScreen(navController, incidentId, incidentRepo)
        }
    }
}

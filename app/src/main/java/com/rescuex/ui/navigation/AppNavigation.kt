package com.rescuex.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rescuex.RescueXApp
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object EmergencyActive : Screen("emergency_active")
    object AIAssistant : Screen("ai_assistant")
    object EmergencyDetails : Screen("emergency_details/{incidentId}") {
        fun createRoute(incidentId: String) = "emergency_details/$incidentId"
    }
    object History : Screen("history")
    object EmergencyContacts : Screen("emergency_contacts")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object ResponderDashboard : Screen("responder_dashboard")
    object HospitalDashboard : Screen("hospital_dashboard")
    object ResponderIncident : Screen("responder_incident/{incidentId}") {
        fun createRoute(incidentId: String) = "responder_incident/$incidentId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as RescueXApp
    val container = app.container

    // Retrieve shared singletons from container
    val incidentRepo = container.incidentRepository
    val authRepo = container.authRepository
    val contactRepo = container.contactRepository
    val voiceAssistantRepo = container.voiceAssistantRepository
    val locationManager = container.locationManager

    // ViewModel Factories for proper instantiation
    val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(authRepo, incidentRepo) as T
                modelClass.isAssignableFrom(EmergencyViewModel::class.java) -> EmergencyViewModel(incidentRepo, locationManager, voiceAssistantRepo) as T
                modelClass.isAssignableFrom(HistoryViewModel::class.java) -> HistoryViewModel(incidentRepo) as T
                modelClass.isAssignableFrom(ContactsViewModel::class.java) -> ContactsViewModel(contactRepo) as T
                modelClass.isAssignableFrom(ResponderViewModel::class.java) -> ResponderViewModel(incidentRepo) as T
                modelClass.isAssignableFrom(HospitalViewModel::class.java) -> HospitalViewModel(incidentRepo, "H-001") as T
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val emergencyViewModel: EmergencyViewModel = viewModel(factory = factory)
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)
    val contactsViewModel: ContactsViewModel = viewModel(factory = factory)
    val responderViewModel: ResponderViewModel = viewModel(factory = factory)
    val hospitalViewModel: HospitalViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController, homeViewModel, emergencyViewModel)
        }
        composable(Screen.EmergencyActive.route) {
            ActiveEmergencyScreen(navController, emergencyViewModel)
        }
        composable(Screen.AIAssistant.route) {
            AIAssistantScreen(navController, emergencyViewModel)
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
            ResponderDashboard(navController, responderViewModel)
        }
        composable(Screen.HospitalDashboard.route) {
            HospitalDashboard(navController, hospitalViewModel)
        }
        composable(Screen.ResponderIncident.route) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
            IncidentScreen(navController, incidentId, incidentRepo)
        }
    }
}

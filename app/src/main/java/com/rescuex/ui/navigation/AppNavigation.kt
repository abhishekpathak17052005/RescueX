package com.rescuex.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rescuex.ui.screens.home.HomeScreen
import com.rescuex.ui.screens.history.HistoryScreen
import com.rescuex.ui.screens.contacts.EmergencyContactsScreen
import com.rescuex.ui.screens.profile.ProfileScreen
import com.rescuex.ui.screens.profile.SettingsScreen
import com.rescuex.ui.screens.emergency.EmergencyScreen
import com.rescuex.ui.screens.emergency.ActiveEmergencyScreen
import com.rescuex.ui.screens.emergency.AIAssistantScreen
import com.rescuex.ui.screens.emergency.EmergencyDetailsScreen
import com.rescuex.ui.screens.responder.ResponderDashboard
import com.rescuex.ui.screens.responder.IncidentScreen
import com.rescuex.viewmodel.HomeViewModel
import com.rescuex.viewmodel.EmergencyViewModel
import com.rescuex.viewmodel.HistoryViewModel
import com.rescuex.viewmodel.ContactsViewModel
import com.rescuex.viewmodel.ResponderViewModel
import com.rescuex.data.repository.MockAuthRepository
import com.rescuex.data.repository.MockIncidentRepository
import com.rescuex.data.repository.MockContactRepository

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
    object ResponderIncident : Screen("responder_incident/{incidentId}") {
        fun createRoute(incidentId: String) = "responder_incident/$incidentId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    // For now, create ViewModels here or use a simple DI/Factory
    val incidentRepo = MockIncidentRepository()
    val authRepo = MockAuthRepository()
    val contactRepo = MockContactRepository()

    val homeViewModel = HomeViewModel(authRepo, incidentRepo)
    val emergencyViewModel = EmergencyViewModel(incidentRepo)
    val historyViewModel = HistoryViewModel(incidentRepo)
    val contactsViewModel = ContactsViewModel(contactRepo)
    val responderViewModel = ResponderViewModel(incidentRepo)

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
        composable(Screen.ResponderIncident.route) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
            IncidentScreen(navController, incidentId, incidentRepo)
        }
    }
}

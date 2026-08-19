package com.rescuex.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rescuex.data.repository.AuthRepository
import com.rescuex.ui.components.RescueXBottomNavigation
import com.rescuex.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    authRepository: AuthRepository
) {
    val user = remember { authRepository.getCurrentUser() }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile", fontWeight = FontWeight.Bold) })
        },
        bottomBar = {
            RescueXBottomNavigation(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = user?.name ?: "Demo User", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = user?.email ?: "demo@rescuex.app", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(text = user?.phone ?: "+91 XXXXX XXXXX", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(32.dp))
            
            ProfileSection("Emergency Settings") {
                ProfileItem("Emergency Contacts", Icons.Default.People) { navController.navigate(Screen.EmergencyContacts.route) }
                ProfileItem("Privacy & Security", Icons.Default.Security) { }
            }
            
            ProfileSection("App Settings") {
                ProfileItem("Settings", Icons.Default.Settings) { navController.navigate(Screen.Settings.route) }
                ProfileItem("About RescueX", Icons.Default.Info) { }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { /* Demo Logout */ },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Log Out")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            tonalElevation = 1.dp
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun ProfileItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(imageVector = icon, contentDescription = null) },
        trailingContent = { Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null) },
        modifier = Modifier
            .background(Color.Transparent)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .clickable { onClick() }
    )
}

package com.rescuex.ui.screens.emergency

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rescuex.viewmodel.EmergencyViewModel
import com.rescuex.viewmodel.AIAssistantState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    navController: NavHostController,
    emergencyViewModel: EmergencyViewModel
) {
    val state by emergencyViewModel.aiAssistantState.collectAsState()
    
    // Auto-progress demo
    LaunchedEffect(state) {
        if (state == AIAssistantState.LISTENING) {
            delay(2000)
            emergencyViewModel.setAIAssistantState(AIAssistantState.PROCESSING)
            delay(1500)
            emergencyViewModel.setAIAssistantState(AIAssistantState.CONNECTED)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RescueX AI Assistant") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tell me what happened.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "I'll help organize the emergency information.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            VoiceUI(state)

            Spacer(modifier = Modifier.height(64.dp))

            if (state == AIAssistantState.IDLE) {
                Button(
                    onClick = { emergencyViewModel.startAIAssistant() },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text("Start Assistant")
                }
            } else {
                Text(
                    text = state.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text("Sample Questions:", style = MaterialTheme.typography.labelMedium)
                listOf(
                    "What happened?",
                    "Are you currently safe?",
                    "Are you injured?",
                    "Do you need medical assistance?"
                ).forEach { question ->
                    Text(
                        text = "• $question",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "AI-assisted information collection",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun VoiceUI(state: AIAssistantState) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        if (state == AIAssistantState.LISTENING || state == AIAssistantState.CONNECTED) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(if (state == AIAssistantState.LISTENING) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )
        }
        
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = if (state == AIAssistantState.CONNECTED) com.rescuex.ui.theme.SafetyGreen else MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (state == AIAssistantState.PROCESSING) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(48.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

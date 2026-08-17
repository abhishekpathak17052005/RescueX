package com.rescuex.ui.screens.emergency

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.MicOff
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
import androidx.navigation.NavHostController
import com.rescuex.viewmodel.EmergencyViewModel
import com.rescuex.data.repository.AssistantState
import com.rescuex.ui.theme.SafetyGreen
import com.rescuex.ui.theme.RescueRed

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    navController: NavHostController,
    emergencyViewModel: EmergencyViewModel
) {
    val state by emergencyViewModel.assistantState.collectAsState()
    val context = LocalContext.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            emergencyViewModel.startAIAssistant()
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
                text = "Emergency Information Assistant",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (state) {
                    AssistantState.READY -> "Ready to help collect information."
                    AssistantState.LISTENING -> "Listening to you..."
                    AssistantState.THINKING -> "Processing information..."
                    AssistantState.SPEAKING -> "AI is responding..."
                    AssistantState.CONNECTED -> "Conversation active."
                    AssistantState.ENDED -> "Conversation complete."
                    AssistantState.ERROR -> "Service unavailable."
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            VoiceVisualization(state)

            Spacer(modifier = Modifier.height(64.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state == AssistantState.READY || state == AssistantState.ENDED || state == AssistantState.ERROR) {
                    Button(
                        onClick = { 
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            if (hasPermission) {
                                emergencyViewModel.startAIAssistant()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(if (state == AssistantState.ENDED) "Restart Assistant" else "Start Assistant")
                    }
                } else {
                    IconButton(
                        onClick = { /* Demo Mute */ },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.MicOff, contentDescription = "Mute")
                    }
                    
                    Spacer(modifier = Modifier.width(32.dp))
                    
                    IconButton(
                        onClick = { emergencyViewModel.stopAIAssistant() },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(RescueRed)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End Conversation", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            if (state == AssistantState.READY) {
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text("RescueX will share:", style = MaterialTheme.typography.labelMedium)
                    Text("• Current Location", style = MaterialTheme.typography.bodySmall)
                    Text("• Incident Timestamp", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Demo Mode - No real emergency services notified",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun VoiceVisualization(state: AssistantState) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_pulse")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        val showPulse = state != AssistantState.READY && state != AssistantState.ENDED && state != AssistantState.ERROR
        
        if (showPulse) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) pulseScale else 1.1f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            )
        }
        
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = when (state) {
                AssistantState.CONNECTED, AssistantState.SPEAKING -> SafetyGreen
                AssistantState.ERROR -> RescueRed
                else -> MaterialTheme.colorScheme.primary
            },
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (state == AssistantState.THINKING) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(48.dp))
                } else {
                    Icon(
                        imageVector = if (state == AssistantState.ENDED) Icons.Default.CallEnd else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

package com.rescuex.ui.screens.emergency

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.rescuex.MainActivity
import com.rescuex.data.repository.AssistantState
import com.rescuex.ui.theme.RescueRed
import com.rescuex.ui.theme.SafetyGreen
import com.rescuex.viewmodel.EmergencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    navController: NavHostController,
    emergencyViewModel: EmergencyViewModel
) {
    val state by emergencyViewModel.assistantState.collectAsState()
    val isMuted by emergencyViewModel.isMuted.collectAsState()
    val activeIncident by emergencyViewModel.activeIncident.collectAsState()
    val context = LocalContext.current
    val TAG = "VapiAssistant"

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "[MIC DEBUG] Permission callback received: $isGranted")
        if (isGranted) {
            Log.d(TAG, "[MIC DEBUG] RECORD_AUDIO GRANTED")
            Log.d(TAG, "[VAPI DEBUG] Starting Vapi after microphone permission granted")
            emergencyViewModel.startAIAssistant()
        } else {
            Log.e(TAG, "[MIC DEBUG] RECORD_AUDIO DENIED")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RescueX AI Assistant") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
            if (activeIncident == null) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = RescueRed,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Active Emergency Found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your emergency session might have been lost. Please return to the Home screen and press SOS again.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { navController.navigate("home") }) {
                    Text("Return to Home")
                }
            } else {
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
                        AssistantState.CONNECTING -> "Connecting..."
                        AssistantState.CONNECTED -> "Conversation active."
                        AssistantState.LISTENING -> "Listening to you..."
                        AssistantState.THINKING -> "Processing information..."
                        AssistantState.SPEAKING -> "AI is responding..."
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
                                Log.d(TAG, "[MIC DEBUG] Start Assistant button clicked")
                                Log.d(TAG, "[MIC DEBUG] Checking RECORD_AUDIO permission")
                                
                                val permission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                )
                                
                                Log.d(TAG, "[MIC DEBUG] permission=$permission")
                                
                                if (permission == PackageManager.PERMISSION_GRANTED) {
                                    Log.d(TAG, "[MIC DEBUG] RECORD_AUDIO GRANTED")
                                    Log.d(TAG, "[VAPI DEBUG] Starting Vapi after microphone permission granted")
                                    emergencyViewModel.startAIAssistant()
                                } else {
                                    Log.d(TAG, "[MIC DEBUG] RECORD_AUDIO NOT GRANTED - requesting")
                                    
                                    val activity = context as? MainActivity
                                    if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {
                                        // This check isn't perfect for permanent denial but is a hint
                                        Log.d(TAG, "[MIC DEBUG] Requesting RECORD_AUDIO permission")
                                    }

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
                            onClick = { emergencyViewModel.toggleMute() },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) RescueRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, 
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = if (isMuted) RescueRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(32.dp))
                        
                        IconButton(
                            onClick = { emergencyViewModel.stopAIAssistant() },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(RescueRed)
                        ) {
                            Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Conversation", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
                
                if (state == AssistantState.READY) {
                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                        Text("RescueX will share:", style = MaterialTheme.typography.labelMedium)
                        Text("• Current Location", style = MaterialTheme.typography.bodySmall)
                        Text("• Incident ID: ${activeIncident?.id}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (state == AssistantState.READY || state == AssistantState.ERROR) "Demo Mode available" else "AI-assisted coordination",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
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
                AssistantState.CONNECTED, AssistantState.SPEAKING, AssistantState.LISTENING -> SafetyGreen
                AssistantState.ERROR -> RescueRed
                else -> MaterialTheme.colorScheme.primary
            },
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (state == AssistantState.THINKING || state == AssistantState.CONNECTING) {
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

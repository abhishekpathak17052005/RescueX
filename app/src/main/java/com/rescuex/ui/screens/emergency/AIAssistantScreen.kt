package com.rescuex.ui.screens.emergency

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.rescuex.data.model.EmergencyType
import com.rescuex.data.model.Severity
import com.rescuex.data.repository.AssistantState
import com.rescuex.ui.navigation.Screen
import com.rescuex.ui.theme.RescueRed
import com.rescuex.ui.theme.SafetyGreen
import com.rescuex.viewmodel.AiSessionState
import com.rescuex.viewmodel.EmergencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    navController: NavHostController,
    incidentId: String,
    emergencyViewModel: EmergencyViewModel
) {
    val state by emergencyViewModel.assistantState.collectAsState()
    val aiSessionState by emergencyViewModel.aiSessionState.collectAsState()
    val isMuted by emergencyViewModel.isMuted.collectAsState()
    val transcript by emergencyViewModel.transcript.collectAsState()
    val activeIncident by emergencyViewModel.activeIncident.collectAsState()
    val context = LocalContext.current
    val TAG = "VapiAssistant"

    var typedMessage by remember { mutableStateOf("") }

    LaunchedEffect(incidentId) {
        if (incidentId.isNotEmpty() && incidentId != "none") {
            Log.i("INCIDENT FLOW", "[INCIDENT FLOW] AI Assistant route with incident: $incidentId")
            emergencyViewModel.loadIncident(incidentId)
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "[MIC DEBUG] Permission callback received: $isGranted")
        if (isGranted) {
            Log.d(TAG, "[MIC DEBUG] RECORD_AUDIO GRANTED - starting assistant")
            emergencyViewModel.startAIAssistant()
        } else {
            Log.e(TAG, "[MIC DEBUG] RECORD_AUDIO DENIED")
        }
    }

    fun startAssistantSafely() {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            emergencyViewModel.startAIAssistant()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Attempt auto-start on screen launch if requested
    var hasAutoStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasAutoStarted && (aiSessionState == AiSessionState.NOT_STARTED || aiSessionState == AiSessionState.CONNECTING)) {
            hasAutoStarted = true
            val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            if (permission == PackageManager.PERMISSION_GRANTED) {
                emergencyViewModel.startAIAssistant()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RescueX AI Assistant", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (activeIncident == null) {
                Spacer(modifier = Modifier.height(48.dp))
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = RescueRed,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Active Emergency Found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your emergency session was not found. Return to Home and press SOS to begin.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { navController.navigate(Screen.Home.route) }) {
                    Text("Return to Home")
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AI Emergency Coordination",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (state) {
                        AssistantState.READY -> "Tap the microphone to begin emergency conversation"
                        AssistantState.CONNECTING -> "Connecting to AI emergency triage..."
                        AssistantState.CONNECTED -> "AI Connected. Speak or tap your answers below."
                        AssistantState.LISTENING -> "AI is listening to you... Speak now."
                        AssistantState.THINKING -> "AI is thinking & dispatching responders..."
                        AssistantState.SPEAKING -> "RescueX AI is speaking..."
                        AssistantState.ENDED -> "Emergency coordination complete"
                        AssistantState.ERROR -> "Emergency coordination active"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive Voice Visualization
                VoiceVisualization(
                    state = state,
                    onClick = {
                        if (state == AssistantState.READY || state == AssistantState.ENDED || state == AssistantState.ERROR) {
                            startAssistantSafely()
                        } else {
                            emergencyViewModel.stopAIAssistant()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (state == AssistantState.READY || state == AssistantState.ENDED) "Tap circle to speak" else "Tap circle to end call",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live Transcript / AI Output Card
                if (transcript.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (state == AssistantState.ENDED) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafetyGreen, modifier = Modifier.size(18.dp))
                                } else {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(SafetyGreen))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (state == AssistantState.ENDED) "Status: Complete" else "Live AI Dialogue",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = transcript,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                val isCallActive = state != AssistantState.READY && state != AssistantState.ENDED && state != AssistantState.ERROR

                // Live Response Chips while conversation is active
                if (isCallActive) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Quick Voice Answer (Tap to speak to AI):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { emergencyViewModel.sendUserMessage("Yes, please send an ambulance immediately!") },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RescueRed)
                                ) {
                                    Text("🚑 Yes, Send Ambulance", style = MaterialTheme.typography.labelSmall)
                                }
                                FilledTonalButton(
                                    onClick = { emergencyViewModel.sendUserMessage("I am having severe chest pain and pressure.") },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Text("❤️ Chest Pain", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { emergencyViewModel.sendUserMessage("There is a serious road accident with injuries.") },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Text("🚗 Car Crash", style = MaterialTheme.typography.labelSmall)
                                }
                                FilledTonalButton(
                                    onClick = { emergencyViewModel.sendUserMessage("The patient has severe difficulty breathing.") },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Text("🫁 Breathing Issue", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text input option so user can also type to AI
                    OutlinedTextField(
                        value = typedMessage,
                        onValueChange = { typedMessage = it },
                        placeholder = { Text("Or type answer to AI here...", style = MaterialTheme.typography.bodySmall) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (typedMessage.isNotBlank()) {
                                        emergencyViewModel.sendUserMessage(typedMessage.trim())
                                        typedMessage = ""
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (typedMessage.isNotBlank()) {
                                    emergencyViewModel.sendUserMessage(typedMessage.trim())
                                    typedMessage = ""
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Call control buttons (Mute & Hang Up)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { emergencyViewModel.toggleMute() },
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) RescueRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
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
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(RescueRed)
                        ) {
                            Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White)
                        }
                    }
                } else {
                    // Ready / Ended / Error controls
                    Button(
                        onClick = { startAssistantSafely() },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (state == AssistantState.ENDED) "Restart Conversation" else "Start Assistant")
                    }

                    if (state == AssistantState.ENDED || activeIncident?.ambulanceId != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FilledTonalButton(
                            onClick = {
                                activeIncident?.incidentId?.let { id ->
                                    navController.navigate(Screen.EmergencyActive.createRoute(id)) {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Text("View Emergency Status & Ambulance Tracking")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Emulator audio hint
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Emulator Audio Tip: Click '•••' on emulator sidebar > Microphone > turn ON 'Virtual microphone uses host audio input' to speak with your PC mic.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Emergency Triage Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Quick Emergency Triage",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to dispatch responders immediately without voice:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {
                                    emergencyViewModel.triggerQuickTriage(
                                        EmergencyType.MEDICAL,
                                        Severity.CRITICAL,
                                        "Cardiac arrest / severe chest pain reported."
                                    )
                                },
                                label = { Text("❤️ Cardiac") },
                                modifier = Modifier.weight(1f)
                            )
                            AssistChip(
                                onClick = {
                                    emergencyViewModel.triggerQuickTriage(
                                        EmergencyType.ACCIDENT,
                                        Severity.CRITICAL,
                                        "Major traffic collision with injuries."
                                    )
                                },
                                label = { Text("🚗 Accident") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {
                                    emergencyViewModel.triggerQuickTriage(
                                        EmergencyType.MEDICAL,
                                        Severity.HIGH,
                                        "Acute respiratory distress / breathing emergency."
                                    )
                                },
                                label = { Text("🫁 Breathing") },
                                modifier = Modifier.weight(1f)
                            )
                            AssistChip(
                                onClick = {
                                    emergencyViewModel.triggerQuickTriage(
                                        EmergencyType.MEDICAL,
                                        Severity.HIGH,
                                        "Severe bleeding and physical trauma."
                                    )
                                },
                                label = { Text("🩸 Trauma") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                activeIncident?.let { inc ->
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("Active Emergency Session:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("• Incident ID: ${inc.incidentId}", style = MaterialTheme.typography.bodySmall)
                        Text("• Location: ${inc.pickupAddress}", style = MaterialTheme.typography.bodySmall)
                        if (!inc.ambulanceId.isNullOrEmpty()) {
                            Text("• Dispatched Ambulance: ${inc.ambulanceId} (${inc.ambulanceStatus.name})", style = MaterialTheme.typography.bodySmall, color = SafetyGreen)
                        }
                        if (!inc.hospitalName.isNullOrEmpty()) {
                            Text("• Destination Hospital: ${inc.hospitalName} (${inc.hospitalStatus})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun VoiceVisualization(
    state: AssistantState,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_pulse")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val isActive = state == AssistantState.CONNECTING ||
            state == AssistantState.CONNECTED ||
            state == AssistantState.LISTENING ||
            state == AssistantState.THINKING ||
            state == AssistantState.SPEAKING

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(170.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 85.dp),
                onClick = onClick
            )
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(135.dp)
                    .scale(if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) pulseScale else 1.1f)
                    .clip(CircleShape)
                    .background(
                        if (state == AssistantState.SPEAKING || state == AssistantState.CONNECTED)
                            SafetyGreen.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
            )
        }
        
        Surface(
            modifier = Modifier.size(96.dp),
            shape = CircleShape,
            color = when (state) {
                AssistantState.CONNECTED, AssistantState.SPEAKING, AssistantState.LISTENING -> SafetyGreen
                AssistantState.ERROR -> RescueRed
                AssistantState.CONNECTING, AssistantState.THINKING -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.primary
            },
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (state == AssistantState.THINKING || state == AssistantState.CONNECTING) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(42.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (state == AssistantState.ENDED) Icons.Default.CallEnd else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        modifier = Modifier.size(44.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

package com.rescuex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rescuex.data.model.IncidentStatus
import com.rescuex.ui.theme.SafetyGreen

@Composable
fun EmergencyTimeline(currentStatus: IncidentStatus) {
    val steps = listOf(
        IncidentStatus.SOS_ACTIVATED to "SOS Activated",
        IncidentStatus.INFO_COLLECTION to "Information Collection",
        IncidentStatus.RESPONDER_ASSIGNED to "Responder Assignment",
        IncidentStatus.RESPONDER_EN_ROUTE to "Responder En Route",
        IncidentStatus.RESPONDER_ARRIVED to "Responder Arrived",
        IncidentStatus.RESOLVED to "Resolved"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, (status, label) ->
            val isActive = status == currentStatus
            val isCompleted = steps.indexOfFirst { it.first == currentStatus } > index
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                TimelineIcon(isCompleted, isActive)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isActive) MaterialTheme.colorScheme.primary else if (isCompleted) SafetyGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(start = 11.dp)
                        .width(2.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@Composable
fun TimelineIcon(isCompleted: Boolean, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                if (isCompleted) SafetyGreen 
                else if (isActive) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
        } else if (isActive) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

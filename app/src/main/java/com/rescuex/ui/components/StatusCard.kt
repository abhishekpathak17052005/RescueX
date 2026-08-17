package com.rescuex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rescuex.ui.theme.SafetyGreen

@Composable
fun StatusCard(isSafe: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isSafe) SafetyGreen else MaterialTheme.colorScheme.error)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Current Safety Status",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = if (isSafe) "You're currently safe" else "Emergency Active",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSafe) SafetyGreen else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

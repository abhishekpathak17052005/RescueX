package com.rescuex.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rescuex.data.model.Ambulance
import com.rescuex.data.model.Hospital
import com.rescuex.ui.theme.SafetyGreen

@Composable
fun AmbulanceStatusCard(ambulance: Ambulance) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Emergency, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Ambulance: ${ambulance.status.name}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = "ETA: ${ambulance.etaMinutes ?: "?"} minutes", style = MaterialTheme.typography.bodyMedium)
                Text(text = "ID: ${ambulance.id}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun HospitalSelectionCard(hospital: Hospital) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Receiving Hospital", style = MaterialTheme.typography.labelSmall)
                Text(text = hospital.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = hospital.address, style = MaterialTheme.typography.bodySmall)
                Text(text = "Verified for specialized care", style = MaterialTheme.typography.labelSmall, color = SafetyGreen)
            }
        }
    }
}

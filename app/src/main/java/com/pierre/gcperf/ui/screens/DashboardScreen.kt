package com.pierre.gcperf.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pierre.gcperf.core.HookLog
import com.pierre.gcperf.core.ProfileDb

@Composable
fun DashboardScreen() {
    val profiles = ProfileDb.all()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StatusCard() }
        item { StatsCard(profiles.size) }
    }
}

@Composable
fun StatusCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.CheckCircle, null,
                    tint = MaterialTheme.colorScheme.primary)
                Text("GCPerf Active", style = MaterialTheme.typography.titleMedium)
            }
            Text("LSposed module loaded. Hooks installed in android, " +
                "GameCenter and game processes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StatsCard(profileCount: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem(Icons.Filled.Bolt, "Profiles", "$profileCount")
            StatItem(Icons.Filled.ThermostatAuto, "Thermal", "Bypass")
            StatItem(Icons.Filled.DisplaySettings, "Display", "Unlocked")
            StatItem(Icons.Filled.Speed, "SR", "Override")
        }
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector,
             label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

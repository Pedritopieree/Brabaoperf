package com.pierre.gcperf.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pierre.gcperf.core.GameProfile
import com.pierre.gcperf.core.ProfileDb

@Composable
fun AppListScreen() {
    val profiles = ProfileDb.all()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(profiles) { p -> GameProfileCard(p) }
    }
}

@Composable
fun GameProfileCard(p: GameProfile) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val tierIcon = when (p.tier) {
                GameProfile.Tier.BOOST  -> Icons.Filled.Bolt
                GameProfile.Tier.POWER  -> Icons.Filled.ElectricBolt
                GameProfile.Tier.SR_ONLY -> Icons.Filled.AutoAwesome
            }
            val tierColor = when (p.tier) {
                GameProfile.Tier.BOOST  -> MaterialTheme.colorScheme.primary
                GameProfile.Tier.POWER  -> MaterialTheme.colorScheme.secondary
                GameProfile.Tier.SR_ONLY -> MaterialTheme.colorScheme.tertiary
            }
            Icon(tierIcon, null, tint = tierColor)
            Column(Modifier.weight(1f)) {
                Text(p.packageName.substringAfterLast('.'),
                     style = MaterialTheme.typography.bodyMedium)
                Text(p.packageName,
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (p.gameFps > 0) Text("${p.gameFps}fps",
                    style = MaterialTheme.typography.labelLarge,
                    color = tierColor)
                if (p.srEnabled) Text("SR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

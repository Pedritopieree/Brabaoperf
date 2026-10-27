package com.pierre.gcperf.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pierre.gcperf.core.HookLog
import kotlinx.coroutines.delay

@Composable
fun LogScreen() {
    var lines by remember { mutableStateOf(emptyList<String>()) }
    LaunchedEffect(Unit) {
        while (true) {
            lines = HookLog.readLines(300)
            delay(2000)
        }
    }
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Text("Hook Log", style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(lines.reversed()) { line ->
                val color = when {
                    line.contains("[E ") -> MaterialTheme.colorScheme.error
                    line.contains("[W ") -> MaterialTheme.colorScheme.tertiary
                    line.contains("[I ") -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Text(line, fontSize = 10.sp, color = color,
                    modifier = Modifier.padding(vertical = 1.dp))
            }
        }
    }
}

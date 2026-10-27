package com.pierre.gcperf.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary   = Color(0xFF80CBC4),
    secondary = Color(0xFF4DB6AC),
    background = Color(0xFF0D1117),
    surface   = Color(0xFF161B22),
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = Color(0xFF21262D),
)

@Composable
fun GCPerfTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = DarkColors, content = content)

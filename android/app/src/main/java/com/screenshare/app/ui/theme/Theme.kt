package com.screenshare.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D4FF),
    onPrimary = Color(0xFF003E4D),
    primaryContainer = Color(0xFF005466),
    onPrimaryContainer = Color(0xFFB8EAFF),
    
    secondary = Color(0xFF4ADE80),
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF005328),
    onSecondaryContainer = Color(0xFF6DF798),
    
    tertiary = Color(0xFFFFB800),
    onTertiary = Color(0xFF422C00),
    tertiaryContainer = Color(0xFF5F4100),
    onTertiaryContainer = Color(0xFFFFDDB3),
    
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFE1E3E5),
    
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFE1E3E5),
    
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFF8B949E),
    
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006A7A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB8EAFF),
    onPrimaryContainer = Color(0xFF001F27),
    
    secondary = Color(0xFF006D34),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF6DF798),
    onSecondaryContainer = Color(0xFF00210D),
    
    background = Color(0xFFFAFCFF),
    onBackground = Color(0xFF191C1E),
    
    surface = Color(0xFFFAFCFF),
    onSurface = Color(0xFF191C1E),
    
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41484D),
    
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun ScreenShareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

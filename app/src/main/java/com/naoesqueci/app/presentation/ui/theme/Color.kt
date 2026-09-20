package com.naoesqueci.app.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light colors
private val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF1B5E20),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF5D4037),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7CCC8),
    onSecondaryContainer = Color(0xFF3E2723),
    tertiary = Color(0xFF00695C),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2DFDB),
    onTertiaryContainer = Color(0xFF004D40),
    error = Color(0xFFC62828),
    onError = Color.White,
    errorContainer = Color(0xFFFCEEEE),
    onErrorContainer = Color(0xFFC62828),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1D1D1D),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1D1D1D),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF494949),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFC5C5C5),
    surfaceContainerHighest = Color(0xFFEEEEEE),
    surfaceContainerHigh = Color(0xFFF5F5F5),
    surfaceContainer = Color(0xFFFAFAFA),
    surfaceContainerLow = Color.White,
    surfaceContainerLowest = Color.White,
)

// Dark colors
private val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFBCAAA4),
    onSecondary = Color(0xFF3E2723),
    secondaryContainer = Color(0xFF5D4037),
    onSecondaryContainer = Color(0xFFD7CCC8),
    tertiary = Color(0xFF80CBC4),
    onTertiary = Color(0xFF004D40),
    tertiaryContainer = Color(0xFF00695C),
    onTertiaryContainer = Color(0xFFB2DFDB),
    error = Color(0xFFEF5350),
    onError = Color(0xFF1D1D1D),
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Color(0xFFFCEEEE),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFC4C4C4),
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFF494949),
    surfaceContainerHighest = Color(0xFF1E1E1E),
    surfaceContainerHigh = Color(0xFF1E1E1E),
    surfaceContainer = Color(0xFF1E1E1E),
    surfaceContainerLow = Color(0xFF121212),
    surfaceContainerLowest = Color(0xFF121212),
)

@Composable
fun AppColorScheme(darkTheme: Boolean = false) =
    if (darkTheme) DarkColorScheme else LightColorScheme
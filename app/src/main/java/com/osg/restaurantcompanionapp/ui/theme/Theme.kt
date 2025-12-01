package com.osg.restaurantcompanionapp.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = PastelPrimary,
    onPrimary = Color.White,
    primaryContainer = SoftLavender,
    onPrimaryContainer = Color(0xFF2E1D3E),

    secondary = PastelSecondary,
    onSecondary = Color.White,
    secondaryContainer = PastelBlue,
    onSecondaryContainer = Color(0xFF1D1D3E),

    tertiary = PastelTertiary,
    onTertiary = Color.White,
    tertiaryContainer = LightPeach,
    onTertiaryContainer = Color(0xFF3E2D1D),

    background = SoftGray,
    onBackground = Color(0xFF1C1B1F),

    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = WarmGray,
    onSurfaceVariant = Color(0xFF49454F),

    surfaceTint = Color.Transparent,

    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),

    error = SoftRed,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFFFFEDEA),
    onErrorContainer = Color(0xFF410002),

    outline = Color(0xFFD4D4D8),
    outlineVariant = Color(0xFFE5E5EA),

    scrim = Color(0x1A000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = PastelSecondary,
    onPrimary = Color(0xFF2E1D3E),
    primaryContainer = DarkLavender,
    onPrimaryContainer = SoftLavender,

    secondary = PastelBlue,
    onSecondary = Color(0xFF1D1D3E),
    secondaryContainer = DarkBlue,
    onSecondaryContainer = PastelBlue,

    tertiary = PastelTertiary,
    onTertiary = Color(0xFF3E2D1D),
    tertiaryContainer = DarkPeach,
    onTertiaryContainer = LightPeach,

    background = DarkGray,
    onBackground = Color(0xFFE6E1E5),

    surface = MediumGray,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),

    surfaceTint = Color.Transparent,

    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF52525E),
    outlineVariant = Color(0xFF3A3A3C),

    scrim = Color(0x33000000)
)

@Composable
fun RestaurantCompanionAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        shapes = MinimalistShapes,
        content = content
    )
}
package com.mushind.mind.core.design

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.mushind.mind.domain.model.AccentPalette
import com.mushind.mind.domain.model.ThemeMode

fun resolveDarkTheme(mode: ThemeMode, systemDark: Boolean): Boolean = when (mode) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

internal fun appColorScheme(darkTheme: Boolean, palette: AccentPalette): ColorScheme {
    val accent = palette.colors()
    return if (darkTheme) {
        darkColorScheme(
            primary = accent.darkPrimary,
            onPrimary = accent.darkOnPrimary,
            primaryContainer = accent.darkContainer,
            onPrimaryContainer = accent.darkOnContainer,
            secondary = accent.darkPrimary,
            onSecondary = accent.darkOnPrimary,
            secondaryContainer = accent.darkContainer,
            onSecondaryContainer = accent.darkOnContainer,
            background = DarkBackground,
            onBackground = DarkText,
            surface = DarkSurface,
            onSurface = DarkText,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = DarkTextSecondary,
        )
    } else {
        lightColorScheme(
            primary = accent.lightPrimary,
            onPrimary = accent.lightOnPrimary,
            primaryContainer = accent.lightContainer,
            onPrimaryContainer = accent.lightOnContainer,
            secondary = accent.lightPrimary,
            onSecondary = accent.lightOnPrimary,
            secondaryContainer = accent.lightContainer,
            onSecondaryContainer = accent.lightOnContainer,
            background = LightBackground,
            onBackground = LightText,
            surface = LightSurface,
            onSurface = LightText,
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = LightTextSecondary,
        )
    }
}

@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentPalette: AccentPalette = AccentPalette.SAGE,
    content: @Composable () -> Unit,
) {
    val darkTheme = resolveDarkTheme(themeMode, isSystemInDarkTheme())
    val colorScheme = appColorScheme(darkTheme, accentPalette)
    val semanticColors = if (darkTheme) {
        AppColors(
            positive = Color(0xFFA7D9B0),
            onPositive = Color(0xFF12391D),
            warning = Color(0xFFF2C14E),
            onWarning = Color(0xFF3E2E00),
        )
    } else {
        AppColors(
            positive = Color(0xFF2E6840),
            onPositive = Color.White,
            warning = Color(0xFF765500),
            onWarning = Color.White,
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        view.context.findActivity()?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides semanticColors,
        LocalAppSpacing provides AppSpacing(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

@Composable
fun MindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) = AppTheme(
    themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
    content = content,
)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

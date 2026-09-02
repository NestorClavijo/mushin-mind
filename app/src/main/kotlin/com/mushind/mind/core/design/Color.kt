package com.mushind.mind.core.design

import androidx.compose.ui.graphics.Color
import com.mushind.mind.domain.model.AccentPalette

internal val LightBackground = Color(0xFFF8F8F5)
internal val LightSurface = Color(0xFFFFFFFF)
internal val LightSurfaceVariant = Color(0xFFECEDE8)
internal val LightText = Color(0xFF1B1C1A)
internal val LightTextSecondary = Color(0xFF5F625D)

internal val DarkBackground = Color(0xFF121411)
internal val DarkSurface = Color(0xFF1B1E1A)
internal val DarkSurfaceVariant = Color(0xFF292D28)
internal val DarkText = Color(0xFFE4E5E0)
internal val DarkTextSecondary = Color(0xFFC2C7C0)

internal data class AccentColors(
    val lightPrimary: Color,
    val lightOnPrimary: Color,
    val lightContainer: Color,
    val lightOnContainer: Color,
    val darkPrimary: Color,
    val darkOnPrimary: Color,
    val darkContainer: Color,
    val darkOnContainer: Color,
)

internal fun AccentPalette.colors(): AccentColors = when (this) {
    AccentPalette.SAGE -> AccentColors(
        Color(0xFF405E51), Color.White, Color(0xFFD6E8DC), Color(0xFF17392C),
        Color(0xFFA5CFB7), Color(0xFF103526), Color(0xFF294E3D), Color(0xFFC1EBD2),
    )
    AccentPalette.OCEAN -> AccentColors(
        Color(0xFF245C73), Color.White, Color(0xFFC6E7F5), Color(0xFF003546),
        Color(0xFF8DCAE6), Color(0xFF003544), Color(0xFF174C60), Color(0xFFB7E5FA),
    )
    AccentPalette.AMBER -> AccentColors(
        Color(0xFF765500), Color.White, Color(0xFFFFDEA2), Color(0xFF261900),
        Color(0xFFF2C14E), Color(0xFF3E2E00), Color(0xFF5A4300), Color(0xFFFFDEA2),
    )
    AccentPalette.PLUM -> AccentColors(
        Color(0xFF684C73), Color.White, Color(0xFFF1D8F5), Color(0xFF35203D),
        Color(0xFFD8B7E2), Color(0xFF3A2840), Color(0xFF513958), Color(0xFFF5D9FC),
    )
}

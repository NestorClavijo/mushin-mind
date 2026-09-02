package com.mushind.mind.core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppColors(
    val positive: Color,
    val onPositive: Color,
    val warning: Color,
    val onWarning: Color,
)

@Immutable
data class AppSpacing(
    val xSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val xLarge: Dp = 20.dp,
    val xxLarge: Dp = 28.dp,
)

object AppElevation {
    val level0 = 0.dp
    val level1 = 1.dp
    val level2 = 3.dp
    val level3 = 6.dp
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

internal val LocalAppColors = staticCompositionLocalOf {
    AppColors(Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified)
}
internal val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

val MaterialTheme.appColors: AppColors
    @Composable get() = LocalAppColors.current

val MaterialTheme.appSpacing: AppSpacing
    @Composable get() = LocalAppSpacing.current

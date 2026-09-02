package com.mushind.mind.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class AccentPalette {
    SAGE,
    OCEAN,
    AMBER,
    PLUM,
}

data class AppearanceSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentPalette: AccentPalette = AccentPalette.SAGE,
)

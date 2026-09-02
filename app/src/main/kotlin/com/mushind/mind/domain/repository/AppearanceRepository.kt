package com.mushind.mind.domain.repository

import com.mushind.mind.domain.model.AccentPalette
import com.mushind.mind.domain.model.AppearanceSettings
import com.mushind.mind.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppearanceRepository {
    val settings: Flow<AppearanceSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAccentPalette(palette: AccentPalette)
}

package com.mushind.mind.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mushind.mind.domain.model.AccentPalette
import com.mushind.mind.domain.model.AppearanceSettings
import com.mushind.mind.domain.model.ThemeMode
import com.mushind.mind.domain.repository.AppearanceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AppearancePreferences @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AppearanceRepository {
    override val settings: Flow<AppearanceSettings> = context.settingsDataStore.data.map { preferences ->
        AppearanceSettings(
            themeMode = preferences[THEME_MODE].toEnumOrDefault(ThemeMode.SYSTEM),
            accentPalette = preferences[ACCENT_PALETTE].toEnumOrDefault(AccentPalette.SAGE),
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[THEME_MODE] = mode.name }
    }

    override suspend fun setAccentPalette(palette: AccentPalette) {
        context.settingsDataStore.edit { it[ACCENT_PALETTE] = palette.name }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("appearance_theme_mode")
        val ACCENT_PALETTE = stringPreferencesKey("appearance_accent_palette")
    }
}

private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
    this?.let { stored -> enumValues<T>().firstOrNull { it.name == stored } } ?: default

package com.mushind.mind.data.preferences

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mushind.mind.domain.model.AccentPalette
import com.mushind.mind.domain.model.AppearanceSettings
import com.mushind.mind.domain.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppearancePreferencesTest {
    @Test
    fun themeAndAccentSurviveSubsequentReads() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = AppearancePreferences(context)

        preferences.setThemeMode(ThemeMode.DARK)
        preferences.setAccentPalette(AccentPalette.PLUM)

        assertEquals(
            AppearanceSettings(ThemeMode.DARK, AccentPalette.PLUM),
            preferences.settings.first(),
        )

        preferences.setThemeMode(ThemeMode.SYSTEM)
        preferences.setAccentPalette(AccentPalette.SAGE)
    }

    @Test
    fun reminderEnabledAndTimeSurviveSubsequentReads() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = ReminderPreferences(context)

        try {
            preferences.setTime(7, 45)
            preferences.setEnabled(true)

            assertEquals(ReminderSettings(enabled = true, hour = 7, minute = 45), preferences.settings.first())
        } finally {
            preferences.setEnabled(false)
            preferences.setTime(21, 30)
        }
    }
}

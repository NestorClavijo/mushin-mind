package com.mushind.mind.core.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.mushind.mind.domain.model.AccentPalette
import com.mushind.mind.domain.model.ThemeMode
import kotlin.math.max
import kotlin.math.min
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTest {
    @Test
    fun `theme mode resolves system light and dark explicitly`() {
        assertEquals(false, resolveDarkTheme(ThemeMode.SYSTEM, systemDark = false))
        assertEquals(true, resolveDarkTheme(ThemeMode.SYSTEM, systemDark = true))
        assertEquals(false, resolveDarkTheme(ThemeMode.LIGHT, systemDark = true))
        assertEquals(true, resolveDarkTheme(ThemeMode.DARK, systemDark = false))
    }

    @Test
    fun `every accent changes the primary color`() {
        val lightPrimaries = AccentPalette.entries.map { appColorScheme(false, it).primary }
        val darkPrimaries = AccentPalette.entries.map { appColorScheme(true, it).primary }

        assertEquals(AccentPalette.entries.size, lightPrimaries.distinct().size)
        assertEquals(AccentPalette.entries.size, darkPrimaries.distinct().size)
        assertNotEquals(lightPrimaries, darkPrimaries)
    }

    @Test
    fun `primary buttons keep readable contrast for every supported theme`() {
        AccentPalette.entries.forEach { accent ->
            listOf(false, true).forEach { dark ->
                val colors = appColorScheme(dark, accent)
                assertTrue(
                    "$accent dark=$dark contrast=${contrast(colors.primary, colors.onPrimary)}",
                    contrast(colors.primary, colors.onPrimary) >= 4.5,
                )
                assertTrue(contrast(colors.background, colors.onBackground) >= 7.0)
                assertTrue(contrast(colors.primaryContainer, colors.onPrimaryContainer) >= 4.5)
            }
        }
    }

    private fun contrast(first: Color, second: Color): Float {
        val firstLuminance = first.luminance()
        val secondLuminance = second.luminance()
        return (max(firstLuminance, secondLuminance) + 0.05f) /
            (min(firstLuminance, secondLuminance) + 0.05f)
    }
}

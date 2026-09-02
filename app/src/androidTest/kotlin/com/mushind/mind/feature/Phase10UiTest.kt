package com.mushind.mind.feature

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.mushind.mind.core.design.AppTheme
import com.mushind.mind.domain.model.AccentPalette
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.AppearanceSettings
import com.mushind.mind.domain.model.Challenge
import com.mushind.mind.domain.model.ChallengeQuestion
import com.mushind.mind.domain.model.ChallengeQuestionType
import com.mushind.mind.domain.model.InstalledApplication
import com.mushind.mind.domain.model.RestrictedApp
import com.mushind.mind.domain.model.Task
import com.mushind.mind.domain.model.ThemeMode
import com.mushind.mind.feature.apps.AppRuleEditor
import com.mushind.mind.feature.apps.ChallengeDialog
import com.mushind.mind.feature.apps.ProtectedChangeUiState
import com.mushind.mind.feature.home.CreateTaskDialog
import com.mushind.mind.feature.home.TodayTaskRow
import com.mushind.mind.feature.settings.AppearanceSettingsRows
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class Phase10UiTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun createTaskDialogReturnsValidatedInput() {
        var created: Pair<String, String>? = null
        compose.setContent { AppTheme { CreateTaskDialog({}, { title, reward -> created = title to reward }) } }

        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("Leer 20 minutos")
        compose.onAllNodes(hasSetTextAction())[1].performTextReplacement("30")
        compose.onNodeWithText("Crear tarea").performClick()

        compose.runOnIdle { assertEquals("Leer 20 minutos" to "30", created) }
    }

    @Test
    fun pendingTaskCanBeCompletedFromItsRow() {
        val task = Task(
            id = "task-1",
            planId = "plan-1",
            title = "Estudiar",
            rewardPoints = 20,
            plannedDate = LocalDate.of(2026, 9, 1),
            createdAt = Instant.parse("2026-09-01T10:00:00Z"),
        )
        var completedId: String? = null
        compose.setContent { AppTheme { TodayTaskRow(task, { completedId = it }, {}) } }

        compose.onNodeWithContentDescription("Completar Estudiar").performClick()

        compose.runOnIdle { assertEquals(task.id, completedId) }
    }

    @Test
    fun appRuleEditorSavesDefaultTemporaryRule() {
        var saved: Triple<AppRuleType, String, String>? = null
        val app = InstalledApplication("com.example.focus", "Focus", false, false)
        compose.setContent {
            AppTheme {
                AppRuleEditor(app, null, {}, {}, { type, cost, duration ->
                    saved = Triple(type, cost, duration)
                }, {})
            }
        }

        compose.onNodeWithText("Activar y guardar").performClick()

        compose.runOnIdle {
            assertEquals(Triple(AppRuleType.TEMPORARY_SESSION, "20", "15"), saved)
        }
    }

    @Test
    fun appearanceRowsExposeThemeAndAccentActions() {
        var themeClicked = false
        var accentClicked = false
        compose.setContent {
            AppTheme {
                AppearanceSettingsRows(
                    AppearanceSettings(ThemeMode.DARK, AccentPalette.PLUM),
                    { themeClicked = true },
                    { accentClicked = true },
                )
            }
        }

        compose.onNodeWithText("Oscuro").performClick()
        compose.onNodeWithText("Ciruela").performClick()

        compose.runOnIdle { assertTrue(themeClicked && accentClicked) }
    }

    @Test
    fun challengeAnswerIsForwarded() {
        val now = Instant.parse("2026-09-01T10:00:00Z")
        val restricted = RestrictedApp("com.example.focus", "Focus", true, false, null, now, now)
        val challenge = Challenge(
            "attempt-1",
            restricted.packageName,
            listOf(ChallengeQuestion("q1", ChallengeQuestionType.ARITHMETIC, "3 + 5", listOf(6, 7, 8), 8)),
            now,
            now,
            LocalDate.of(2026, 9, 2),
        )
        var answer: Int? = null
        compose.setContent {
            AppTheme {
                ChallengeDialog(
                    ProtectedChangeUiState.InProgress(restricted, false, null, challenge),
                    { answer = it },
                    {},
                )
            }
        }

        compose.onNodeWithText("8").performClick()

        compose.runOnIdle { assertEquals(8, answer) }
    }
}

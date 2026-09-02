package com.mushind.mind.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.mushind.mind.domain.model.EmergencyPenaltyMode
import com.mushind.mind.domain.model.EmergencyPolicy
import com.mushind.mind.domain.repository.EmergencyPolicyRepository

internal val Context.settingsDataStore by preferencesDataStore(name = "settings")

data class ReminderSettings(
    val enabled: Boolean = false,
    val hour: Int = 21,
    val minute: Int = 30,
)

@Singleton
class ReminderPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val settings: Flow<ReminderSettings> = context.settingsDataStore.data.map { preferences ->
        ReminderSettings(
            enabled = preferences[ENABLED] ?: false,
            hour = preferences[HOUR] ?: 21,
            minute = preferences[MINUTE] ?: 30,
        )
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[ENABLED] = enabled }
    }

    suspend fun setTime(hour: Int, minute: Int) {
        require(hour in 0..23 && minute in 0..59)
        context.settingsDataStore.edit {
            it[HOUR] = hour
            it[MINUTE] = minute
        }
    }

    private companion object {
        val ENABLED = booleanPreferencesKey("planning_reminder_enabled")
        val HOUR = intPreferencesKey("planning_reminder_hour")
        val MINUTE = intPreferencesKey("planning_reminder_minute")
    }
}

@Singleton
class EmergencyPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : EmergencyPolicyRepository {
    override val policy: Flow<EmergencyPolicy> = context.settingsDataStore.data.map { preferences ->
        EmergencyPolicy(
            durationMinutes = preferences[DURATION] ?: 10,
            penaltyMode = EmergencyPenaltyMode.valueOf(
                preferences[PENALTY_MODE] ?: EmergencyPenaltyMode.FIXED_POINTS.name,
            ),
            fixedPenaltyPoints = preferences[PENALTY_POINTS] ?: 20,
        )
    }

    override suspend fun setDurationMinutes(minutes: Int) {
        require(minutes in 1..60)
        context.settingsDataStore.edit { it[DURATION] = minutes }
    }

    override suspend fun setFixedPenalty(points: Int) {
        require(points in 0..500)
        context.settingsDataStore.edit {
            it[PENALTY_MODE] = if (points == 0) EmergencyPenaltyMode.NONE.name else EmergencyPenaltyMode.FIXED_POINTS.name
            it[PENALTY_POINTS] = points
        }
    }

    private companion object {
        val DURATION = intPreferencesKey("emergency_duration_minutes")
        val PENALTY_MODE = androidx.datastore.preferences.core.stringPreferencesKey("emergency_penalty_mode")
        val PENALTY_POINTS = intPreferencesKey("emergency_penalty_points")
    }
}

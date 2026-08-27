package com.mushind.mind.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushind.mind.data.preferences.ReminderPreferences
import com.mushind.mind.data.preferences.ReminderSettings
import com.mushind.mind.platform.notifications.PlanningReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: ReminderPreferences,
    private val scheduler: PlanningReminderScheduler,
) : ViewModel() {
    val reminderSettings = preferences.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReminderSettings(),
    )

    fun setPlanningReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setEnabled(enabled)
            scheduler.sync(reminderSettings.value.copy(enabled = enabled))
        }
    }
}

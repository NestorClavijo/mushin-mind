package com.mushind.mind.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushind.mind.data.preferences.ReminderPreferences
import com.mushind.mind.data.preferences.ReminderSettings
import com.mushind.mind.platform.notifications.PlanningReminderScheduler
import com.mushind.mind.platform.accessibility.ProtectionStatusMonitor
import com.mushind.mind.platform.debug.DebugPointSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: ReminderPreferences,
    private val scheduler: PlanningReminderScheduler,
    private val protectionStatusMonitor: ProtectionStatusMonitor,
    private val debugPointSeeder: DebugPointSeeder,
) : ViewModel() {
    private val _isProtectionEnabled = MutableStateFlow(false)
    val isProtectionEnabled = _isProtectionEnabled.asStateFlow()
    private val _debugBalance = MutableStateFlow<Int?>(null)
    val debugBalance = _debugBalance.asStateFlow()

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

    fun refreshProtectionStatus() {
        _isProtectionEnabled.value = protectionStatusMonitor.isEnabled()
    }

    fun addDebugPoints() {
        viewModelScope.launch { _debugBalance.value = debugPointSeeder.add() }
    }
}

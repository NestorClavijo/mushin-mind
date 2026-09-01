package com.mushind.mind.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushind.mind.data.preferences.ReminderPreferences
import com.mushind.mind.data.preferences.ReminderSettings
import com.mushind.mind.platform.notifications.PlanningReminderScheduler
import com.mushind.mind.platform.accessibility.ProtectionStatusMonitor
import com.mushind.mind.platform.debug.DebugPointSeeder
import com.mushind.mind.domain.repository.EmergencyPolicyRepository
import com.mushind.mind.domain.model.EmergencyPolicy
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
    private val emergencyPolicies: EmergencyPolicyRepository,
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
    val emergencyPolicy = emergencyPolicies.policy.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EmergencyPolicy(),
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

    fun cycleEmergencyDuration() {
        val options = listOf(5, 10, 15, 30)
        val current = emergencyPolicy.value.durationMinutes
        val next = options[(options.indexOf(current).takeIf { it >= 0 } ?: 0).plus(1) % options.size]
        viewModelScope.launch { emergencyPolicies.setDurationMinutes(next) }
    }

    fun cycleEmergencyPenalty() {
        val options = listOf(0, 10, 20, 30)
        val current = emergencyPolicy.value.fixedPenaltyPoints
        val next = options[(options.indexOf(current).takeIf { it >= 0 } ?: 0).plus(1) % options.size]
        viewModelScope.launch { emergencyPolicies.setFixedPenalty(next) }
    }
}

package com.mushind.mind.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.mushind.mind.R
import com.mushind.mind.BuildConfig
import com.mushind.mind.core.design.component.ScreenHeader
import com.mushind.mind.core.design.component.PermissionBanner
import com.mushind.mind.domain.model.AccentPalette
import com.mushind.mind.domain.model.ThemeMode
import com.mushind.mind.domain.model.AppearanceSettings

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val reminderSettings by viewModel.reminderSettings.collectAsStateWithLifecycle()
    val isProtectionEnabled by viewModel.isProtectionEnabled.collectAsStateWithLifecycle()
    val debugBalance by viewModel.debugBalance.collectAsStateWithLifecycle()
    val emergencyPolicy by viewModel.emergencyPolicy.collectAsStateWithLifecycle()
    val appearance by viewModel.appearance.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.setPlanningReminderEnabled(granted)
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshProtectionStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader(title = stringResource(R.string.settings_title))
        SettingsSection(stringResource(R.string.protection)) {
            if (isProtectionEnabled) {
                SettingsRow(
                    title = stringResource(R.string.protection),
                    value = stringResource(R.string.protection_enabled),
                    onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                )
            } else {
                PermissionBanner(
                    title = "Protección desactivada",
                    description = "Activa el servicio de accesibilidad para aplicar las reglas de acceso.",
                    actionLabel = "Revisar permiso",
                    onAction = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
        SettingsSection(stringResource(R.string.routine)) {
            SettingsSwitchRow(
                title = stringResource(R.string.planning_reminder),
                value = stringResource(R.string.planning_reminder_default),
                checked = reminderSettings.enabled,
                onCheckedChange = { enabled ->
                    val needsPermission =
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) != PackageManager.PERMISSION_GRANTED
                    if (enabled && needsPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setPlanningReminderEnabled(enabled)
                    }
                },
            )
        }
        SettingsSection(stringResource(R.string.appearance)) {
            AppearanceSettingsRows(
                appearance = appearance,
                onThemeClick = viewModel::cycleThemeMode,
                onAccentClick = viewModel::cycleAccentPalette,
            )
        }
        SettingsSection("Emergencia") {
            SettingsRow(
                title = "Duración del acceso",
                value = "${emergencyPolicy.durationMinutes} min",
                onClick = viewModel::cycleEmergencyDuration,
            )
            SettingsRow(
                title = "Penalización",
                value = if (emergencyPolicy.fixedPenaltyPoints == 0) "Sin penalización" else "${emergencyPolicy.fixedPenaltyPoints} pts",
                onClick = viewModel::cycleEmergencyPenalty,
            )
        }
        if (BuildConfig.DEBUG) {
            SettingsSection("Desarrollo") {
                SettingsRow(
                    title = "Añadir puntos de prueba",
                    value = debugBalance?.let { "Saldo: $it pts" } ?: "+40 pts",
                    onClick = viewModel::addDebugPoints,
                )
            }
        }
    }
}

@Composable
internal fun AppearanceSettingsRows(
    appearance: AppearanceSettings,
    onThemeClick: () -> Unit,
    onAccentClick: () -> Unit,
) {
    SettingsRow(
        title = stringResource(R.string.theme),
        value = appearance.themeMode.displayName(),
        onClick = onThemeClick,
    )
    SettingsRow(
        title = "Acento",
        value = appearance.accentPalette.displayName(),
        onClick = onAccentClick,
    )
}

private fun ThemeMode.displayName() = when (this) {
    ThemeMode.SYSTEM -> "Seguir sistema"
    ThemeMode.LIGHT -> "Claro"
    ThemeMode.DARK -> "Oscuro"
}

private fun AccentPalette.displayName() = when (this) {
    AccentPalette.SAGE -> "Salvia"
    AccentPalette.OCEAN -> "Océano"
    AccentPalette.AMBER -> "Ámbar"
    AccentPalette.PLUM -> "Ciruela"
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    value: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title)
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 8.dp),
    )
    content()
}

@Composable
private fun SettingsRow(title: String, value: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
}

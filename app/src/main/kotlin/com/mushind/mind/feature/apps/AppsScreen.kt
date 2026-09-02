package com.mushind.mind.feature.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushind.mind.core.design.component.EmptyState
import com.mushind.mind.core.design.component.ScreenHeader
import com.mushind.mind.core.design.component.AppRuleCard
import com.mushind.mind.core.design.component.PrimaryButton
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.InstalledApplication
import com.mushind.mind.domain.model.AppRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppsScreen(viewModel: AppsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            state.selectedApp?.let { app ->
                AppRuleEditor(
                    app,
                    state.pendingChanges[app.packageName],
                    { viewModel.select(null) },
                    viewModel::requestDisable,
                    viewModel::saveRule,
                    viewModel::cancelPendingChange,
                )
            } ?: AppCatalog(state, viewModel::setQuery, { viewModel.select(it.packageName) }, viewModel::refresh)
        }
    }
    when (val protected = state.protectedChange) {
        is ProtectedChangeUiState.Review -> ProtectedChangeReview(
            protected,
            viewModel::startChallenge,
            viewModel::abandonProtectedChange,
        )
        is ProtectedChangeUiState.InProgress -> ChallengeDialog(
            protected,
            viewModel::answerChallenge,
            viewModel::abandonProtectedChange,
        )
        is ProtectedChangeUiState.Completed -> ChallengeCompletedDialog(
            protected,
            viewModel::confirmProtectedChange,
            viewModel::abandonProtectedChange,
        )
        null -> Unit
    }
}

@Composable
private fun AppCatalog(
    state: AppsUiState,
    onQueryChange: (String) -> Unit,
    onSelect: (InstalledApplication) -> Unit,
    onRetry: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Aplicaciones")
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            placeholder = { Text("Buscar por nombre") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.apps.isEmpty() -> EmptyState(
                title = if (state.query.isBlank()) "No encontramos aplicaciones" else "Sin resultados",
                explanation = "Solo aparecen aplicaciones que se pueden abrir desde el dispositivo.",
                actionLabel = "Volver a cargar",
                onAction = onRetry,
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.apps, key = { it.packageName }) { app -> AppRow(app) { onSelect(app) } }
            }
        }
    }
}

@Composable
private fun AppRow(app: InstalledApplication, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            InstalledAppIcon(app.packageName)
            Column(Modifier.weight(1f)) {
                Text(app.displayName, fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        app.restriction?.isEnabled == true -> "Control activo"
                        app.isCritical -> "App sensible · revisa antes de restringir"
                        else -> "Sin control"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = app.restriction?.isEnabled == true, onCheckedChange = { onClick() })
        }
    }
}

@Composable
private fun AppRuleEditor(
    app: InstalledApplication,
    pendingChange: com.mushind.mind.domain.model.PendingRuleChange?,
    onBack: () -> Unit,
    onDisable: () -> Unit,
    onSave: (AppRuleType, String, String) -> Unit,
    onCancelPending: (String) -> Unit,
) {
    val current = app.restriction?.rule
    val isEnabled = app.restriction?.isEnabled == true
    var type by remember(app.packageName, current) {
        androidx.compose.runtime.mutableStateOf(current?.type ?: AppRuleType.TEMPORARY_SESSION)
    }
    var cost by remember(app.packageName, current) {
        androidx.compose.runtime.mutableStateOf((current?.costPoints ?: 20).toString())
    }
    var duration by remember(app.packageName, current) {
        androidx.compose.runtime.mutableStateOf((current?.durationMinutes ?: 15).toString())
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver") }
                InstalledAppIcon(app.packageName)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(app.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (app.isCritical) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        "Esta app puede ser importante para el sistema o la seguridad. Comprueba que conservarás las funciones esenciales.",
                        Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
        pendingChange?.let { pending ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Cambio pendiente", fontWeight = FontWeight.Bold)
                        Text("Se aplicará el ${pending.effectiveDay}: ${pendingSummary(pending.proposedEnabled, pending.proposedRule)}")
                        TextButton(onClick = { onCancelPending(pending.id) }) { Text("Cancelar cambio") }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Control de acceso", fontWeight = FontWeight.SemiBold)
                    Text(if (isEnabled) "Activo" else "Se activará al guardar")
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { enabled ->
                        if (!enabled && isEnabled) onDisable()
                        if (enabled && !isEnabled) onSave(type, cost, duration)
                    },
                )
            }
        }
        item {
            Text("Tipo de regla", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                RuleTypeChip("Sesión temporal", AppRuleType.TEMPORARY_SESSION, type) { type = it }
                RuleTypeChip("Hasta terminar el día", AppRuleType.UNTIL_END_OF_DAY, type) { type = it }
                RuleTypeChip("Tiempo acumulable", AppRuleType.PURCHASABLE_TIME, type) { type = it }
            }
        }
        item {
            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it.filter(Char::isDigit) },
                label = { Text("Costo en puntos (5–500)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        if (type != AppRuleType.UNTIL_END_OF_DAY) {
            item {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter(Char::isDigit) },
                    label = { Text(if (type == AppRuleType.PURCHASABLE_TIME) "Minutos por unidad (5–240)" else "Duración (5–240 min)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item { AppRuleCard("Resumen de la regla", ruleSummary(type, cost, duration)) }
        item {
            PrimaryButton(
                label = if (current == null) "Activar y guardar" else "Guardar cambios",
                onClick = { onSave(type, cost, duration) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ProtectedChangeReview(
    state: ProtectedChangeUiState.Review,
    onStart: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Este cambio facilita el acceso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Para evitar cambios por impulso, debes completar un reto. El cambio se aplicará mañana.")
                Text("Actual: ${pendingSummary(state.current.isEnabled, state.current.rule)}")
                Text("Nuevo: ${pendingSummary(state.proposedEnabled, state.proposedRule)}")
            }
        },
        confirmButton = { Button(onClick = onStart) { Text("Comenzar reto") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } },
    )
}

@Composable
private fun ChallengeDialog(
    state: ProtectedChangeUiState.InProgress,
    onAnswer: (Int) -> Unit,
    onAbandon: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false),
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier.fillMaxSize().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Reto de concentración", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                val total = state.challenge.questions.size
                val completed = state.questionIndex.coerceAtMost(total)
                Text("$completed / $total")
                LinearProgressIndicator(
                    progress = { completed.toFloat() / total },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                )
                val question = state.challenge.questions.getOrNull(state.questionIndex)
                if (question != null && !state.waitingForMinimumTime) {
                    Text(question.prompt, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(18.dp))
                    question.options.forEach { option ->
                        Button(
                            onClick = { onAnswer(option) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        ) { Text(option.toString()) }
                    }
                    if (state.mistakes > 0) {
                        Text("Errores: ${state.mistakes}. El progreso retrocede al fallar.")
                    }
                } else {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Mantén la pausa hasta completar el tiempo mínimo.")
                }
                Spacer(Modifier.height(20.dp))
                TextButton(onClick = onAbandon) { Text("Abandonar reto") }
            }
        }
    }
}

@Composable
private fun ChallengeCompletedDialog(
    state: ProtectedChangeUiState.Completed,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false),
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier.fillMaxSize().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Reto completado", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text("El cambio quedará programado para ${state.challenge.effectiveDay}.")
                Text(pendingSummary(state.proposedEnabled, state.proposedRule), modifier = Modifier.padding(16.dp))
                Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) { Text("Confirmar cambio") }
                TextButton(onClick = onCancel) { Text("Cancelar") }
            }
        }
    }
}

private fun pendingSummary(enabled: Boolean, rule: AppRule?): String = when {
    !enabled -> "Control desactivado"
    rule == null -> "Sin regla"
    rule.type == AppRuleType.UNTIL_END_OF_DAY -> "${rule.costPoints} pts hasta terminar el día"
    else -> "${rule.costPoints} pts por ${rule.durationMinutes} min"
}

@Composable
private fun RuleTypeChip(
    label: String,
    value: AppRuleType,
    selected: AppRuleType,
    onSelect: (AppRuleType) -> Unit,
) {
    FilterChip(
        selected = selected == value,
        onClick = { onSelect(value) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun ruleSummary(type: AppRuleType, cost: String, duration: String): String = when (type) {
    AppRuleType.TEMPORARY_SESSION -> "Resumen: pagarás $cost puntos para acceder durante $duration minutos."
    AppRuleType.UNTIL_END_OF_DAY -> "Resumen: pagarás $cost puntos para acceder hasta el cierre del día lógico."
    AppRuleType.PURCHASABLE_TIME -> "Resumen: cada $cost puntos añadirán $duration minutos de acceso acumulable."
}

@Composable
private fun InstalledAppIcon(packageName: String) {
    val packageManager = LocalContext.current.packageManager
    val bitmap by produceState<ImageBitmap?>(null, packageName) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                packageManager.getApplicationIcon(packageName).toBitmap(96, 96).asImageBitmap()
            }.getOrNull()
        }
    }
    bitmap?.let { Image(it, null, modifier = Modifier.size(44.dp)) }
        ?: Icon(Icons.Outlined.Apps, null, modifier = Modifier.size(44.dp))
}

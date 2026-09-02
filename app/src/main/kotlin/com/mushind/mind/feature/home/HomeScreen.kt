package com.mushind.mind.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushind.mind.R
import com.mushind.mind.core.design.component.EmptyState
import com.mushind.mind.core.design.component.PrimaryButton
import com.mushind.mind.core.design.component.SecondaryButton
import com.mushind.mind.core.design.component.TaskRow
import com.mushind.mind.domain.model.DailyPlan
import com.mushind.mind.domain.model.DailyPlanStatus
import com.mushind.mind.domain.model.Task
import com.mushind.mind.domain.model.TaskStatus

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.home_greeting),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(20.dp))
            BalanceCard(balance = (uiState as? HomeUiState.Content)?.progress?.balance ?: 0)
            Spacer(Modifier.height(28.dp))
            when (val state = uiState) {
                HomeUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                HomeUiState.Error -> EmptyState(
                    title = stringResource(R.string.daily_cycle_error),
                    explanation = stringResource(R.string.daily_cycle_error),
                    actionLabel = stringResource(R.string.retry),
                    onAction = viewModel::reconcile,
                )
                is HomeUiState.Content -> DailyPlanContent(
                    state = state,
                    onPrepareTomorrow = viewModel::prepareTomorrow,
                    onConfirmTomorrow = viewModel::confirmTomorrow,
                    onCreateTomorrowTask = viewModel::createTomorrowTask,
                    onCompleteTask = viewModel::completeTask,
                    onSkipTask = viewModel::skipTask,
                )
            }
        }
    }
}

@Composable
private fun DailyPlanContent(
    state: HomeUiState.Content,
    onPrepareTomorrow: () -> Unit,
    onConfirmTomorrow: () -> Unit,
    onCreateTomorrowTask: (String, String) -> Unit,
    onCompleteTask: (String) -> Unit,
    onSkipTask: (String) -> Unit,
) {
    var showTaskDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.today_plan), style = MaterialTheme.typography.titleLarge)
        state.todayPlan?.let { PlanStatusLabel(it.status) }
    }
    if (state.todayTasks.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.no_tasks_today),
            explanation = stringResource(R.string.no_tasks_explanation),
        )
    } else {
        state.todayTasks.forEach { task -> TodayTaskRow(task, onCompleteTask, onSkipTask) }
        Spacer(Modifier.height(20.dp))
    }

    val tomorrowPlan = state.tomorrowPlan
    if (tomorrowPlan == null) {
        PrimaryButton(
            label = stringResource(R.string.plan_tomorrow),
            onClick = onPrepareTomorrow,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        TomorrowPlanCard(
            plan = tomorrowPlan,
            tasks = state.tomorrowTasks,
            onConfirm = onConfirmTomorrow,
            onAddTask = { showTaskDialog = true },
        )
    }
    if (showTaskDialog) {
        CreateTaskDialog(
            onDismiss = { showTaskDialog = false },
            onCreate = { title, reward ->
                onCreateTomorrowTask(title, reward)
                showTaskDialog = false
            },
        )
    }
}

@Composable
private fun TomorrowPlanCard(
    plan: DailyPlan,
    tasks: List<Task>,
    onConfirm: () -> Unit,
    onAddTask: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.tomorrow_plan), style = MaterialTheme.typography.titleMedium)
            Text(
                text = plan.date.toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PlanStatusLabel(plan.status)
            tasks.forEach { task ->
                TaskRow(
                    title = task.title,
                    status = "${task.rewardPoints} puntos",
                )
            }
            if (plan.status == DailyPlanStatus.DRAFT) {
                SecondaryButton(
                    label = "Añadir tarea",
                    onClick = onAddTask,
                    modifier = Modifier.fillMaxWidth(),
                )
                SecondaryButton(
                    label = stringResource(R.string.confirm_plan),
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = tasks.isNotEmpty(),
                )
            } else if (plan.status == DailyPlanStatus.CONFIRMED) {
                Text(stringResource(R.string.plan_ready), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
internal fun TodayTaskRow(
    task: Task,
    onComplete: (String) -> Unit,
    onSkip: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = task.status == TaskStatus.COMPLETED,
                    onCheckedChange = { checked -> if (checked) onComplete(task.id) },
                    enabled = task.status == TaskStatus.PENDING,
                    modifier = Modifier.semantics {
                        contentDescription = "Completar ${task.title}"
                    },
                )
                Column(Modifier.weight(1f)) {
                    Text(task.title, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${task.rewardPoints} puntos · ${task.status.displayName()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (task.status == TaskStatus.PENDING) {
                    TextButton(onClick = { onSkip(task.id) }) { Text("Omitir") }
                }
            }
        }
    }
}

@Composable
internal fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var reward by remember { mutableStateOf("20") }
    val rewardValue = reward.toIntOrNull()
    val valid = title.isNotBlank() && rewardValue != null && rewardValue in Task.MIN_REWARD..Task.MAX_REWARD
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva tarea para mañana") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = reward,
                    onValueChange = { reward = it.filter(Char::isDigit) },
                    label = { Text("Recompensa (5–100 puntos)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(title, reward) }, enabled = valid) { Text("Crear tarea") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

private fun TaskStatus.displayName() = when (this) {
    TaskStatus.PENDING -> "Pendiente"
    TaskStatus.COMPLETED -> "Completada"
    TaskStatus.SKIPPED -> "Omitida"
    TaskStatus.CANCELLED -> "Cancelada"
}

@Composable
private fun PlanStatusLabel(status: DailyPlanStatus) {
    val label = when (status) {
        DailyPlanStatus.DRAFT -> R.string.plan_draft
        DailyPlanStatus.CONFIRMED -> R.string.plan_confirmed
        DailyPlanStatus.ACTIVE -> R.string.plan_active
        DailyPlanStatus.CLOSED -> R.string.plan_closed
    }
    Text(
        text = stringResource(label),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun BalanceCard(balance: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = balance.toString(), style = MaterialTheme.typography.displaySmall)
            Text(
                text = stringResource(R.string.available_balance),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.today_balance_activity),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

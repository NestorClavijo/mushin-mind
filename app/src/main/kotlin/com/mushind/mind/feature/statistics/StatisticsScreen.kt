package com.mushind.mind.feature.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushind.mind.R
import com.mushind.mind.core.design.component.EmptyState
import com.mushind.mind.core.design.component.ScreenHeader
import com.mushind.mind.domain.model.AppStatistics
import com.mushind.mind.domain.model.DailyStatistics
import com.mushind.mind.domain.model.EmergencyUnlock
import com.mushind.mind.domain.model.PeriodStatistics
import com.mushind.mind.domain.model.PointTransaction
import com.mushind.mind.domain.model.PointTransactionType
import com.mushind.mind.domain.model.StatisticsDashboard
import com.mushind.mind.domain.model.Task
import com.mushind.mind.domain.model.TaskStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.statistics_title))
        when (val current = state) {
            StatisticsUiState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 48.dp),
            )
            StatisticsUiState.Error -> EmptyState(
                title = "No pudimos cargar las estadísticas",
                explanation = "Tus datos siguen guardados. Vuelve a intentarlo más tarde.",
            )
            is StatisticsUiState.Ready -> Dashboard(current.dashboard)
        }
    }
}

@Composable
private fun Dashboard(dashboard: StatisticsDashboard) {
    val hasActivity = dashboard.week.plannedTasks > 0 || dashboard.transactions.isNotEmpty() ||
        dashboard.emergencyUnlocks.isNotEmpty()
    if (!hasActivity) {
        EmptyState(
            title = stringResource(R.string.statistics_empty),
            explanation = stringResource(R.string.statistics_empty_explanation),
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { TodayCard(dashboard.today.totals) }
        item { WeekCard(dashboard.week, dashboard.days) }
        if (dashboard.apps.isNotEmpty()) {
            item { SectionTitle("Aplicaciones esta semana") }
            items(dashboard.apps.take(8), key = AppStatistics::packageName) { AppRow(it) }
        }
        if (dashboard.transactions.isNotEmpty()) {
            item { SectionTitle("Transacciones recientes") }
            items(dashboard.transactions.take(10), key = PointTransaction::id) { TransactionRow(it) }
        }
        if (dashboard.tasks.isNotEmpty()) {
            item { SectionTitle("Historial de tareas") }
            items(dashboard.tasks.take(10), key = Task::id) { TaskRow(it) }
        }
        if (dashboard.emergencyUnlocks.isNotEmpty()) {
            item { SectionTitle("Emergencias") }
            items(dashboard.emergencyUnlocks.take(10), key = EmergencyUnlock::id) { EmergencyRow(it) }
        }
    }
}

@Composable
private fun TodayCard(totals: PeriodStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Hoy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "${totals.completedTasks}/${totals.plannedTasks} tareas · ${totals.completionPercent}%",
                style = MaterialTheme.typography.titleMedium,
            )
            MetricRow("Puntos ganados", "+${totals.pointsEarned} pts")
            MetricRow("Puntos gastados", "-${totals.pointsSpent} pts")
            MetricRow("Neto", signed(totals.netPoints))
            MetricRow("Tiempo comprado", "${totals.purchasedMinutes} min")
            if (totals.emergencies > 0) MetricRow("Emergencias", totals.emergencies.toString())
        }
    }
}

@Composable
private fun WeekCard(totals: PeriodStatistics, days: List<DailyStatistics>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Esta semana", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "${totals.completedTasks}/${totals.plannedTasks} tareas · ${totals.completionPercent}% · ${signed(totals.netPoints)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            days.forEach { day ->
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(day.day.format(DAY_FORMATTER).replaceFirstChar(Char::titlecase))
                    Text("${day.totals.completedTasks}/${day.totals.plannedTasks} · ${signed(day.totals.netPoints)}")
                }
            }
            if (totals.emergencies > 0) MetricRow("Emergencias", totals.emergencies.toString())
        }
    }
}

@Composable
private fun AppRow(app: AppStatistics) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(app.displayName, fontWeight = FontWeight.SemiBold)
                Text(
                    "${app.unlocks} accesos · ${app.purchasedMinutes} min · ${app.emergencies} emergencias",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text("-${app.pointsSpent} pts")
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun TransactionRow(transaction: PointTransaction) {
    HistoryRow(
        title = transaction.description,
        detail = "${transaction.type.displayName()} · ${formatInstant(transaction.createdAt)}",
        value = signed(transaction.amount),
    )
}

@Composable
private fun TaskRow(task: Task) {
    HistoryRow(
        title = task.title,
        detail = "${task.status.displayName()} · ${task.plannedDate}",
        value = if (task.status == TaskStatus.COMPLETED && task.generatesPoints) "+${task.rewardPoints} pts" else "",
    )
}

@Composable
private fun EmergencyRow(event: EmergencyUnlock) {
    HistoryRow(
        title = event.packageName,
        detail = "${event.durationMinutes} min · ${event.reason ?: "Sin motivo"} · ${formatInstant(event.createdAt)}",
        value = "-${event.appliedPenaltyPoints} pts",
    )
}

@Composable
private fun HistoryRow(title: String, detail: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (value.isNotEmpty()) Text(value)
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

private fun signed(points: Int) = if (points >= 0) "+$points pts" else "$points pts"

private fun PointTransactionType.displayName() = when (this) {
    PointTransactionType.TASK_REWARD -> "Tarea"
    PointTransactionType.APP_UNLOCK -> "Acceso"
    PointTransactionType.EMERGENCY_PENALTY -> "Emergencia"
    PointTransactionType.CORRECTION -> "Ajuste"
}

private fun TaskStatus.displayName() = when (this) {
    TaskStatus.PENDING -> "Pendiente"
    TaskStatus.COMPLETED -> "Completada"
    TaskStatus.SKIPPED -> "Omitida"
    TaskStatus.CANCELLED -> "Cancelada"
}

private fun formatInstant(instant: Instant): String =
    instant.atZone(ZoneId.systemDefault()).format(TIME_FORMATTER)

private val DAY_FORMATTER = DateTimeFormatter.ofPattern("EEE d", Locale.forLanguageTag("es"))
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.forLanguageTag("es"))

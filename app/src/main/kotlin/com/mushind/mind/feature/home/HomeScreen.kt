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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushind.mind.R
import com.mushind.mind.core.design.component.EmptyState
import com.mushind.mind.core.design.component.PrimaryButton
import com.mushind.mind.core.design.component.SecondaryButton
import com.mushind.mind.domain.model.DailyPlan
import com.mushind.mind.domain.model.DailyPlanStatus

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.home_greeting),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(20.dp))
        BalanceCard(balance = 0)
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
            )
        }
    }
}

@Composable
private fun DailyPlanContent(
    state: HomeUiState.Content,
    onPrepareTomorrow: () -> Unit,
    onConfirmTomorrow: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.today_plan), style = MaterialTheme.typography.titleLarge)
        state.todayPlan?.let { PlanStatusLabel(it.status) }
    }
    EmptyState(
        title = stringResource(R.string.no_tasks_today),
        explanation = stringResource(R.string.no_tasks_explanation),
    )

    val tomorrowPlan = state.tomorrowPlan
    if (tomorrowPlan == null) {
        PrimaryButton(
            label = stringResource(R.string.plan_tomorrow),
            onClick = onPrepareTomorrow,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        TomorrowPlanCard(tomorrowPlan, onConfirmTomorrow)
    }
}

@Composable
private fun TomorrowPlanCard(plan: DailyPlan, onConfirm: () -> Unit) {
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
            if (plan.status == DailyPlanStatus.DRAFT) {
                SecondaryButton(
                    label = stringResource(R.string.confirm_plan),
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else if (plan.status == DailyPlanStatus.CONFIRMED) {
                Text(stringResource(R.string.plan_ready), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
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

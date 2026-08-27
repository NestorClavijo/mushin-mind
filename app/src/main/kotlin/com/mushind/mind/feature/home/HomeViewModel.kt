package com.mushind.mind.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.DailyPlan
import com.mushind.mind.domain.model.LogicalDayResolver
import com.mushind.mind.domain.repository.DailyPlanRepository
import com.mushind.mind.domain.usecase.ConfirmDailyPlan
import com.mushind.mind.domain.usecase.PrepareTomorrowPlan
import com.mushind.mind.domain.usecase.ReconcileDays
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    clock: ClockProvider,
    logicalDayResolver: LogicalDayResolver,
    repository: DailyPlanRepository,
    private val prepareTomorrowPlan: PrepareTomorrowPlan,
    private val confirmDailyPlan: ConfirmDailyPlan,
    private val reconcileDays: ReconcileDays,
) : ViewModel() {
    private val today = logicalDayResolver.resolve(clock.now(), clock.zoneId())

    val uiState = combine(
        repository.observeByDate(today),
        repository.observeByDate(today.plusDays(1)),
    ) { todayPlan, tomorrowPlan ->
        HomeUiState.Content(todayPlan = todayPlan, tomorrowPlan = tomorrowPlan) as HomeUiState
    }.catch {
        emit(HomeUiState.Error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading,
    )

    init {
        reconcile()
    }

    fun prepareTomorrow() {
        viewModelScope.launch { prepareTomorrowPlan() }
    }

    fun confirmTomorrow() {
        val plan = (uiState.value as? HomeUiState.Content)?.tomorrowPlan ?: return
        viewModelScope.launch { confirmDailyPlan(plan.id) }
    }

    fun reconcile() {
        viewModelScope.launch { reconcileDays() }
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Error : HomeUiState
    data class Content(
        val todayPlan: DailyPlan?,
        val tomorrowPlan: DailyPlan?,
    ) : HomeUiState
}

package com.mushind.mind.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.StatisticsDashboard
import com.mushind.mind.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    repository: StatisticsRepository,
    clock: ClockProvider,
) : ViewModel() {
    val state = repository.observeDashboard(clock.now(), clock.zoneId())
        .map<StatisticsDashboard, StatisticsUiState>(StatisticsUiState::Ready)
        .catch { emit(StatisticsUiState.Error) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatisticsUiState.Loading,
        )
}

sealed interface StatisticsUiState {
    data object Loading : StatisticsUiState
    data object Error : StatisticsUiState
    data class Ready(val dashboard: StatisticsDashboard) : StatisticsUiState
}

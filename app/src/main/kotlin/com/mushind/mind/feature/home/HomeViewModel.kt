package com.mushind.mind.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.domain.model.DailyPlan
import com.mushind.mind.domain.model.DailyPlanStatus
import com.mushind.mind.domain.model.LogicalDayResolver
import com.mushind.mind.domain.model.Task
import com.mushind.mind.domain.model.TaskOrigin
import com.mushind.mind.domain.model.UserProgress
import com.mushind.mind.domain.repository.DailyPlanRepository
import com.mushind.mind.domain.repository.TaskRepository
import com.mushind.mind.domain.usecase.ConfirmDailyPlan
import com.mushind.mind.domain.usecase.PrepareTomorrowPlan
import com.mushind.mind.domain.usecase.ReconcileDays
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val clock: ClockProvider,
    logicalDayResolver: LogicalDayResolver,
    repository: DailyPlanRepository,
    private val taskRepository: TaskRepository,
    private val idProvider: IdProvider,
    private val prepareTomorrowPlan: PrepareTomorrowPlan,
    private val confirmDailyPlan: ConfirmDailyPlan,
    private val reconcileDays: ReconcileDays,
) : ViewModel() {
    private val today = logicalDayResolver.resolve(clock.now(), clock.zoneId())
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    val uiState = combine(
        repository.observeByDate(today),
        repository.observeByDate(today.plusDays(1)),
        taskRepository.observeByDate(today),
        taskRepository.observeByDate(today.plusDays(1)),
        taskRepository.observeProgress(),
    ) { todayPlan, tomorrowPlan, todayTasks, tomorrowTasks, progress ->
        HomeUiState.Content(
            todayPlan = todayPlan,
            tomorrowPlan = tomorrowPlan,
            todayTasks = todayTasks,
            tomorrowTasks = tomorrowTasks,
            progress = progress,
        ) as HomeUiState
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

    fun createTomorrowTask(title: String, rewardText: String) {
        val state = uiState.value as? HomeUiState.Content ?: return
        val plan = state.tomorrowPlan
        val reward = rewardText.toIntOrNull()
        if (plan == null || plan.status != DailyPlanStatus.DRAFT) {
            _message.value = "Solo puedes editar el plan de mañana mientras sea borrador."
            return
        }
        if (title.isBlank() || reward == null || reward !in Task.MIN_REWARD..Task.MAX_REWARD) {
            _message.value = "Escribe un título y una recompensa entre 5 y 100 puntos."
            return
        }
        viewModelScope.launch {
            runCatching {
                taskRepository.create(
                    Task(
                        id = idProvider.newId(),
                        planId = plan.id,
                        title = title.trim(),
                        rewardPoints = reward,
                        plannedDate = plan.date,
                        origin = TaskOrigin.PLANNED,
                        generatesPoints = true,
                        createdAt = clock.now(),
                    ),
                )
            }.onFailure { _message.value = "No se pudo crear la tarea." }
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            runCatching { taskRepository.complete(taskId, clock.now()) }
                .onFailure { _message.value = "No se pudo completar la tarea." }
        }
    }

    fun skipTask(taskId: String) {
        viewModelScope.launch {
            runCatching { taskRepository.skip(taskId) }
                .onFailure { _message.value = "No se pudo omitir la tarea." }
        }
    }

    fun dismissMessage() {
        _message.value = null
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Error : HomeUiState
    data class Content(
        val todayPlan: DailyPlan?,
        val tomorrowPlan: DailyPlan?,
        val todayTasks: List<Task>,
        val tomorrowTasks: List<Task>,
        val progress: UserProgress,
    ) : HomeUiState
}

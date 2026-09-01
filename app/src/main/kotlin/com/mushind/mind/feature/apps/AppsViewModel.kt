package com.mushind.mind.feature.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.InstalledApplication
import com.mushind.mind.domain.model.Challenge
import com.mushind.mind.domain.model.PendingRuleChange
import com.mushind.mind.domain.model.RestrictedApp
import com.mushind.mind.domain.repository.AppRulesRepository
import com.mushind.mind.domain.repository.ProtectedRuleChangeRepository
import com.mushind.mind.domain.usecase.AbandonChallenge
import com.mushind.mind.domain.usecase.CancelPendingRuleChange
import com.mushind.mind.domain.usecase.CompleteChallenge
import com.mushind.mind.domain.usecase.ConfirmPendingRuleChange
import com.mushind.mind.domain.usecase.CreateAppRule
import com.mushind.mind.domain.usecase.GetInstalledApps
import com.mushind.mind.domain.usecase.RuleChangeResult
import com.mushind.mind.domain.usecase.UpdateAppRule
import com.mushind.mind.domain.usecase.StartProtectedRuleChange
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.math.max

sealed interface ProtectedChangeUiState {
    val current: RestrictedApp
    val proposedEnabled: Boolean
    val proposedRule: AppRule?

    data class Review(
        override val current: RestrictedApp,
        override val proposedEnabled: Boolean,
        override val proposedRule: AppRule?,
    ) : ProtectedChangeUiState

    data class InProgress(
        override val current: RestrictedApp,
        override val proposedEnabled: Boolean,
        override val proposedRule: AppRule?,
        val challenge: Challenge,
        val questionIndex: Int = 0,
        val mistakes: Int = 0,
        val waitingForMinimumTime: Boolean = false,
    ) : ProtectedChangeUiState

    data class Completed(
        override val current: RestrictedApp,
        override val proposedEnabled: Boolean,
        override val proposedRule: AppRule?,
        val challenge: Challenge,
    ) : ProtectedChangeUiState
}

data class AppsUiState(
    val apps: List<InstalledApplication> = emptyList(),
    val selectedApp: InstalledApplication? = null,
    val query: String = "",
    val isLoading: Boolean = true,
    val message: String? = null,
    val pendingChanges: Map<String, PendingRuleChange> = emptyMap(),
    val protectedChange: ProtectedChangeUiState? = null,
)

private data class AppsMetadata(
    val query: String,
    val selectedPackage: String?,
    val isLoading: Boolean,
    val message: String?,
    val protectedChange: ProtectedChangeUiState?,
)

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val getInstalledApps: GetInstalledApps,
    rulesRepository: AppRulesRepository,
    private val createAppRule: CreateAppRule,
    private val updateAppRule: UpdateAppRule,
    private val clock: ClockProvider,
    protectedRuleChangeRepository: ProtectedRuleChangeRepository,
    private val startProtectedRuleChange: StartProtectedRuleChange,
    private val completeChallenge: CompleteChallenge,
    private val abandonChallenge: AbandonChallenge,
    private val confirmPendingRuleChange: ConfirmPendingRuleChange,
    private val cancelPendingRuleChange: CancelPendingRuleChange,
) : ViewModel() {
    private val catalog = MutableStateFlow<List<InstalledApplication>>(emptyList())
    private val query = MutableStateFlow("")
    private val selectedPackage = MutableStateFlow<String?>(null)
    private val isLoading = MutableStateFlow(true)
    private val message = MutableStateFlow<String?>(null)
    private val protectedChange = MutableStateFlow<ProtectedChangeUiState?>(null)
    private var challengeCompletionJob: Job? = null

    private val metadata = combine(query, selectedPackage, isLoading, message, protectedChange) {
            currentQuery, selection, loading, currentMessage, currentProtectedChange ->
        AppsMetadata(currentQuery, selection, loading, currentMessage, currentProtectedChange)
    }

    val state = combine(
        catalog,
        rulesRepository.observeRestrictedApps(),
        protectedRuleChangeRepository.observePending(),
        metadata,
    ) { catalogApps, restrictedApps, pendingChanges, metadata ->
        val restrictions = restrictedApps.associateBy { it.packageName }
        val enriched = catalogApps.map { it.copy(restriction = restrictions[it.packageName]) }
        AppsUiState(
            apps = enriched
                .filter { metadata.query.isBlank() || it.displayName.contains(metadata.query.trim(), true) }
                .sortedWith(compareByDescending<InstalledApplication> { it.restriction?.isEnabled == true }
                    .thenBy { it.displayName.lowercase() }),
            selectedApp = enriched.firstOrNull { it.packageName == metadata.selectedPackage },
            query = metadata.query,
            isLoading = metadata.isLoading,
            message = metadata.message,
            pendingChanges = pendingChanges.associateBy { it.packageName },
            protectedChange = metadata.protectedChange,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppsUiState())

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            isLoading.value = true
            runCatching { getInstalledApps() }
                .onSuccess { catalog.value = it }
                .onFailure { message.value = "No se pudo leer el catálogo de aplicaciones." }
            isLoading.value = false
        }
    }

    fun setQuery(value: String) { query.value = value }
    fun select(packageName: String?) { selectedPackage.value = packageName; message.value = null }
    fun dismissMessage() { message.value = null }
    fun requestDisable() {
        val current = state.value.selectedApp?.restriction ?: return
        protectedChange.value = ProtectedChangeUiState.Review(current, false, current.rule)
    }

    fun saveRule(type: AppRuleType, costText: String, durationText: String) {
        val app = state.value.selectedApp ?: return
        val cost = costText.toIntOrNull()
        val duration = if (type == AppRuleType.UNTIL_END_OF_DAY) null else durationText.toIntOrNull()
        if (cost == null || (type != AppRuleType.UNTIL_END_OF_DAY && duration == null)) {
            message.value = "Revisa el costo y la duración. Deben ser números válidos."
            return
        }
        viewModelScope.launch {
            val result = runCatching {
                val current = app.restriction
                if (current?.rule == null) {
                    createAppRule(app, type, cost, duration)
                } else {
                    val now = clock.now()
                    updateAppRule(
                        current,
                        AppRule(app.packageName, type, cost, duration, current.rule.createdAt, now),
                    )
                }
            }.getOrElse {
                message.value = it.message ?: "La regla no es válida."
                return@launch
            }
            message.value = when (result) {
                RuleChangeResult.Saved -> {
                    state.value.pendingChanges[app.packageName]?.let { cancelPendingRuleChange(it.id) }
                    "Regla guardada correctamente."
                }
                RuleChangeResult.RequiresChallenge -> {
                    val current = app.restriction ?: return@launch
                    val proposed = AppRule(app.packageName, type, cost, duration, current.rule?.createdAt ?: clock.now(), clock.now())
                    protectedChange.value = ProtectedChangeUiState.Review(current, true, proposed)
                    null
                }
            }
        }
    }

    fun startChallenge() {
        val review = protectedChange.value as? ProtectedChangeUiState.Review ?: return
        viewModelScope.launch {
            val challenge = startProtectedRuleChange(
                review.current.packageName,
                review.proposedEnabled,
                review.proposedRule,
            )
            protectedChange.value = ProtectedChangeUiState.InProgress(
                review.current,
                review.proposedEnabled,
                review.proposedRule,
                challenge,
            )
        }
    }

    fun answerChallenge(answer: Int) {
        val progress = protectedChange.value as? ProtectedChangeUiState.InProgress ?: return
        if (progress.waitingForMinimumTime) return
        val question = progress.challenge.questions.getOrNull(progress.questionIndex) ?: return
        if (answer != question.correctAnswer) {
            protectedChange.value = progress.copy(
                questionIndex = max(0, progress.questionIndex - 1),
                mistakes = progress.mistakes + 1,
            )
            return
        }
        val answered = progress.questionIndex + 1
        if (answered < progress.challenge.questions.size) {
            protectedChange.value = progress.copy(questionIndex = answered)
            return
        }
        protectedChange.value = progress.copy(questionIndex = answered, waitingForMinimumTime = true)
        challengeCompletionJob = viewModelScope.launch {
            val wait = Duration.between(clock.now(), progress.challenge.minimumCompletesAt).toMillis()
            if (wait > 0) delay(wait)
            if (completeChallenge(progress.challenge, answered, progress.mistakes)) {
                protectedChange.value = ProtectedChangeUiState.Completed(
                    progress.current,
                    progress.proposedEnabled,
                    progress.proposedRule,
                    progress.challenge,
                )
            } else {
                message.value = "No se pudo completar el reto. Inténtalo de nuevo."
                protectedChange.value = null
            }
        }
    }

    fun abandonProtectedChange() {
        val current = protectedChange.value
        challengeCompletionJob?.cancel()
        challengeCompletionJob = null
        protectedChange.value = null
        if (current is ProtectedChangeUiState.InProgress) {
            viewModelScope.launch { abandonChallenge(current.challenge.attemptId) }
        }
    }

    fun confirmProtectedChange() {
        val completed = protectedChange.value as? ProtectedChangeUiState.Completed ?: return
        viewModelScope.launch {
            val pending = confirmPendingRuleChange(completed.challenge.attemptId)
            protectedChange.value = null
            message.value = if (pending != null) {
                "Cambio programado para ${pending.effectiveDay}."
            } else {
                "No se pudo programar el cambio."
            }
        }
    }

    fun cancelPendingChange(id: String) {
        viewModelScope.launch {
            if (cancelPendingRuleChange(id)) message.value = "Cambio pendiente cancelado."
        }
    }
}

package com.mushind.mind.feature.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.InstalledApplication
import com.mushind.mind.domain.repository.AppRulesRepository
import com.mushind.mind.domain.usecase.CreateAppRule
import com.mushind.mind.domain.usecase.GetInstalledApps
import com.mushind.mind.domain.usecase.RuleChangeResult
import com.mushind.mind.domain.usecase.UpdateAppRule
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppsUiState(
    val apps: List<InstalledApplication> = emptyList(),
    val selectedApp: InstalledApplication? = null,
    val query: String = "",
    val isLoading: Boolean = true,
    val message: String? = null,
)

private data class AppsMetadata(
    val query: String,
    val selectedPackage: String?,
    val isLoading: Boolean,
    val message: String?,
)

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val getInstalledApps: GetInstalledApps,
    rulesRepository: AppRulesRepository,
    private val createAppRule: CreateAppRule,
    private val updateAppRule: UpdateAppRule,
    private val clock: ClockProvider,
) : ViewModel() {
    private val catalog = MutableStateFlow<List<InstalledApplication>>(emptyList())
    private val query = MutableStateFlow("")
    private val selectedPackage = MutableStateFlow<String?>(null)
    private val isLoading = MutableStateFlow(true)
    private val message = MutableStateFlow<String?>(null)

    private val metadata = combine(query, selectedPackage, isLoading, message) {
            currentQuery, selection, loading, currentMessage ->
        AppsMetadata(currentQuery, selection, loading, currentMessage)
    }

    val state = combine(catalog, rulesRepository.observeRestrictedApps(), metadata) {
            catalogApps, restrictedApps, metadata ->
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
        message.value = "Desactivar un control es permisivo y requerirá el reto de la Fase 6."
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
                RuleChangeResult.Saved -> "Regla guardada correctamente."
                RuleChangeResult.RequiresChallenge ->
                    "Este cambio reduce la restricción y requerirá el reto de la Fase 6."
            }
        }
    }
}

package com.mushind.mind.domain.repository

import java.time.Instant
import java.time.LocalDate

interface DailyCycleRepository {
    suspend fun lastReconciledDay(): LocalDate?
    suspend fun reconcileDay(day: LocalDate, transitionAt: Instant): DailyReconciliation
}

data class DailyReconciliation(
    val day: LocalDate,
    val closedPlans: Int,
    val expiredDailySessions: Int,
    val activatedPlan: Boolean,
    val appliedRuleChanges: Int = 0,
)

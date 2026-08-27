package com.mushind.mind.domain.repository

import com.mushind.mind.domain.model.DailyPlan
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface DailyPlanRepository {
    fun observeByDate(date: LocalDate): Flow<DailyPlan?>
    suspend fun getByDate(date: LocalDate): DailyPlan?
    suspend fun create(plan: DailyPlan)
    suspend fun confirm(planId: String, confirmedAt: Instant): Boolean
}


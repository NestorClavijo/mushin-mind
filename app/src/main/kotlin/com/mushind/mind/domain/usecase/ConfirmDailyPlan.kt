package com.mushind.mind.domain.usecase

import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.repository.DailyPlanRepository
import javax.inject.Inject

class ConfirmDailyPlan @Inject constructor(
    private val repository: DailyPlanRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(planId: String): Boolean = repository.confirm(planId, clock.now())
}

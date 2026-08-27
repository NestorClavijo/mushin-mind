package com.mushind.mind.domain.usecase

import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.DailyPlan
import com.mushind.mind.domain.model.DailyPlanStatus
import com.mushind.mind.domain.model.LogicalDayResolver
import com.mushind.mind.domain.repository.DailyPlanRepository
import javax.inject.Inject

class PrepareTomorrowPlan @Inject constructor(
    private val repository: DailyPlanRepository,
    private val clock: ClockProvider,
    private val logicalDayResolver: LogicalDayResolver,
    private val idProvider: IdProvider,
) {
    suspend operator fun invoke(): DailyPlan {
        val now = clock.now()
        val tomorrow = logicalDayResolver.resolve(now, clock.zoneId()).plusDays(1)
        repository.getByDate(tomorrow)?.let { return it }

        return DailyPlan(
            id = idProvider.newId(),
            date = tomorrow,
            status = DailyPlanStatus.DRAFT,
            createdAt = now,
        ).also { repository.create(it) }
    }
}

package com.mushind.mind.domain.usecase

import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.LogicalDayResolver
import com.mushind.mind.domain.repository.DailyCycleRepository
import com.mushind.mind.domain.repository.DailyReconciliation
import javax.inject.Inject

class ReconcileDays @Inject constructor(
    private val repository: DailyCycleRepository,
    private val clock: ClockProvider,
    private val logicalDayResolver: LogicalDayResolver,
) {
    suspend operator fun invoke(): List<DailyReconciliation> {
        val now = clock.now()
        val currentDay = logicalDayResolver.resolve(now, clock.zoneId())
        val lastDay = repository.lastReconciledDay()
        val firstDay = when {
            lastDay == null -> currentDay
            lastDay >= currentDay -> currentDay
            else -> lastDay.plusDays(1)
        }

        val results = mutableListOf<DailyReconciliation>()
        var day = firstDay
        while (day <= currentDay) {
            results += repository.reconcileDay(
                day = day,
                transitionAt = logicalDayResolver.startsAt(day, clock.zoneId()),
            )
            day = day.plusDays(1)
        }
        return results
    }
}

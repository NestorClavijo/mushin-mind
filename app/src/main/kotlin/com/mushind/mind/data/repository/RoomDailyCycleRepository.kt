package com.mushind.mind.data.repository

import androidx.room.withTransaction
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.data.local.entity.DailyCycleStateEntity
import com.mushind.mind.data.local.entity.DailySummaryEntity
import com.mushind.mind.domain.repository.DailyCycleRepository
import com.mushind.mind.domain.repository.DailyReconciliation
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class RoomDailyCycleRepository @Inject constructor(
    private val database: MindDatabase,
) : DailyCycleRepository {
    private val dao = database.dailyCycleDao()

    override suspend fun lastReconciledDay(): LocalDate? = dao.lastReconciledDay()

    override suspend fun reconcileDay(
        day: LocalDate,
        transitionAt: Instant,
    ): DailyReconciliation = database.withTransaction {
        var closedPlans = 0
        dao.openPlansBefore(day).forEach { plan ->
            val counts = dao.taskCounts(plan.id)
            dao.insertSummary(
                DailySummaryEntity(
                    day = plan.date,
                    plannedTasks = counts.plannedTasks,
                    completedTasks = counts.completedTasks,
                    skippedTasks = counts.skippedTasks,
                    cancelledTasks = counts.cancelledTasks,
                    pointsEarned = dao.pointsEarnedForPlan(plan.id),
                    pointsSpent = dao.pointsSpentForDay(plan.date),
                    createdAt = transitionAt,
                ),
            )
            closedPlans += dao.closePlan(plan.id, transitionAt)
        }

        val expiredSessions = dao.expireDailySessionsBefore(day)
        val appliedRuleChanges = applyDueRuleChanges(database, day, transitionAt)
        val activated = dao.activateConfirmedPlan(day) == 1
        dao.setCycleState(DailyCycleStateEntity(lastReconciledDay = day))

        DailyReconciliation(
            day = day,
            closedPlans = closedPlans,
            expiredDailySessions = expiredSessions,
            appliedRuleChanges = appliedRuleChanges,
            activatedPlan = activated,
        )
    }
}

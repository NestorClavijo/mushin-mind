package com.mushind.mind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mushind.mind.data.local.entity.DailyCycleStateEntity
import com.mushind.mind.data.local.entity.DailyPlanEntity
import com.mushind.mind.data.local.entity.DailySummaryEntity
import com.mushind.mind.data.local.entity.UnlockSessionStatus
import com.mushind.mind.data.local.entity.UnlockSessionType
import com.mushind.mind.domain.model.DailyPlanStatus
import com.mushind.mind.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate

data class TaskStatusCounts(
    val plannedTasks: Int,
    val completedTasks: Int,
    val skippedTasks: Int,
    val cancelledTasks: Int,
)

@Dao
interface DailyCycleDao {
    @Query("SELECT lastReconciledDay FROM daily_cycle_state WHERE id = 1")
    suspend fun lastReconciledDay(): LocalDate?

    @Query(
        """
        SELECT * FROM daily_plans
        WHERE date < :day AND status IN (:activeStatus, :confirmedStatus)
        ORDER BY date ASC
        """,
    )
    suspend fun openPlansBefore(
        day: LocalDate,
        activeStatus: DailyPlanStatus = DailyPlanStatus.ACTIVE,
        confirmedStatus: DailyPlanStatus = DailyPlanStatus.CONFIRMED,
    ): List<DailyPlanEntity>

    @Query(
        """
        SELECT
            COUNT(*) AS plannedTasks,
            COALESCE(SUM(CASE WHEN status = :completed THEN 1 ELSE 0 END), 0) AS completedTasks,
            COALESCE(SUM(CASE WHEN status = :skipped THEN 1 ELSE 0 END), 0) AS skippedTasks,
            COALESCE(SUM(CASE WHEN status = :cancelled THEN 1 ELSE 0 END), 0) AS cancelledTasks
        FROM tasks WHERE planId = :planId
        """,
    )
    suspend fun taskCounts(
        planId: String,
        completed: TaskStatus = TaskStatus.COMPLETED,
        skipped: TaskStatus = TaskStatus.SKIPPED,
        cancelled: TaskStatus = TaskStatus.CANCELLED,
    ): TaskStatusCounts

    @Query(
        """
        SELECT COALESCE(SUM(point_transactions.amount), 0)
        FROM point_transactions
        INNER JOIN tasks ON tasks.id = point_transactions.referenceId
        WHERE tasks.planId = :planId AND point_transactions.amount > 0
        """,
    )
    suspend fun pointsEarnedForPlan(planId: String): Int

    @Query("SELECT COALESCE(SUM(costPoints), 0) FROM unlock_sessions WHERE logicalDay = :day")
    suspend fun pointsSpentForDay(day: LocalDate): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSummary(summary: DailySummaryEntity): Long

    @Query("SELECT * FROM daily_summaries WHERE day = :day")
    suspend fun getSummary(day: LocalDate): DailySummaryEntity?

    @Query(
        """
        UPDATE daily_plans SET status = :closedStatus, closedAt = :closedAt
        WHERE id = :planId AND status IN (:activeStatus, :confirmedStatus)
        """,
    )
    suspend fun closePlan(
        planId: String,
        closedAt: Instant,
        activeStatus: DailyPlanStatus = DailyPlanStatus.ACTIVE,
        confirmedStatus: DailyPlanStatus = DailyPlanStatus.CONFIRMED,
        closedStatus: DailyPlanStatus = DailyPlanStatus.CLOSED,
    ): Int

    @Query(
        """
        UPDATE unlock_sessions SET status = :expiredStatus
        WHERE logicalDay < :day AND type = :dailyType AND status = :activeStatus
        """,
    )
    suspend fun expireDailySessionsBefore(
        day: LocalDate,
        dailyType: UnlockSessionType = UnlockSessionType.UNTIL_END_OF_DAY,
        activeStatus: UnlockSessionStatus = UnlockSessionStatus.ACTIVE,
        expiredStatus: UnlockSessionStatus = UnlockSessionStatus.EXPIRED,
    ): Int

    @Query(
        """
        UPDATE daily_plans SET status = :activeStatus
        WHERE date = :day AND status = :confirmedStatus
        """,
    )
    suspend fun activateConfirmedPlan(
        day: LocalDate,
        confirmedStatus: DailyPlanStatus = DailyPlanStatus.CONFIRMED,
        activeStatus: DailyPlanStatus = DailyPlanStatus.ACTIVE,
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setCycleState(state: DailyCycleStateEntity)
}

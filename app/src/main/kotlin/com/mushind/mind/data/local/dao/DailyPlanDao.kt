package com.mushind.mind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mushind.mind.data.local.entity.DailyPlanEntity
import com.mushind.mind.domain.model.DailyPlanStatus
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyPlanDao {
    @Query("SELECT * FROM daily_plans WHERE date = :date LIMIT 1")
    fun observeByDate(date: LocalDate): Flow<DailyPlanEntity?>

    @Query("SELECT * FROM daily_plans WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: LocalDate): DailyPlanEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(plan: DailyPlanEntity)

    @Query(
        """
        UPDATE daily_plans
        SET status = :confirmedStatus, confirmedAt = :confirmedAt
        WHERE id = :planId AND status = :draftStatus
        """,
    )
    suspend fun confirm(
        planId: String,
        confirmedAt: Instant,
        draftStatus: DailyPlanStatus = DailyPlanStatus.DRAFT,
        confirmedStatus: DailyPlanStatus = DailyPlanStatus.CONFIRMED,
    ): Int
}


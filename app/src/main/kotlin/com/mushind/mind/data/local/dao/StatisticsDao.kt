package com.mushind.mind.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.mushind.mind.data.local.entity.EmergencyUnlockEntity
import com.mushind.mind.data.local.entity.PointTransactionEntity
import com.mushind.mind.data.local.entity.TaskEntity
import com.mushind.mind.data.local.entity.UnlockSessionEntity
import com.mushind.mind.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

data class AppLabelRow(
    val packageName: String,
    val displayName: String,
)

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM tasks WHERE plannedDate >= :fromDay AND plannedDate < :toDay")
    fun observeTasks(fromDay: LocalDate, toDay: LocalDate): Flow<List<TaskEntity>>

    @Query(
        """SELECT * FROM point_transactions
        WHERE createdAt >= :fromInstant AND createdAt < :toInstant
        ORDER BY createdAt DESC""",
    )
    fun observeTransactions(
        fromInstant: Instant,
        toInstant: Instant,
    ): Flow<List<PointTransactionEntity>>

    @Query("SELECT * FROM unlock_sessions WHERE logicalDay >= :fromDay AND logicalDay < :toDay")
    fun observeSessions(fromDay: LocalDate, toDay: LocalDate): Flow<List<UnlockSessionEntity>>

    @Query(
        """SELECT * FROM emergency_unlocks
        WHERE createdAt >= :fromInstant AND createdAt < :toInstant
        ORDER BY createdAt DESC""",
    )
    fun observeEmergencies(
        fromInstant: Instant,
        toInstant: Instant,
    ): Flow<List<EmergencyUnlockEntity>>

    @Query("SELECT packageName, displayName FROM restricted_apps")
    fun observeAppLabels(): Flow<List<AppLabelRow>>

    @Query("SELECT * FROM point_transactions ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecentTransactions(limit: Int): Flow<List<PointTransactionEntity>>

    @Query(
        """SELECT * FROM tasks WHERE status != :pending
        ORDER BY COALESCE(completedAt, createdAt) DESC LIMIT :limit""",
    )
    fun observeRecentTasks(
        limit: Int,
        pending: TaskStatus = TaskStatus.PENDING,
    ): Flow<List<TaskEntity>>

    @Query("SELECT * FROM emergency_unlocks ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecentEmergencies(limit: Int): Flow<List<EmergencyUnlockEntity>>
}

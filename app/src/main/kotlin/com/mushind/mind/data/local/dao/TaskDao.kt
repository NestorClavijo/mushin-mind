package com.mushind.mind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mushind.mind.data.local.entity.TaskEntity
import com.mushind.mind.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE plannedDate = :day ORDER BY createdAt ASC")
    fun observeByDate(day: LocalDate): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Query(
        """UPDATE tasks SET status = :completed, completedAt = :completedAt
        WHERE id = :id AND status = :pending""",
    )
    suspend fun completeIfPending(
        id: String,
        completedAt: Instant,
        pending: TaskStatus = TaskStatus.PENDING,
        completed: TaskStatus = TaskStatus.COMPLETED,
    ): Int

    @Query("UPDATE tasks SET status = :skipped WHERE id = :id AND status = :pending")
    suspend fun skipIfPending(
        id: String,
        pending: TaskStatus = TaskStatus.PENDING,
        skipped: TaskStatus = TaskStatus.SKIPPED,
    ): Int
}

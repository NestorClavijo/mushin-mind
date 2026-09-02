package com.mushind.mind.domain.repository

import com.mushind.mind.domain.model.Task
import com.mushind.mind.domain.model.UserProgress
import com.mushind.mind.domain.usecase.CompleteTaskResult
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeByDate(day: LocalDate): Flow<List<Task>>
    fun observeProgress(): Flow<UserProgress>
    suspend fun create(task: Task)
    suspend fun complete(taskId: String, completedAt: Instant): CompleteTaskResult?
    suspend fun skip(taskId: String): Boolean
}

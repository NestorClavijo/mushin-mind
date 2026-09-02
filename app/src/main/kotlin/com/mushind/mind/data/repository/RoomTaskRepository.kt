package com.mushind.mind.data.repository

import androidx.room.withTransaction
import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.data.local.entity.PointTransactionEntity
import com.mushind.mind.data.local.entity.TaskEntity
import com.mushind.mind.data.local.entity.UserProgressEntity
import com.mushind.mind.domain.model.Task
import com.mushind.mind.domain.model.UserProgress
import com.mushind.mind.domain.repository.TaskRepository
import com.mushind.mind.domain.usecase.CompleteTask
import com.mushind.mind.domain.usecase.CompleteTaskResult
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTaskRepository @Inject constructor(
    private val database: MindDatabase,
    private val idProvider: IdProvider,
) : TaskRepository {
    private val taskDao = database.taskDao()
    private val progressDao = database.unlockSessionDao()
    private val completeTask = CompleteTask()

    override fun observeByDate(day: LocalDate): Flow<List<Task>> =
        taskDao.observeByDate(day).map { tasks -> tasks.map(TaskEntity::toDomain) }

    override fun observeProgress(): Flow<UserProgress> =
        progressDao.observeProgress().map { it?.toDomain() ?: UserProgress() }

    override suspend fun create(task: Task) = taskDao.insert(task.toEntity())

    override suspend fun complete(taskId: String, completedAt: Instant): CompleteTaskResult? =
        database.withTransaction {
            val task = taskDao.getById(taskId)?.toDomain() ?: return@withTransaction null
            progressDao.initializeProgress(UserProgressEntity(balance = 0, xp = 0))
            val progress = progressDao.getProgress()?.toDomain() ?: UserProgress()
            when (val result = completeTask.execute(task, progress, completedAt, idProvider.newId())) {
                CompleteTaskResult.AlreadyCompleted -> result
                is CompleteTaskResult.Completed -> {
                    if (taskDao.completeIfPending(taskId, completedAt) != 1) {
                        return@withTransaction CompleteTaskResult.AlreadyCompleted
                    }
                    val points = result.progress.balance - progress.balance
                    val xp = result.progress.xp - progress.xp
                    check(progressDao.addReward(points, xp) == 1)
                    result.transaction?.let { transaction ->
                        progressDao.insertTransaction(
                            PointTransactionEntity(
                                transaction.id,
                                transaction.type,
                                transaction.amount,
                                transaction.referenceId,
                                transaction.description,
                                transaction.createdAt,
                            ),
                        )
                    }
                    result
                }
            }
        }

    override suspend fun skip(taskId: String): Boolean = taskDao.skipIfPending(taskId) == 1
}

private fun TaskEntity.toDomain() = Task(
    id, planId, title, description, rewardPoints, plannedDate, status, origin,
    generatesPoints, createdAt, completedAt,
)

private fun Task.toEntity() = TaskEntity(
    id, planId, title, description, rewardPoints, plannedDate, status, origin,
    generatesPoints, createdAt, completedAt,
)

private fun UserProgressEntity.toDomain() = UserProgress(balance, xp)

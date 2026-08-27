package com.mushind.mind.domain.usecase

import com.mushind.mind.domain.model.PointTransaction
import com.mushind.mind.domain.model.PointTransactionType
import com.mushind.mind.domain.model.Task
import com.mushind.mind.domain.model.TaskStatus
import com.mushind.mind.domain.model.UserProgress
import java.time.Instant

class CompleteTask {
    fun execute(
        task: Task,
        progress: UserProgress,
        completedAt: Instant,
        transactionId: String,
    ): CompleteTaskResult {
        if (task.status == TaskStatus.COMPLETED) return CompleteTaskResult.AlreadyCompleted
        require(task.status == TaskStatus.PENDING) { "Only pending tasks can be completed" }

        val pointsEarned = if (task.generatesPoints) task.rewardPoints else 0
        val xpEarned = task.rewardPoints
        val completedTask = task.copy(
            status = TaskStatus.COMPLETED,
            completedAt = completedAt,
        )
        val updatedProgress = progress.copy(
            balance = progress.balance + pointsEarned,
            xp = progress.xp + xpEarned,
        )
        val transaction = if (pointsEarned > 0) {
            PointTransaction(
                id = transactionId,
                type = PointTransactionType.TASK_REWARD,
                amount = pointsEarned,
                referenceId = task.id,
                description = task.title,
                createdAt = completedAt,
            )
        } else {
            null
        }

        return CompleteTaskResult.Completed(
            task = completedTask,
            progress = updatedProgress,
            transaction = transaction,
        )
    }
}

sealed interface CompleteTaskResult {
    data object AlreadyCompleted : CompleteTaskResult

    data class Completed(
        val task: Task,
        val progress: UserProgress,
        val transaction: PointTransaction?,
    ) : CompleteTaskResult
}


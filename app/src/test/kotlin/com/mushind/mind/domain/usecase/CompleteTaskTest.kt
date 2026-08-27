package com.mushind.mind.domain.usecase

import com.mushind.mind.domain.model.PointTransactionType
import com.mushind.mind.domain.model.Task
import com.mushind.mind.domain.model.TaskOrigin
import com.mushind.mind.domain.model.TaskStatus
import com.mushind.mind.domain.model.UserProgress
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class CompleteTaskTest {
    private val useCase = CompleteTask()
    private val now = Instant.parse("2026-08-26T15:00:00Z")

    @Test
    fun `completing a planned task awards points exactly once`() {
        val result = useCase.execute(task(), UserProgress(), now, "transaction-1")
            as CompleteTaskResult.Completed

        assertEquals(TaskStatus.COMPLETED, result.task.status)
        assertEquals(20, result.progress.balance)
        assertEquals(20, result.transaction?.amount)
        assertEquals(PointTransactionType.TASK_REWARD, result.transaction?.type)

        val repeated = useCase.execute(result.task, result.progress, now, "transaction-2")
        assertSame(CompleteTaskResult.AlreadyCompleted, repeated)
    }

    @Test
    fun `task added during active day earns xp but no spendable points`() {
        val result = useCase.execute(
            task = task(origin = TaskOrigin.ADDED_TODAY, generatesPoints = false),
            progress = UserProgress(balance = 10, xp = 40),
            completedAt = now,
            transactionId = "unused",
        ) as CompleteTaskResult.Completed

        assertEquals(10, result.progress.balance)
        assertEquals(60, result.progress.xp)
        assertNull(result.transaction)
    }

    @Test
    fun `xp is historical and independent from current balance`() {
        val result = useCase.execute(
            task = task(),
            progress = UserProgress(balance = 5, xp = 100),
            completedAt = now,
            transactionId = "transaction-1",
        ) as CompleteTaskResult.Completed

        assertEquals(25, result.progress.balance)
        assertEquals(120, result.progress.xp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `skipped task cannot be completed`() {
        useCase.execute(
            task = task().copy(status = TaskStatus.SKIPPED),
            progress = UserProgress(),
            completedAt = now,
            transactionId = "transaction-1",
        )
    }

    private fun task(
        origin: TaskOrigin = TaskOrigin.PLANNED,
        generatesPoints: Boolean = true,
    ) = Task(
        id = "task-1",
        planId = "plan-1",
        title = "Estudiar Kotlin",
        rewardPoints = 20,
        plannedDate = LocalDate.parse("2026-08-26"),
        origin = origin,
        generatesPoints = generatesPoints,
        createdAt = Instant.parse("2026-08-25T20:00:00Z"),
    )
}


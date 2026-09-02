package com.mushind.mind.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.data.local.entity.DailyPlanEntity
import com.mushind.mind.domain.model.DailyPlanStatus
import com.mushind.mind.domain.model.PointTransactionType
import com.mushind.mind.domain.model.Task
import com.mushind.mind.domain.model.TaskOrigin
import com.mushind.mind.domain.model.TaskStatus
import com.mushind.mind.domain.model.UserProgress
import com.mushind.mind.domain.usecase.CompleteTaskResult
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomTaskRepositoryTest {
    private lateinit var database: MindDatabase
    private lateinit var repository: RoomTaskRepository
    private val day = LocalDate.parse("2026-09-01")
    private val now = Instant.parse("2026-09-01T15:00:00Z")

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MindDatabase::class.java).build()
        val ids = AtomicInteger()
        repository = RoomTaskRepository(database, IdProvider { "transaction-${ids.incrementAndGet()}" })
        database.dailyPlanDao().insert(
            DailyPlanEntity("plan-1", day, DailyPlanStatus.ACTIVE, now.minusSeconds(86_400), now, null),
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun completingTaskUpdatesTaskLedgerBalanceAndXpAtomically() = runBlocking {
        repository.create(task("task-1"))

        val result = repository.complete("task-1", now) as CompleteTaskResult.Completed

        assertEquals(TaskStatus.COMPLETED, result.task.status)
        assertEquals(UserProgress(balance = 20, xp = 20), repository.observeProgress().first())
        assertEquals(1, database.unlockSessionDao().transactionCount(PointTransactionType.TASK_REWARD))
        assertEquals(TaskStatus.COMPLETED, repository.observeByDate(day).first().single().status)

        assertSame(CompleteTaskResult.AlreadyCompleted, repository.complete("task-1", now.plusSeconds(1)))
        assertEquals(UserProgress(balance = 20, xp = 20), repository.observeProgress().first())
        assertEquals(1, database.unlockSessionDao().transactionCount(PointTransactionType.TASK_REWARD))
    }

    @Test
    fun taskAddedTodayEarnsXpWithoutCreatingSpendableLedgerEntry() = runBlocking {
        repository.create(task("task-1", origin = TaskOrigin.ADDED_TODAY, generatesPoints = false))

        repository.complete("task-1", now)

        assertEquals(UserProgress(balance = 0, xp = 20), repository.observeProgress().first())
        assertEquals(0, database.unlockSessionDao().transactionCount(PointTransactionType.TASK_REWARD))
    }

    @Test
    fun transactionFailureRollsBackTaskAndProgress() = runBlocking {
        val colliding = RoomTaskRepository(database, IdProvider { "same-transaction" })
        colliding.create(task("task-1"))
        colliding.create(task("task-2"))
        colliding.complete("task-1", now)

        runCatching { colliding.complete("task-2", now.plusSeconds(1)) }

        assertEquals(TaskStatus.PENDING, database.taskDao().getById("task-2")?.status)
        assertEquals(UserProgress(balance = 20, xp = 20), colliding.observeProgress().first())
        assertEquals(1, database.unlockSessionDao().transactionCount(PointTransactionType.TASK_REWARD))
    }

    private fun task(
        id: String,
        origin: TaskOrigin = TaskOrigin.PLANNED,
        generatesPoints: Boolean = true,
    ) = Task(
        id = id,
        planId = "plan-1",
        title = "Tarea $id",
        rewardPoints = 20,
        plannedDate = day,
        origin = origin,
        generatesPoints = generatesPoints,
        createdAt = now.minusSeconds(3_600),
    )
}

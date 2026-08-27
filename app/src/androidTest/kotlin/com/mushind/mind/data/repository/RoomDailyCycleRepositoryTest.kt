package com.mushind.mind.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.data.local.entity.DailyPlanEntity
import com.mushind.mind.data.local.entity.UnlockSessionEntity
import com.mushind.mind.data.local.entity.UnlockSessionStatus
import com.mushind.mind.data.local.entity.UnlockSessionType
import com.mushind.mind.domain.model.DailyPlanStatus
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDailyCycleRepositoryTest {
    private lateinit var database: MindDatabase
    private lateinit var repository: RoomDailyCycleRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomDailyCycleRepository(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun reconcileClosesPreviousPlanExpiresDailySessionAndActivatesCurrentPlan() = runBlocking {
        val yesterday = LocalDate.parse("2026-08-25")
        val today = LocalDate.parse("2026-08-26")
        val transition = Instant.parse("2026-08-26T05:00:00Z")
        database.dailyPlanDao().insert(plan("yesterday", yesterday, DailyPlanStatus.ACTIVE))
        database.dailyPlanDao().insert(plan("today", today, DailyPlanStatus.CONFIRMED))
        database.unlockSessionDao().insert(
            UnlockSessionEntity(
                id = "session-1",
                packageName = "com.example.distracting",
                type = UnlockSessionType.UNTIL_END_OF_DAY,
                startsAt = transition.minusSeconds(3_600),
                endsAt = transition,
                logicalDay = yesterday,
                costPoints = 30,
                status = UnlockSessionStatus.ACTIVE,
            ),
        )

        val result = repository.reconcileDay(today, transition)

        assertEquals(1, result.closedPlans)
        assertEquals(1, result.expiredDailySessions)
        assertEquals(true, result.activatedPlan)
        assertEquals(DailyPlanStatus.CLOSED, database.dailyPlanDao().getByDate(yesterday)?.status)
        assertEquals(DailyPlanStatus.ACTIVE, database.dailyPlanDao().getByDate(today)?.status)
        assertEquals(
            UnlockSessionStatus.EXPIRED,
            database.unlockSessionDao().getById("session-1")?.status,
        )
        assertNotNull(database.dailyCycleDao().getSummary(yesterday))
    }

    private fun plan(id: String, day: LocalDate, status: DailyPlanStatus) = DailyPlanEntity(
        id = id,
        date = day,
        status = status,
        createdAt = Instant.parse("2026-08-24T20:00:00Z"),
        confirmedAt = Instant.parse("2026-08-24T21:00:00Z"),
        closedAt = null,
    )
}

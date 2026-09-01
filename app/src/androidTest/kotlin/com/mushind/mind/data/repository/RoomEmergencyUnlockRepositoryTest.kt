package com.mushind.mind.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.data.local.entity.UnlockSessionStatus
import com.mushind.mind.data.local.entity.UserProgressEntity
import com.mushind.mind.domain.model.EmergencyPenaltyMode
import com.mushind.mind.domain.model.EmergencyPolicy
import com.mushind.mind.domain.model.UnlockSessionKind
import com.mushind.mind.domain.repository.EmergencyUnlockRequest
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomEmergencyUnlockRepositoryTest {
    private lateinit var database: MindDatabase
    private lateinit var repository: RoomEmergencyUnlockRepository
    private val now = Instant.parse("2026-08-26T15:00:00Z")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MindDatabase::class.java).build()
        val sequence = AtomicInteger()
        repository = RoomEmergencyUnlockRepository(database, IdProvider { "id-${sequence.incrementAndGet()}" })
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun emergencyIsAuditedAndCreatesDedicatedSession() = runBlocking {
        seedBalance(40)

        val result = repository.create(request(reason = "Necesito responder una llamada"))
        val stored = repository.get(result.event.id)

        assertNotNull(stored)
        assertEquals("Necesito responder una llamada", stored?.reason)
        assertEquals(20, stored?.appliedPenaltyPoints)
        assertEquals(40, stored?.balanceBefore)
        assertEquals(20, stored?.balanceAfter)
        assertEquals(UnlockSessionKind.EMERGENCY, result.session.type)
        assertEquals(now.plusSeconds(10 * 60), result.session.endsAt)
        assertEquals(20, database.unlockSessionDao().getBalance())
    }

    @Test
    fun penaltyIsCappedAtAvailableBalance() = runBlocking {
        seedBalance(7)

        val result = repository.create(request(penaltyPoints = 20))

        assertEquals(7, result.event.appliedPenaltyPoints)
        assertEquals(0, result.event.balanceAfter)
        assertEquals(0, database.unlockSessionDao().getBalance())
    }

    @Test
    fun zeroBalanceStillCreatesAuditedEmergencyWithoutDebt() = runBlocking {
        seedBalance(0)

        val result = repository.create(request(penaltyPoints = 20))

        assertEquals(0, result.event.appliedPenaltyPoints)
        assertEquals(0, result.event.balanceAfter)
        assertNotNull(repository.get(result.event.id))
    }

    @Test
    fun emergencySessionExpiresAtConfiguredTimestamp() = runBlocking {
        seedBalance(40)
        val result = repository.create(request())

        assertNotNull(database.unlockSessionDao().getActiveSession(PACKAGE_NAME, result.session.endsAt.minusMillis(1)))
        assertEquals(1, database.unlockSessionDao().expireSessions(result.session.endsAt))
        assertNull(database.unlockSessionDao().getActiveSession(PACKAGE_NAME, result.session.endsAt))
        assertEquals(UnlockSessionStatus.EXPIRED, database.unlockSessionDao().getById(result.session.id)?.status)
    }

    private suspend fun seedBalance(balance: Int) {
        database.unlockSessionDao().setProgress(UserProgressEntity(balance = balance, xp = 0))
    }

    private fun request(
        reason: String? = null,
        penaltyPoints: Int = 20,
    ) = EmergencyUnlockRequest(
        packageName = PACKAGE_NAME,
        reason = reason,
        policy = EmergencyPolicy(
            durationMinutes = 10,
            penaltyMode = EmergencyPenaltyMode.FIXED_POINTS,
            fixedPenaltyPoints = penaltyPoints,
        ),
        createdAt = now,
        logicalDay = LocalDate.parse("2026-08-26"),
    )

    private companion object {
        const val PACKAGE_NAME = "com.example.messaging"
    }
}

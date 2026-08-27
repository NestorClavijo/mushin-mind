package com.mushind.mind.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.data.local.entity.UserProgressEntity
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.repository.SessionPurchaseRequest
import com.mushind.mind.domain.repository.SessionPurchaseResult
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomUnlockSessionRepositoryTest {
    private lateinit var database: MindDatabase
    private lateinit var repository: RoomUnlockSessionRepository
    private val now = Instant.parse("2026-08-26T15:00:00Z")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MindDatabase::class.java).build()
        val sequence = AtomicInteger()
        repository = RoomUnlockSessionRepository(database, IdProvider { "id-${sequence.incrementAndGet()}" })
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun purchaseDebitsBalanceAndCreatesSessionAtomically() = runBlocking {
        seedBalance(40)

        val result = repository.purchase(request()) as SessionPurchaseResult.Purchased

        assertEquals(10, result.remainingBalance)
        assertEquals(now.plusSeconds(20 * 60), result.session.endsAt)
        assertEquals(1, database.unlockSessionDao().unlockTransactionCount())
    }

    @Test
    fun insufficientBalanceCreatesNeitherDebitNorSession() = runBlocking {
        seedBalance(20)

        val result = repository.purchase(request())

        assertEquals(SessionPurchaseResult.InsufficientPoints(20, 30), result)
        assertEquals(0, database.unlockSessionDao().unlockTransactionCount())
        assertNull(repository.getActiveSession(PACKAGE_NAME, now))
    }

    @Test
    fun concurrentPurchasesCannotOverspend() = runBlocking {
        seedBalance(40)

        val results = listOf(
            async(Dispatchers.Default) { repository.purchase(request()) },
            async(Dispatchers.Default) { repository.purchase(request()) },
        ).awaitAll()

        assertEquals(1, results.count { it is SessionPurchaseResult.Purchased })
        assertEquals(1, results.count { it is SessionPurchaseResult.InsufficientPoints })
        assertEquals(10, repository.currentBalance())
        assertEquals(1, database.unlockSessionDao().unlockTransactionCount())
    }

    @Test
    fun purchaseDuringTemporarySessionExtendsFromCurrentEnd() = runBlocking {
        seedBalance(70)
        val first = repository.purchase(request()) as SessionPurchaseResult.Purchased

        val second = repository.purchase(request()) as SessionPurchaseResult.Purchased

        assertEquals(first.session.id, second.session.id)
        assertEquals(now.plusSeconds(40 * 60), second.session.endsAt)
        assertEquals(10, second.remainingBalance)
        assertEquals(2, database.unlockSessionDao().unlockTransactionCount())
    }

    @Test
    fun untilEndOfDaySessionUsesLogicalBoundaryAndRuleSnapshot() = runBlocking {
        seedBalance(40)
        val dayEnd = Instant.parse("2026-08-27T05:00:00Z")
        val dailyRule = AppRule(
            PACKAGE_NAME,
            AppRuleType.UNTIL_END_OF_DAY,
            30,
            null,
            now,
            now,
        )

        val result = repository.purchase(
            SessionPurchaseRequest(dailyRule, now, LocalDate.parse("2026-08-26"), dayEnd),
        ) as SessionPurchaseResult.Purchased

        assertEquals(dayEnd, result.session.endsAt)
        assertEquals(AppRuleType.UNTIL_END_OF_DAY, result.session.appliedRuleType)
    }

    @Test
    fun timestampExpirationAndEarlyEndInvalidateSessionWithoutRefund() = runBlocking {
        seedBalance(40)
        val purchased = repository.purchase(request()) as SessionPurchaseResult.Purchased

        assertEquals(1, repository.expireSessions(purchased.session.endsAt))
        assertNull(repository.getActiveSession(PACKAGE_NAME, purchased.session.endsAt))
        assertEquals(10, repository.currentBalance())

        seedBalance(40)
        val another = repository.purchase(request(now.plusSeconds(3_000))) as SessionPurchaseResult.Purchased
        assertEquals(true, repository.endSessionEarly(another.session.id, now.plusSeconds(3_060)))
        assertNull(repository.getActiveSession(PACKAGE_NAME, now.plusSeconds(3_061)))
        assertEquals(10, repository.currentBalance())
    }

    @Test
    fun logicalDayReconciliationDoesNotExpireValidTemporarySession() = runBlocking {
        seedBalance(40)
        val purchased = repository.purchase(request(durationMinutes = 240)) as SessionPurchaseResult.Purchased

        RoomDailyCycleRepository(database).reconcileDay(
            LocalDate.parse("2026-08-27"),
            now.plusSeconds(60),
        )

        assertNotNull(repository.getActiveSession(PACKAGE_NAME, now.plusSeconds(120)))
        assertEquals(purchased.session.endsAt, repository.getActiveSession(PACKAGE_NAME, now.plusSeconds(120))?.endsAt)
    }

    private suspend fun seedBalance(balance: Int) {
        database.unlockSessionDao().setProgress(UserProgressEntity(balance = balance, xp = 0))
    }

    private fun request(
        purchasedAt: Instant = now,
        durationMinutes: Int = 20,
    ) = SessionPurchaseRequest(
        rule = AppRule(
            PACKAGE_NAME,
            AppRuleType.TEMPORARY_SESSION,
            30,
            durationMinutes,
            now,
            now,
        ),
        purchasedAt = purchasedAt,
        logicalDay = LocalDate.parse("2026-08-26"),
        logicalDayEnd = Instant.parse("2026-08-27T05:00:00Z"),
    )

    private companion object {
        const val PACKAGE_NAME = "com.example.video"
    }
}

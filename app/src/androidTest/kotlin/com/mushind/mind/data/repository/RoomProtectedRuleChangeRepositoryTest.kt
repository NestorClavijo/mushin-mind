package com.mushind.mind.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.Challenge
import com.mushind.mind.domain.model.ChallengeQuestion
import com.mushind.mind.domain.model.ChallengeQuestionType
import com.mushind.mind.domain.model.RestrictedApp
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomProtectedRuleChangeRepositoryTest {
    private lateinit var database: MindDatabase
    private lateinit var repository: RoomProtectedRuleChangeRepository
    private lateinit var rulesRepository: RoomAppRulesRepository
    private val now = Instant.parse("2026-08-26T15:00:00Z")
    private val tomorrow = LocalDate.parse("2026-08-27")

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MindDatabase::class.java).build()
        val sequence = AtomicInteger()
        repository = RoomProtectedRuleChangeRepository(database, IdProvider { "id-${sequence.incrementAndGet()}" })
        rulesRepository = RoomAppRulesRepository(database.appRulesDao())
        val current = rule(30)
        rulesRepository.saveRestrictedApp(
            RestrictedApp(PACKAGE, "Social", true, false, current, now, now),
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun permissiveChangeDoesNotApplyToday() = runBlocking {
        saveSuccessfulAttempt(rule(10))
        repository.schedulePending(ATTEMPT, now)

        assertEquals(30, rulesRepository.getRestrictedApp(PACKAGE)?.rule?.costPoints)
        assertEquals(0, repository.applyDue(tomorrow.minusDays(1), now))
        assertEquals(30, rulesRepository.getRestrictedApp(PACKAGE)?.rule?.costPoints)
    }

    @Test
    fun abandoningChallengeCreatesNoPendingChange() = runBlocking {
        repository.saveAttempt(challenge(), true, rule(10))

        assertEquals(true, repository.abandonAttempt(ATTEMPT))
        assertNull(repository.schedulePending(ATTEMPT, now))
        assertEquals(emptyList<Any>(), repository.observePending().first())
    }

    @Test
    fun challengeCannotCompleteBeforeMinimumDuration() = runBlocking {
        val delayed = challenge().copy(minimumCompletesAt = now.plusSeconds(60))
        repository.saveAttempt(delayed, true, rule(10))

        assertEquals(false, repository.completeAttempt(ATTEMPT, 1, 0, now.plusSeconds(59)))
        assertNull(repository.schedulePending(ATTEMPT, now.plusSeconds(59)))
    }

    @Test
    fun successfulChallengeCreatesOnlyOnePendingChange() = runBlocking {
        saveSuccessfulAttempt(rule(10))

        val first = repository.schedulePending(ATTEMPT, now)
        val repeated = repository.schedulePending(ATTEMPT, now.plusSeconds(1))

        assertEquals(first?.id, repeated?.id)
        assertEquals(1, repository.observePending().first().size)
    }

    @Test
    fun dueChangeAppliesExactlyOnceAtNewLogicalDay() = runBlocking {
        saveSuccessfulAttempt(rule(10))
        repository.schedulePending(ATTEMPT, now)

        val dailyCycle = RoomDailyCycleRepository(database)
        assertEquals(1, dailyCycle.reconcileDay(tomorrow, now.plusSeconds(60)).appliedRuleChanges)
        assertEquals(0, dailyCycle.reconcileDay(tomorrow, now.plusSeconds(120)).appliedRuleChanges)
        assertEquals(10, rulesRepository.getRestrictedApp(PACKAGE)?.rule?.costPoints)
    }

    @Test
    fun `new request replaces previous pending change for same app`() = runBlocking {
        saveSuccessfulAttempt(rule(10), ATTEMPT)
        repository.schedulePending(ATTEMPT, now)
        saveSuccessfulAttempt(rule(15), "attempt-2")

        repository.schedulePending("attempt-2", now.plusSeconds(1))

        val pending = repository.observePending().first()
        assertEquals(1, pending.size)
        assertEquals(15, pending.single().proposedRule?.costPoints)
    }

    private suspend fun saveSuccessfulAttempt(proposed: AppRule, id: String = ATTEMPT) {
        val challenge = challenge(id)
        repository.saveAttempt(challenge, true, proposed)
        assertEquals(true, repository.completeAttempt(id, 1, 0, now))
    }

    private fun challenge(id: String = ATTEMPT) = Challenge(
        attemptId = id,
        packageName = PACKAGE,
        questions = listOf(
            ChallengeQuestion("q1", ChallengeQuestionType.ARITHMETIC, "1 + 1 = ?", listOf(1, 2, 3), 2),
        ),
        startedAt = now,
        minimumCompletesAt = now,
        effectiveDay = tomorrow,
    )

    private fun rule(cost: Int) = AppRule(
        PACKAGE, AppRuleType.TEMPORARY_SESSION, cost, 20, now, now,
    )

    private companion object {
        const val PACKAGE = "com.example.social"
        const val ATTEMPT = "attempt-1"
    }
}

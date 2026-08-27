package com.mushind.mind.domain.usecase

import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.AccessDecision
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.RestrictedApp
import com.mushind.mind.domain.model.UnlockSession
import com.mushind.mind.domain.model.UnlockSessionKind
import com.mushind.mind.domain.repository.SessionPurchaseRequest
import com.mushind.mind.domain.repository.SessionPurchaseResult
import com.mushind.mind.domain.repository.UnlockSessionRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionEngineTest {
    private val now = Instant.parse("2026-08-26T15:00:00Z")
    private val clock = SessionTestClock(now)
    private val rule = AppRule(
        "com.example.video",
        AppRuleType.TEMPORARY_SESSION,
        30,
        20,
        now,
        now,
    )

    @Test
    fun `unrestricted application is allowed`() = runBlocking {
        val decision = CanUnlockApp(FakeSessionRepository(balance = 0), clock)(null)

        assertTrue(decision is AccessDecision.Allowed)
    }

    @Test
    fun `active session takes priority over restriction`() = runBlocking {
        val session = session(endsAt = now.plusSeconds(600))
        val repository = FakeSessionRepository(balance = 0, active = session)

        val decision = CanUnlockApp(repository, clock)(restriction())

        assertEquals(session, (decision as AccessDecision.Allowed).session)
    }

    @Test
    fun `insufficient balance reports exact missing points`() = runBlocking {
        val decision = CanUnlockApp(FakeSessionRepository(balance = 20), clock)(restriction())
            as AccessDecision.BlockedInsufficientPoints

        assertEquals(30, decision.costPoints)
        assertEquals(20, decision.balance)
        assertEquals(10, decision.missingPoints)
    }

    @Test
    fun `sufficient balance offers purchase`() = runBlocking {
        val decision = CanUnlockApp(FakeSessionRepository(balance = 40), clock)(restriction())
            as AccessDecision.BlockedPurchasable

        assertEquals(40, decision.balance)
        assertEquals(rule, decision.rule)
    }

    @Test
    fun `session validity uses absolute timestamps`() {
        val session = session(endsAt = now.plusSeconds(600))

        assertTrue(session.isActiveAt(now))
        assertEquals(Duration.ofMinutes(10), session.remainingAt(now))
        assertTrue(!session.isActiveAt(now.plusSeconds(600)))
        assertEquals(Duration.ZERO, session.remainingAt(now.plusSeconds(601)))
    }

    @Test
    fun `get active expires stale sessions before evaluation`() = runBlocking {
        val repository = FakeSessionRepository(balance = 0, active = session(endsAt = now.minusSeconds(1)))

        val result = GetActiveSession(repository, clock)(rule.packageName)

        assertNull(result)
        assertEquals(1, repository.expireCalls)
    }

    private fun restriction() = RestrictedApp(
        rule.packageName, "Vídeo", true, false, rule, now, now,
    )

    private fun session(endsAt: Instant) = UnlockSession(
        "session", rule.packageName, UnlockSessionKind.TEMPORARY, AppRuleType.TEMPORARY_SESSION,
        now.minusSeconds(60), endsAt, LocalDate.parse("2026-08-26"), 30,
    )
}

private class FakeSessionRepository(
    private val balance: Int,
    private var active: UnlockSession? = null,
) : UnlockSessionRepository {
    var expireCalls = 0
    override suspend fun currentBalance() = balance
    override suspend fun getActiveSession(packageName: String, at: Instant) =
        active?.takeIf { it.packageName == packageName && it.isActiveAt(at) }
    override suspend fun purchase(request: SessionPurchaseRequest): SessionPurchaseResult =
        error("Not needed by these tests")
    override suspend fun expireSessions(at: Instant): Int {
        expireCalls++
        val stale = active?.isActiveAt(at) == false
        if (stale) active = null
        return if (stale) 1 else 0
    }
    override suspend fun endSessionEarly(sessionId: String, endedAt: Instant) = false
}

private class SessionTestClock(private val instant: Instant) : ClockProvider {
    override fun now() = instant
    override fun zoneId(): ZoneId = ZoneId.of("America/Bogota")
}

package com.mushind.mind.domain.usecase

import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.AccessDecision
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.LogicalDayResolver
import com.mushind.mind.domain.model.RestrictedApp
import com.mushind.mind.domain.model.UnlockSession
import com.mushind.mind.domain.repository.SessionPurchaseRequest
import com.mushind.mind.domain.repository.SessionPurchaseResult
import com.mushind.mind.domain.repository.UnlockSessionRepository
import javax.inject.Inject

class CanUnlockApp @Inject constructor(
    private val repository: UnlockSessionRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(restriction: RestrictedApp?): AccessDecision {
        if (restriction == null || !restriction.isEnabled) return AccessDecision.Allowed()
        val active = repository.getActiveSession(restriction.packageName, clock.now())
        if (active != null) return AccessDecision.Allowed(active)
        val rule = requireNotNull(restriction.rule) { "An enabled restriction needs a rule" }
        val balance = repository.currentBalance()
        return if (balance >= rule.costPoints) {
            AccessDecision.BlockedPurchasable(rule, balance)
        } else {
            AccessDecision.BlockedInsufficientPoints(
                costPoints = rule.costPoints,
                balance = balance,
                missingPoints = rule.costPoints - balance,
            )
        }
    }
}

class PurchaseUnlock @Inject constructor(
    private val repository: UnlockSessionRepository,
    private val clock: ClockProvider,
    private val logicalDayResolver: LogicalDayResolver,
) {
    suspend operator fun invoke(rule: AppRule): SessionPurchaseResult {
        val now = clock.now()
        val zone = clock.zoneId()
        val day = logicalDayResolver.resolve(now, zone)
        return repository.purchase(
            SessionPurchaseRequest(rule, now, day, logicalDayResolver.endsAt(day, zone)),
        )
    }
}

class GetActiveSession @Inject constructor(
    private val repository: UnlockSessionRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(packageName: String): UnlockSession? {
        val now = clock.now()
        repository.expireSessions(now)
        return repository.getActiveSession(packageName, now)
    }
}

class ExtendSession @Inject constructor(
    private val purchaseUnlock: PurchaseUnlock,
) {
    suspend operator fun invoke(rule: AppRule): SessionPurchaseResult = purchaseUnlock(rule)
}

class ExpireSessions @Inject constructor(
    private val repository: UnlockSessionRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(): Int = repository.expireSessions(clock.now())
}

class EndSessionEarly @Inject constructor(
    private val repository: UnlockSessionRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(sessionId: String): Boolean =
        repository.endSessionEarly(sessionId, clock.now())
}


package com.mushind.mind.domain.usecase

import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.EmergencyPenaltyMode
import com.mushind.mind.domain.model.EmergencyPreview
import com.mushind.mind.domain.model.LogicalDayResolver
import com.mushind.mind.domain.repository.EmergencyPolicyRepository
import com.mushind.mind.domain.repository.EmergencyUnlockRepository
import com.mushind.mind.domain.repository.EmergencyUnlockRequest
import com.mushind.mind.domain.repository.UnlockSessionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlin.math.min

class PrepareEmergencyUnlock @Inject constructor(
    private val policies: EmergencyPolicyRepository,
    private val sessions: UnlockSessionRepository,
) {
    suspend operator fun invoke(): EmergencyPreview {
        val policy = policies.policy.first()
        val balance = sessions.currentBalance()
        val configured = if (policy.penaltyMode == EmergencyPenaltyMode.FIXED_POINTS) {
            policy.fixedPenaltyPoints
        } else 0
        return EmergencyPreview(policy, balance, min(balance, configured))
    }
}

class CreateEmergencyUnlock @Inject constructor(
    private val repository: EmergencyUnlockRepository,
    private val policies: EmergencyPolicyRepository,
    private val clock: ClockProvider,
    private val logicalDayResolver: LogicalDayResolver,
) {
    suspend operator fun invoke(packageName: String, reason: String?) = repository.create(
        EmergencyUnlockRequest(
            packageName = packageName,
            reason = reason?.trim()?.take(500)?.ifBlank { null },
            policy = policies.policy.first(),
            createdAt = clock.now(),
            logicalDay = logicalDayResolver.resolve(clock.now(), clock.zoneId()),
        ),
    )
}

class HoldConfirmationGate(private val requiredMillis: Long = 3_000) {
    private var startedAt: Long? = null
    fun start(atMillis: Long) { startedAt = atMillis }
    fun release(atMillis: Long): Boolean {
        val confirmed = startedAt?.let { atMillis - it >= requiredMillis } == true
        startedAt = null
        return confirmed
    }
    fun cancel() { startedAt = null }
}

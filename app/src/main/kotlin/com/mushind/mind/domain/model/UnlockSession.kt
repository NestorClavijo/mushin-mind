package com.mushind.mind.domain.model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

data class UnlockSession(
    val id: String,
    val packageName: String,
    val type: UnlockSessionKind,
    val appliedRuleType: AppRuleType,
    val startsAt: Instant,
    val endsAt: Instant,
    val logicalDay: LocalDate,
    val costPoints: Int,
    val status: UnlockSessionState = UnlockSessionState.ACTIVE,
) {
    init {
        require(id.isNotBlank()) { "Session id cannot be blank" }
        require(packageName.isNotBlank()) { "Package name cannot be blank" }
        require(endsAt > startsAt) { "Session must end after it starts" }
        require(costPoints > 0) { "Session cost must be positive" }
    }

    fun isActiveAt(instant: Instant): Boolean =
        status == UnlockSessionState.ACTIVE && startsAt <= instant && endsAt > instant

    fun remainingAt(instant: Instant): Duration =
        if (isActiveAt(instant)) Duration.between(instant, endsAt) else Duration.ZERO
}

enum class UnlockSessionKind {
    TEMPORARY,
    UNTIL_END_OF_DAY,
}

enum class UnlockSessionState {
    ACTIVE,
    EXPIRED,
    ENDED,
    CANCELLED,
}

sealed interface AccessDecision {
    data class Allowed(val session: UnlockSession? = null) : AccessDecision

    data class BlockedInsufficientPoints(
        val costPoints: Int,
        val balance: Int,
        val missingPoints: Int,
    ) : AccessDecision

    data class BlockedPurchasable(
        val rule: AppRule,
        val balance: Int,
    ) : AccessDecision
}

package com.mushind.mind.domain.model

import java.time.Instant

enum class EmergencyPenaltyMode { NONE, FIXED_POINTS }

data class EmergencyPolicy(
    val durationMinutes: Int = 10,
    val penaltyMode: EmergencyPenaltyMode = EmergencyPenaltyMode.FIXED_POINTS,
    val fixedPenaltyPoints: Int = 20,
) {
    init {
        require(durationMinutes in 1..60)
        require(fixedPenaltyPoints in 0..500)
    }
}

data class EmergencyUnlock(
    val id: String,
    val sessionId: String,
    val packageName: String,
    val reason: String?,
    val durationMinutes: Int,
    val configuredPenaltyPoints: Int,
    val appliedPenaltyPoints: Int,
    val balanceBefore: Int,
    val balanceAfter: Int,
    val createdAt: Instant,
)

data class EmergencyPreview(
    val policy: EmergencyPolicy,
    val balance: Int,
    val appliedPenaltyPoints: Int,
)


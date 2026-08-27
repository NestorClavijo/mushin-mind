package com.mushind.mind.domain.model

import java.time.Instant

data class PointTransaction(
    val id: String,
    val type: PointTransactionType,
    val amount: Int,
    val referenceId: String,
    val description: String,
    val createdAt: Instant,
) {
    init {
        require(amount != 0) { "A point transaction cannot have a zero amount" }
        require(referenceId.isNotBlank()) { "A point transaction needs a reference" }
    }
}

enum class PointTransactionType {
    TASK_REWARD,
    APP_UNLOCK,
    EMERGENCY_PENALTY,
    CORRECTION,
}


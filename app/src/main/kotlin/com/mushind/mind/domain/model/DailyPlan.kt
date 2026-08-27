package com.mushind.mind.domain.model

import java.time.Instant
import java.time.LocalDate

data class DailyPlan(
    val id: String,
    val date: LocalDate,
    val status: DailyPlanStatus,
    val createdAt: Instant,
    val confirmedAt: Instant? = null,
    val closedAt: Instant? = null,
)

enum class DailyPlanStatus {
    DRAFT,
    CONFIRMED,
    ACTIVE,
    CLOSED,
}


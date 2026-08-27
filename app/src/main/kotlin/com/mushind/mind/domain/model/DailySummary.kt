package com.mushind.mind.domain.model

import java.time.Instant
import java.time.LocalDate

data class DailySummary(
    val day: LocalDate,
    val plannedTasks: Int,
    val completedTasks: Int,
    val skippedTasks: Int,
    val cancelledTasks: Int,
    val pointsEarned: Int,
    val pointsSpent: Int,
    val createdAt: Instant,
)


package com.mushind.mind.domain.model

import java.time.Instant
import java.time.LocalDate

data class Task(
    val id: String,
    val planId: String,
    val title: String,
    val description: String? = null,
    val rewardPoints: Int,
    val plannedDate: LocalDate,
    val status: TaskStatus = TaskStatus.PENDING,
    val origin: TaskOrigin = TaskOrigin.PLANNED,
    val generatesPoints: Boolean = origin == TaskOrigin.PLANNED,
    val createdAt: Instant,
    val completedAt: Instant? = null,
) {
    init {
        require(title.isNotBlank()) { "Task title cannot be blank" }
        require(rewardPoints in MIN_REWARD..MAX_REWARD) {
            "Task reward must be between $MIN_REWARD and $MAX_REWARD"
        }
        require(status == TaskStatus.COMPLETED || completedAt == null) {
            "Only completed tasks can have a completion timestamp"
        }
    }

    companion object {
        const val MIN_REWARD = 5
        const val MAX_REWARD = 100
    }
}

enum class TaskStatus {
    PENDING,
    COMPLETED,
    SKIPPED,
    CANCELLED,
}

enum class TaskOrigin {
    PLANNED,
    ADDED_TODAY,
}


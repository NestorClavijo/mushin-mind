package com.mushind.mind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey val day: LocalDate,
    val plannedTasks: Int,
    val completedTasks: Int,
    val skippedTasks: Int,
    val cancelledTasks: Int,
    val pointsEarned: Int,
    val pointsSpent: Int,
    val createdAt: Instant,
)


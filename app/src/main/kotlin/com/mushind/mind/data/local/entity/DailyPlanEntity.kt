package com.mushind.mind.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mushind.mind.domain.model.DailyPlanStatus
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "daily_plans",
    indices = [Index(value = ["date"], unique = true)],
)
data class DailyPlanEntity(
    @PrimaryKey val id: String,
    val date: LocalDate,
    val status: DailyPlanStatus,
    val createdAt: Instant,
    val confirmedAt: Instant?,
    val closedAt: Instant?,
)


package com.mushind.mind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mushind.mind.domain.model.TaskOrigin
import com.mushind.mind.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = DailyPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("planId"), Index("plannedDate")],
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val title: String,
    val description: String?,
    val rewardPoints: Int,
    val plannedDate: LocalDate,
    val status: TaskStatus,
    val origin: TaskOrigin,
    val generatesPoints: Boolean,
    val createdAt: Instant,
    val completedAt: Instant?,
)


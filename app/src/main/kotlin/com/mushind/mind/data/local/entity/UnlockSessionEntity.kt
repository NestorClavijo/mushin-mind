package com.mushind.mind.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "unlock_sessions",
    indices = [Index("packageName"), Index("logicalDay"), Index("status")],
)
data class UnlockSessionEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val type: UnlockSessionType,
    val startsAt: Instant,
    val endsAt: Instant,
    val logicalDay: LocalDate,
    val costPoints: Int,
    val status: UnlockSessionStatus,
)

enum class UnlockSessionType {
    TEMPORARY,
    UNTIL_END_OF_DAY,
    EMERGENCY,
}

enum class UnlockSessionStatus {
    ACTIVE,
    EXPIRED,
    ENDED,
    CANCELLED,
}


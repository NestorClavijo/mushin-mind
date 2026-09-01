package com.mushind.mind.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "emergency_unlocks",
    indices = [Index(value = ["sessionId"], unique = true), Index("packageName"), Index("createdAt")],
)
data class EmergencyUnlockEntity(
    @PrimaryKey val id: String,
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


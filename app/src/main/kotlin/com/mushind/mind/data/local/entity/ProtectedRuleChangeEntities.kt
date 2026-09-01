package com.mushind.mind.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.ChallengeAttemptStatus
import com.mushind.mind.domain.model.PendingRuleChangeStatus
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "challenge_attempts",
    indices = [Index("packageName"), Index("status")],
)
data class ChallengeAttemptEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val proposedEnabled: Boolean,
    val proposedRuleType: AppRuleType?,
    val proposedCostPoints: Int?,
    val proposedDurationMinutes: Int?,
    val effectiveDay: LocalDate,
    val requiredQuestions: Int,
    val answeredQuestions: Int,
    val mistakes: Int,
    val startedAt: Instant,
    val minimumCompletesAt: Instant,
    val completedAt: Instant?,
    val status: ChallengeAttemptStatus,
)

@Entity(
    tableName = "pending_rule_changes",
    indices = [
        Index("packageName"),
        Index("status"),
        Index("effectiveDay"),
        Index(value = ["challengeAttemptId"], unique = true),
    ],
)
data class PendingRuleChangeEntity(
    @PrimaryKey val id: String,
    val challengeAttemptId: String,
    val packageName: String,
    val proposedEnabled: Boolean,
    val proposedRuleType: AppRuleType?,
    val proposedCostPoints: Int?,
    val proposedDurationMinutes: Int?,
    val requestedAt: Instant,
    val effectiveDay: LocalDate,
    val status: PendingRuleChangeStatus,
    val appliedAt: Instant?,
    val cancelledAt: Instant?,
)

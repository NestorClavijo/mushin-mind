package com.mushind.mind.domain.model

import java.time.Instant
import java.time.LocalDate

data class PendingRuleChange(
    val id: String,
    val challengeAttemptId: String,
    val packageName: String,
    val proposedEnabled: Boolean,
    val proposedRule: AppRule?,
    val requestedAt: Instant,
    val effectiveDay: LocalDate,
    val status: PendingRuleChangeStatus = PendingRuleChangeStatus.PENDING,
    val appliedAt: Instant? = null,
    val cancelledAt: Instant? = null,
)

enum class PendingRuleChangeStatus { PENDING, APPLIED, CANCELLED, REPLACED }

data class Challenge(
    val attemptId: String,
    val packageName: String,
    val questions: List<ChallengeQuestion>,
    val startedAt: Instant,
    val minimumCompletesAt: Instant,
    val effectiveDay: LocalDate,
) {
    init { require(questions.isNotEmpty()) }
}

data class ChallengeQuestion(
    val id: String,
    val type: ChallengeQuestionType,
    val prompt: String,
    val options: List<Int>,
    val correctAnswer: Int,
) {
    init {
        require(options.size >= 3)
        require(correctAnswer in options)
    }
}

enum class ChallengeQuestionType { ARITHMETIC, SEQUENCE }
enum class ChallengeAttemptStatus { IN_PROGRESS, SUCCEEDED, ABANDONED }

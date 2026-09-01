package com.mushind.mind.domain.usecase

import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.Challenge
import com.mushind.mind.domain.model.ChallengeQuestion
import com.mushind.mind.domain.model.ChallengeQuestionType
import com.mushind.mind.domain.model.LogicalDayResolver
import com.mushind.mind.domain.model.PendingRuleChange
import com.mushind.mind.domain.repository.ProtectedRuleChangeRepository
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.absoluteValue

data class ChallengePolicy(
    val requiredQuestions: Int,
    val minimumDuration: Duration,
) {
    init { require(requiredQuestions > 0); require(!minimumDuration.isNegative) }
}

class ChallengeGenerator @Inject constructor() {
    fun generate(
        attemptId: String,
        packageName: String,
        startedAt: java.time.Instant,
        effectiveDay: LocalDate,
        policy: ChallengePolicy,
    ): Challenge {
        val seed = attemptId.hashCode().absoluteValue
        val questions = List(policy.requiredQuestions) { index ->
            if (index % 2 == 0) arithmetic(seed, index) else sequence(seed, index)
        }
        return Challenge(
            attemptId,
            packageName,
            questions,
            startedAt,
            startedAt.plus(policy.minimumDuration),
            effectiveDay,
        )
    }

    private fun arithmetic(seed: Int, index: Int): ChallengeQuestion {
        val left = 11 + (seed + index * 7) % 38
        val right = 7 + (seed / 3 + index * 11) % 31
        val answer = left + right
        return question(index, ChallengeQuestionType.ARITHMETIC, "$left + $right = ?", answer)
    }

    private fun sequence(seed: Int, index: Int): ChallengeQuestion {
        val start = 2 + (seed + index) % 8
        val step = 2 + (seed / 5 + index) % 6
        val values = List(4) { start + it * step }
        val answer = start + 4 * step
        return question(index, ChallengeQuestionType.SEQUENCE, values.joinToString(", ") + ", ?", answer)
    }

    private fun question(
        index: Int,
        type: ChallengeQuestionType,
        prompt: String,
        answer: Int,
    ): ChallengeQuestion {
        val options = listOf(answer - 2, answer - 1, answer, answer + 1).sortedBy { (it * 31 + index * 17) % 7 }
        return ChallengeQuestion("question-$index", type, prompt, options, answer)
    }
}

class StartProtectedRuleChange @Inject constructor(
    private val repository: ProtectedRuleChangeRepository,
    private val generator: ChallengeGenerator,
    private val policy: ChallengePolicy,
    private val idProvider: IdProvider,
    private val clock: ClockProvider,
    private val logicalDayResolver: LogicalDayResolver,
) {
    suspend operator fun invoke(
        packageName: String,
        proposedEnabled: Boolean,
        proposedRule: AppRule?,
    ): Challenge {
        val now = clock.now()
        val effectiveDay = logicalDayResolver.resolve(now, clock.zoneId()).plusDays(1)
        val challenge = generator.generate(idProvider.newId(), packageName, now, effectiveDay, policy)
        repository.saveAttempt(challenge, proposedEnabled, proposedRule)
        return challenge
    }
}

class CompleteChallenge @Inject constructor(
    private val repository: ProtectedRuleChangeRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(challenge: Challenge, answered: Int, mistakes: Int): Boolean =
        repository.completeAttempt(challenge.attemptId, answered, mistakes, clock.now())
}

class AbandonChallenge @Inject constructor(
    private val repository: ProtectedRuleChangeRepository,
) {
    suspend operator fun invoke(attemptId: String): Boolean = repository.abandonAttempt(attemptId)
}

class ConfirmPendingRuleChange @Inject constructor(
    private val repository: ProtectedRuleChangeRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(attemptId: String): PendingRuleChange? =
        repository.schedulePending(attemptId, clock.now())
}

class CancelPendingRuleChange @Inject constructor(
    private val repository: ProtectedRuleChangeRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(id: String): Boolean = repository.cancelPending(id, clock.now())
}

class ApplyPendingRuleChanges @Inject constructor(
    private val repository: ProtectedRuleChangeRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(day: LocalDate): Int = repository.applyDue(day, clock.now())
}


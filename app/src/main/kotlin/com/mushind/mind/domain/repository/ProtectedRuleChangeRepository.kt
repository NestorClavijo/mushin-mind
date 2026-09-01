package com.mushind.mind.domain.repository

import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.Challenge
import com.mushind.mind.domain.model.PendingRuleChange
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface ProtectedRuleChangeRepository {
    suspend fun saveAttempt(challenge: Challenge, proposedEnabled: Boolean, proposedRule: AppRule?)
    suspend fun completeAttempt(id: String, answered: Int, mistakes: Int, at: Instant): Boolean
    suspend fun abandonAttempt(id: String): Boolean
    suspend fun schedulePending(attemptId: String, requestedAt: Instant): PendingRuleChange?
    fun observePending(): Flow<List<PendingRuleChange>>
    suspend fun cancelPending(id: String, at: Instant): Boolean
    suspend fun applyDue(day: LocalDate, at: Instant): Int
}


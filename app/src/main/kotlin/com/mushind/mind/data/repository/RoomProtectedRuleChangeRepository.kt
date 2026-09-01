package com.mushind.mind.data.repository

import androidx.room.withTransaction
import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.data.local.entity.AppRuleEntity
import com.mushind.mind.data.local.entity.ChallengeAttemptEntity
import com.mushind.mind.data.local.entity.PendingRuleChangeEntity
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.Challenge
import com.mushind.mind.domain.model.ChallengeAttemptStatus
import com.mushind.mind.domain.model.PendingRuleChange
import com.mushind.mind.domain.model.PendingRuleChangeStatus
import com.mushind.mind.domain.repository.ProtectedRuleChangeRepository
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomProtectedRuleChangeRepository @Inject constructor(
    private val database: MindDatabase,
    private val idProvider: IdProvider,
) : ProtectedRuleChangeRepository {
    private val dao = database.protectedRuleChangeDao()

    override suspend fun saveAttempt(
        challenge: Challenge,
        proposedEnabled: Boolean,
        proposedRule: AppRule?,
    ) {
        require(!proposedEnabled || proposedRule != null)
        dao.insertAttempt(
            ChallengeAttemptEntity(
                id = challenge.attemptId,
                packageName = challenge.packageName,
                proposedEnabled = proposedEnabled,
                proposedRuleType = proposedRule?.type,
                proposedCostPoints = proposedRule?.costPoints,
                proposedDurationMinutes = proposedRule?.durationMinutes,
                effectiveDay = challenge.effectiveDay,
                requiredQuestions = challenge.questions.size,
                answeredQuestions = 0,
                mistakes = 0,
                startedAt = challenge.startedAt,
                minimumCompletesAt = challenge.minimumCompletesAt,
                completedAt = null,
                status = ChallengeAttemptStatus.IN_PROGRESS,
            ),
        )
    }

    override suspend fun completeAttempt(id: String, answered: Int, mistakes: Int, at: Instant): Boolean =
        dao.completeAttempt(id, answered, mistakes, at) == 1

    override suspend fun abandonAttempt(id: String): Boolean = dao.abandonAttempt(id) == 1

    override suspend fun schedulePending(attemptId: String, requestedAt: Instant): PendingRuleChange? =
        database.withTransaction {
            dao.getByAttempt(attemptId)?.let { return@withTransaction it.toDomain() }
            val attempt = dao.getAttempt(attemptId)
                ?.takeIf { it.status == ChallengeAttemptStatus.SUCCEEDED }
                ?: return@withTransaction null
            dao.replacePending(attempt.packageName, requestedAt)
            val pending = PendingRuleChangeEntity(
                id = idProvider.newId(),
                challengeAttemptId = attempt.id,
                packageName = attempt.packageName,
                proposedEnabled = attempt.proposedEnabled,
                proposedRuleType = attempt.proposedRuleType,
                proposedCostPoints = attempt.proposedCostPoints,
                proposedDurationMinutes = attempt.proposedDurationMinutes,
                requestedAt = requestedAt,
                effectiveDay = attempt.effectiveDay,
                status = PendingRuleChangeStatus.PENDING,
                appliedAt = null,
                cancelledAt = null,
            )
            dao.insertPending(pending)
            pending.toDomain()
        }

    override fun observePending(): Flow<List<PendingRuleChange>> =
        dao.observePending().map { changes -> changes.map(PendingRuleChangeEntity::toDomain) }

    override suspend fun cancelPending(id: String, at: Instant): Boolean = dao.cancelPending(id, at) == 1

    override suspend fun applyDue(day: LocalDate, at: Instant): Int =
        database.withTransaction { applyDueRuleChanges(database, day, at) }
}

suspend fun applyDueRuleChanges(database: MindDatabase, day: LocalDate, at: Instant): Int {
    val dao = database.protectedRuleChangeDao()
    var appliedCount = 0
    dao.dueChanges(day).forEach { change ->
        val app = dao.getRestrictedApp(change.packageName) ?: return@forEach
        dao.upsertRestrictedApp(app.copy(isEnabled = change.proposedEnabled, updatedAt = at))
        if (change.proposedEnabled && change.proposedRuleType != null && change.proposedCostPoints != null) {
            val existingRule = dao.getRule(change.packageName)
            dao.upsertRule(
                AppRuleEntity(
                    packageName = change.packageName,
                    type = change.proposedRuleType,
                    costPoints = change.proposedCostPoints,
                    durationMinutes = change.proposedDurationMinutes,
                    createdAt = existingRule?.createdAt ?: change.requestedAt,
                    updatedAt = at,
                ),
            )
        }
        appliedCount += dao.markApplied(change.id, at)
    }
    return appliedCount
}

private fun PendingRuleChangeEntity.toDomain(): PendingRuleChange {
    val rule = if (proposedRuleType != null && proposedCostPoints != null) {
        AppRule(
            packageName,
            proposedRuleType,
            proposedCostPoints,
            proposedDurationMinutes,
            requestedAt,
            requestedAt,
        )
    } else {
        null
    }
    return PendingRuleChange(
        id,
        challengeAttemptId,
        packageName,
        proposedEnabled,
        rule,
        requestedAt,
        effectiveDay,
        status,
        appliedAt,
        cancelledAt,
    )
}


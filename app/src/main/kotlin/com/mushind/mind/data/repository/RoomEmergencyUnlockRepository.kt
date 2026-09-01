package com.mushind.mind.data.repository

import androidx.room.withTransaction
import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.data.local.entity.EmergencyUnlockEntity
import com.mushind.mind.data.local.entity.PointTransactionEntity
import com.mushind.mind.data.local.entity.UnlockSessionEntity
import com.mushind.mind.data.local.entity.UnlockSessionStatus
import com.mushind.mind.data.local.entity.UnlockSessionType
import com.mushind.mind.data.local.entity.UserProgressEntity
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.EmergencyPenaltyMode
import com.mushind.mind.domain.model.EmergencyUnlock
import com.mushind.mind.domain.model.PointTransactionType
import com.mushind.mind.domain.model.UnlockSession
import com.mushind.mind.domain.model.UnlockSessionKind
import com.mushind.mind.domain.model.UnlockSessionState
import com.mushind.mind.domain.repository.EmergencyUnlockRepository
import com.mushind.mind.domain.repository.EmergencyUnlockRequest
import com.mushind.mind.domain.repository.EmergencyUnlockResult
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.min

class RoomEmergencyUnlockRepository @Inject constructor(
    private val database: MindDatabase,
    private val idProvider: IdProvider,
) : EmergencyUnlockRepository {
    override suspend fun create(request: EmergencyUnlockRequest): EmergencyUnlockResult =
        database.withTransaction {
            val sessionDao = database.unlockSessionDao()
            val eventDao = database.emergencyUnlockDao()
            sessionDao.initializeProgress(UserProgressEntity(balance = 0, xp = 0))
            val balanceBefore = sessionDao.getBalance() ?: 0
            val configuredPenalty = if (request.policy.penaltyMode == EmergencyPenaltyMode.FIXED_POINTS) {
                request.policy.fixedPenaltyPoints
            } else 0
            val appliedPenalty = min(balanceBefore, configuredPenalty)
            if (appliedPenalty > 0) check(sessionDao.debitIfEnough(appliedPenalty) == 1)
            val balanceAfter = sessionDao.getBalance() ?: 0
            val sessionId = idProvider.newId()
            val eventId = idProvider.newId()
            if (appliedPenalty > 0) {
                sessionDao.insertTransaction(
                    PointTransactionEntity(
                        id = idProvider.newId(),
                        type = PointTransactionType.EMERGENCY_PENALTY,
                        amount = -appliedPenalty,
                        referenceId = eventId,
                        description = "Penalización de emergencia para ${request.packageName}",
                        createdAt = request.createdAt,
                    ),
                )
            }
            val sessionEntity = UnlockSessionEntity(
                id = sessionId,
                packageName = request.packageName,
                type = UnlockSessionType.EMERGENCY,
                ruleType = AppRuleType.TEMPORARY_SESSION,
                startsAt = request.createdAt,
                endsAt = request.createdAt.plus(request.policy.durationMinutes.toLong(), ChronoUnit.MINUTES),
                logicalDay = request.logicalDay,
                costPoints = appliedPenalty,
                status = UnlockSessionStatus.ACTIVE,
            )
            sessionDao.insert(sessionEntity)
            val eventEntity = EmergencyUnlockEntity(
                id = eventId,
                sessionId = sessionId,
                packageName = request.packageName,
                reason = request.reason,
                durationMinutes = request.policy.durationMinutes,
                configuredPenaltyPoints = configuredPenalty,
                appliedPenaltyPoints = appliedPenalty,
                balanceBefore = balanceBefore,
                balanceAfter = balanceAfter,
                createdAt = request.createdAt,
            )
            eventDao.insert(eventEntity)
            EmergencyUnlockResult(eventEntity.toDomain(), sessionEntity.toEmergencyDomain())
        }

    override suspend fun get(id: String): EmergencyUnlock? = database.emergencyUnlockDao().get(id)?.toDomain()
}

private fun EmergencyUnlockEntity.toDomain() = EmergencyUnlock(
    id, sessionId, packageName, reason, durationMinutes, configuredPenaltyPoints,
    appliedPenaltyPoints, balanceBefore, balanceAfter, createdAt,
)

private fun UnlockSessionEntity.toEmergencyDomain() = UnlockSession(
    id = id,
    packageName = packageName,
    type = UnlockSessionKind.EMERGENCY,
    appliedRuleType = null,
    startsAt = startsAt,
    endsAt = endsAt,
    logicalDay = logicalDay,
    costPoints = costPoints,
    status = UnlockSessionState.ACTIVE,
)


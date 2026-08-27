package com.mushind.mind.data.repository

import androidx.room.withTransaction
import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.data.local.entity.PointTransactionEntity
import com.mushind.mind.data.local.entity.UnlockSessionEntity
import com.mushind.mind.data.local.entity.UnlockSessionStatus
import com.mushind.mind.data.local.entity.UnlockSessionType
import com.mushind.mind.data.local.entity.UserProgressEntity
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.PointTransactionType
import com.mushind.mind.domain.model.UnlockSession
import com.mushind.mind.domain.model.UnlockSessionKind
import com.mushind.mind.domain.model.UnlockSessionState
import com.mushind.mind.domain.repository.SessionPurchaseRequest
import com.mushind.mind.domain.repository.SessionPurchaseResult
import com.mushind.mind.domain.repository.UnlockSessionRepository
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class RoomUnlockSessionRepository @Inject constructor(
    private val database: MindDatabase,
    private val idProvider: IdProvider,
) : UnlockSessionRepository {
    private val dao = database.unlockSessionDao()

    override suspend fun currentBalance(): Int = dao.getBalance() ?: 0

    override suspend fun getActiveSession(packageName: String, at: java.time.Instant): UnlockSession? =
        dao.getActiveSession(packageName, at)?.toDomain()

    override suspend fun purchase(request: SessionPurchaseRequest): SessionPurchaseResult =
        database.withTransaction {
            dao.expireSessions(request.purchasedAt)
            dao.initializeProgress(UserProgressEntity(balance = 0, xp = 0))
            val balance = dao.getBalance() ?: 0
            if (balance < request.rule.costPoints || dao.debitIfEnough(request.rule.costPoints) != 1) {
                return@withTransaction SessionPurchaseResult.InsufficientPoints(
                    balance = dao.getBalance() ?: balance,
                    costPoints = request.rule.costPoints,
                )
            }

            val sessionType = request.rule.type.toSessionType()
            val existing = if (sessionType == UnlockSessionType.TEMPORARY) {
                dao.getActiveSessionOfType(
                    request.rule.packageName,
                    request.purchasedAt,
                    UnlockSessionType.TEMPORARY,
                )
            } else {
                null
            }
            val sessionId = existing?.id ?: idProvider.newId()
            dao.insertTransaction(
                PointTransactionEntity(
                    id = idProvider.newId(),
                    type = PointTransactionType.APP_UNLOCK,
                    amount = -request.rule.costPoints,
                    referenceId = sessionId,
                    description = "Acceso a ${request.rule.packageName}",
                    createdAt = request.purchasedAt,
                ),
            )

            val saved = if (existing != null) {
                val newEnd = existing.endsAt.plus(
                    requireNotNull(request.rule.durationMinutes).toLong(),
                    ChronoUnit.MINUTES,
                )
                check(dao.extend(existing.id, newEnd, request.rule.costPoints) == 1)
                existing.copy(endsAt = newEnd, costPoints = existing.costPoints + request.rule.costPoints)
            } else {
                val end = when (sessionType) {
                    UnlockSessionType.TEMPORARY -> request.purchasedAt.plus(
                        requireNotNull(request.rule.durationMinutes).toLong(),
                        ChronoUnit.MINUTES,
                    )
                    UnlockSessionType.UNTIL_END_OF_DAY -> request.logicalDayEnd
                    UnlockSessionType.EMERGENCY -> error("Emergency sessions are created by a later phase")
                }
                val entity = UnlockSessionEntity(
                    id = sessionId,
                    packageName = request.rule.packageName,
                    type = sessionType,
                    ruleType = request.rule.type,
                    startsAt = request.purchasedAt,
                    endsAt = end,
                    logicalDay = request.logicalDay,
                    costPoints = request.rule.costPoints,
                    status = UnlockSessionStatus.ACTIVE,
                )
                dao.insert(entity)
                entity
            }

            SessionPurchaseResult.Purchased(saved.toDomain(), dao.getBalance() ?: 0)
        }

    override suspend fun expireSessions(at: java.time.Instant): Int = dao.expireSessions(at)

    override suspend fun endSessionEarly(sessionId: String, endedAt: java.time.Instant): Boolean =
        dao.endEarly(sessionId, endedAt) == 1
}

private fun AppRuleType.toSessionType(): UnlockSessionType = when (this) {
    AppRuleType.TEMPORARY_SESSION,
    AppRuleType.PURCHASABLE_TIME,
    -> UnlockSessionType.TEMPORARY
    AppRuleType.UNTIL_END_OF_DAY -> UnlockSessionType.UNTIL_END_OF_DAY
}

private fun UnlockSessionEntity.toDomain() = UnlockSession(
    id = id,
    packageName = packageName,
    type = when (type) {
        UnlockSessionType.TEMPORARY -> UnlockSessionKind.TEMPORARY
        UnlockSessionType.UNTIL_END_OF_DAY -> UnlockSessionKind.UNTIL_END_OF_DAY
        UnlockSessionType.EMERGENCY -> UnlockSessionKind.TEMPORARY
    },
    appliedRuleType = ruleType,
    startsAt = startsAt,
    endsAt = endsAt,
    logicalDay = logicalDay,
    costPoints = costPoints,
    status = when (status) {
        UnlockSessionStatus.ACTIVE -> UnlockSessionState.ACTIVE
        UnlockSessionStatus.EXPIRED -> UnlockSessionState.EXPIRED
        UnlockSessionStatus.ENDED -> UnlockSessionState.ENDED
        UnlockSessionStatus.CANCELLED -> UnlockSessionState.CANCELLED
    },
)

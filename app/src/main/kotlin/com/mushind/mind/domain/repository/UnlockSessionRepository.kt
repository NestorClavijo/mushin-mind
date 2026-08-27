package com.mushind.mind.domain.repository

import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.UnlockSession
import java.time.Instant
import java.time.LocalDate

data class SessionPurchaseRequest(
    val rule: AppRule,
    val purchasedAt: Instant,
    val logicalDay: LocalDate,
    val logicalDayEnd: Instant,
)

sealed interface SessionPurchaseResult {
    data class Purchased(
        val session: UnlockSession,
        val remainingBalance: Int,
    ) : SessionPurchaseResult

    data class InsufficientPoints(
        val balance: Int,
        val costPoints: Int,
    ) : SessionPurchaseResult
}

interface UnlockSessionRepository {
    suspend fun currentBalance(): Int
    suspend fun getActiveSession(packageName: String, at: Instant): UnlockSession?
    suspend fun purchase(request: SessionPurchaseRequest): SessionPurchaseResult
    suspend fun expireSessions(at: Instant): Int
    suspend fun endSessionEarly(sessionId: String, endedAt: Instant): Boolean
}


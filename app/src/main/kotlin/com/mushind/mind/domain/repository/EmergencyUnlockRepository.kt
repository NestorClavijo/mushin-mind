package com.mushind.mind.domain.repository

import com.mushind.mind.domain.model.EmergencyPolicy
import com.mushind.mind.domain.model.EmergencyUnlock
import com.mushind.mind.domain.model.UnlockSession
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface EmergencyPolicyRepository {
    val policy: Flow<EmergencyPolicy>
    suspend fun setDurationMinutes(minutes: Int)
    suspend fun setFixedPenalty(points: Int)
}

data class EmergencyUnlockRequest(
    val packageName: String,
    val reason: String?,
    val policy: EmergencyPolicy,
    val createdAt: Instant,
    val logicalDay: LocalDate,
)

data class EmergencyUnlockResult(
    val event: EmergencyUnlock,
    val session: UnlockSession,
)

interface EmergencyUnlockRepository {
    suspend fun create(request: EmergencyUnlockRequest): EmergencyUnlockResult
    suspend fun get(id: String): EmergencyUnlock?
}


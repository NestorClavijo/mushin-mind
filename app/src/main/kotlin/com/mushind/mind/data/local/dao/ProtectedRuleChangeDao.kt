package com.mushind.mind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mushind.mind.data.local.entity.AppRuleEntity
import com.mushind.mind.data.local.entity.ChallengeAttemptEntity
import com.mushind.mind.data.local.entity.PendingRuleChangeEntity
import com.mushind.mind.data.local.entity.RestrictedAppEntity
import com.mushind.mind.domain.model.ChallengeAttemptStatus
import com.mushind.mind.domain.model.PendingRuleChangeStatus
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtectedRuleChangeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAttempt(attempt: ChallengeAttemptEntity)

    @Query("SELECT * FROM challenge_attempts WHERE id = :id")
    suspend fun getAttempt(id: String): ChallengeAttemptEntity?

    @Query(
        """UPDATE challenge_attempts SET status = :succeeded, answeredQuestions = :answered,
        mistakes = :mistakes, completedAt = :completedAt
        WHERE id = :id AND status = :inProgress AND requiredQuestions <= :answered
        AND minimumCompletesAt <= :completedAt""",
    )
    suspend fun completeAttempt(
        id: String,
        answered: Int,
        mistakes: Int,
        completedAt: Instant,
        inProgress: ChallengeAttemptStatus = ChallengeAttemptStatus.IN_PROGRESS,
        succeeded: ChallengeAttemptStatus = ChallengeAttemptStatus.SUCCEEDED,
    ): Int

    @Query(
        """UPDATE challenge_attempts SET status = :abandoned
        WHERE id = :id AND status = :inProgress""",
    )
    suspend fun abandonAttempt(
        id: String,
        inProgress: ChallengeAttemptStatus = ChallengeAttemptStatus.IN_PROGRESS,
        abandoned: ChallengeAttemptStatus = ChallengeAttemptStatus.ABANDONED,
    ): Int

    @Query(
        """UPDATE pending_rule_changes SET status = :replaced, cancelledAt = :at
        WHERE packageName = :packageName AND status = :pending""",
    )
    suspend fun replacePending(
        packageName: String,
        at: Instant,
        pending: PendingRuleChangeStatus = PendingRuleChangeStatus.PENDING,
        replaced: PendingRuleChangeStatus = PendingRuleChangeStatus.REPLACED,
    ): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPending(change: PendingRuleChangeEntity)

    @Query("SELECT * FROM pending_rule_changes WHERE status = 'PENDING' ORDER BY requestedAt DESC")
    fun observePending(): Flow<List<PendingRuleChangeEntity>>

    @Query("SELECT * FROM pending_rule_changes WHERE packageName = :packageName AND status = 'PENDING' LIMIT 1")
    suspend fun getPending(packageName: String): PendingRuleChangeEntity?

    @Query("SELECT * FROM pending_rule_changes WHERE challengeAttemptId = :attemptId LIMIT 1")
    suspend fun getByAttempt(attemptId: String): PendingRuleChangeEntity?

    @Query(
        """UPDATE pending_rule_changes SET status = :cancelled, cancelledAt = :at
        WHERE id = :id AND status = :pending""",
    )
    suspend fun cancelPending(
        id: String,
        at: Instant,
        pending: PendingRuleChangeStatus = PendingRuleChangeStatus.PENDING,
        cancelled: PendingRuleChangeStatus = PendingRuleChangeStatus.CANCELLED,
    ): Int

    @Query("SELECT * FROM pending_rule_changes WHERE status = 'PENDING' AND effectiveDay <= :day")
    suspend fun dueChanges(day: LocalDate): List<PendingRuleChangeEntity>

    @Query("SELECT * FROM restricted_apps WHERE packageName = :packageName")
    suspend fun getRestrictedApp(packageName: String): RestrictedAppEntity?

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName")
    suspend fun getRule(packageName: String): AppRuleEntity?

    @Upsert
    suspend fun upsertRestrictedApp(app: RestrictedAppEntity)

    @Upsert
    suspend fun upsertRule(rule: AppRuleEntity)

    @Query(
        """UPDATE pending_rule_changes SET status = :applied, appliedAt = :at
        WHERE id = :id AND status = :pending""",
    )
    suspend fun markApplied(
        id: String,
        at: Instant,
        pending: PendingRuleChangeStatus = PendingRuleChangeStatus.PENDING,
        applied: PendingRuleChangeStatus = PendingRuleChangeStatus.APPLIED,
    ): Int
}

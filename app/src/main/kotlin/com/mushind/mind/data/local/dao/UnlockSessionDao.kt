package com.mushind.mind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mushind.mind.data.local.entity.PointTransactionEntity
import com.mushind.mind.data.local.entity.UnlockSessionEntity
import com.mushind.mind.data.local.entity.UnlockSessionStatus
import com.mushind.mind.data.local.entity.UnlockSessionType
import com.mushind.mind.data.local.entity.UserProgressEntity
import java.time.Instant

@Dao
interface UnlockSessionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: UnlockSessionEntity)

    @Query("SELECT * FROM unlock_sessions WHERE id = :id")
    suspend fun getById(id: String): UnlockSessionEntity?

    @Query(
        """SELECT * FROM unlock_sessions
        WHERE packageName = :packageName AND status = :activeStatus
        AND startsAt <= :at AND endsAt > :at
        ORDER BY endsAt DESC LIMIT 1""",
    )
    suspend fun getActiveSession(
        packageName: String,
        at: Instant,
        activeStatus: UnlockSessionStatus = UnlockSessionStatus.ACTIVE,
    ): UnlockSessionEntity?

    @Query(
        """SELECT * FROM unlock_sessions
        WHERE packageName = :packageName AND status = :activeStatus AND type = :type
        AND startsAt <= :at AND endsAt > :at
        ORDER BY endsAt DESC LIMIT 1""",
    )
    suspend fun getActiveSessionOfType(
        packageName: String,
        at: Instant,
        type: UnlockSessionType,
        activeStatus: UnlockSessionStatus = UnlockSessionStatus.ACTIVE,
    ): UnlockSessionEntity?

    @Query("SELECT balance FROM user_progress WHERE id = 1")
    suspend fun getBalance(): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun initializeProgress(progress: UserProgressEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setProgress(progress: UserProgressEntity)

    @Query("UPDATE user_progress SET balance = balance - :cost WHERE id = 1 AND balance >= :cost")
    suspend fun debitIfEnough(cost: Int): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: PointTransactionEntity)

    @Query(
        """UPDATE unlock_sessions
        SET endsAt = :endsAt, costPoints = costPoints + :additionalCost
        WHERE id = :sessionId AND status = :activeStatus""",
    )
    suspend fun extend(
        sessionId: String,
        endsAt: Instant,
        additionalCost: Int,
        activeStatus: UnlockSessionStatus = UnlockSessionStatus.ACTIVE,
    ): Int

    @Query(
        """UPDATE unlock_sessions SET status = :expiredStatus
        WHERE status = :activeStatus AND endsAt <= :at""",
    )
    suspend fun expireSessions(
        at: Instant,
        activeStatus: UnlockSessionStatus = UnlockSessionStatus.ACTIVE,
        expiredStatus: UnlockSessionStatus = UnlockSessionStatus.EXPIRED,
    ): Int

    @Query(
        """UPDATE unlock_sessions SET status = :endedStatus, endsAt = :endedAt
        WHERE id = :sessionId AND status = :activeStatus
        AND startsAt < :endedAt AND endsAt > :endedAt""",
    )
    suspend fun endEarly(
        sessionId: String,
        endedAt: Instant,
        activeStatus: UnlockSessionStatus = UnlockSessionStatus.ACTIVE,
        endedStatus: UnlockSessionStatus = UnlockSessionStatus.ENDED,
    ): Int

    @Query("SELECT COUNT(*) FROM point_transactions WHERE type = 'APP_UNLOCK'")
    suspend fun unlockTransactionCount(): Int
}

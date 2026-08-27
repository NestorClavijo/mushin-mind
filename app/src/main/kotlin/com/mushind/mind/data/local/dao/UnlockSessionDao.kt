package com.mushind.mind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mushind.mind.data.local.entity.UnlockSessionEntity

@Dao
interface UnlockSessionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: UnlockSessionEntity)

    @Query("SELECT * FROM unlock_sessions WHERE id = :id")
    suspend fun getById(id: String): UnlockSessionEntity?
}

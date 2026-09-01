package com.mushind.mind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mushind.mind.data.local.entity.EmergencyUnlockEntity

@Dao
interface EmergencyUnlockDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(event: EmergencyUnlockEntity)

    @Query("SELECT * FROM emergency_unlocks WHERE id = :id")
    suspend fun get(id: String): EmergencyUnlockEntity?
}


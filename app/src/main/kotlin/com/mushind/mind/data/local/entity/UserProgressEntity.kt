package com.mushind.mind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val balance: Int,
    val xp: Int,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}


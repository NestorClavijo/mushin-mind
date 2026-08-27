package com.mushind.mind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_cycle_state")
data class DailyCycleStateEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val lastReconciledDay: LocalDate,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}


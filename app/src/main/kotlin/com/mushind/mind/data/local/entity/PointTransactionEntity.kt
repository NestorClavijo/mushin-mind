package com.mushind.mind.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mushind.mind.domain.model.PointTransactionType
import java.time.Instant

@Entity(
    tableName = "point_transactions",
    indices = [Index("referenceId"), Index("createdAt")],
)
data class PointTransactionEntity(
    @PrimaryKey val id: String,
    val type: PointTransactionType,
    val amount: Int,
    val referenceId: String,
    val description: String,
    val createdAt: Instant,
)


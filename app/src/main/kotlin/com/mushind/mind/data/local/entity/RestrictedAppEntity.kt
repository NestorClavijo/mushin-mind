package com.mushind.mind.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.mushind.mind.domain.model.AppRuleType
import java.time.Instant

@Entity(tableName = "restricted_apps")
data class RestrictedAppEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val isEnabled: Boolean,
    val isCritical: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Entity(
    tableName = "app_rules",
    foreignKeys = [
        ForeignKey(
            entity = RestrictedAppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class AppRuleEntity(
    @PrimaryKey val packageName: String,
    val type: AppRuleType,
    val costPoints: Int,
    val durationMinutes: Int?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class RestrictedAppWithRule(
    @Embedded val app: RestrictedAppEntity,
    @Relation(parentColumn = "packageName", entityColumn = "packageName")
    val rule: AppRuleEntity?,
)


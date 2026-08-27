package com.mushind.mind.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mushind.mind.data.local.entity.AppRuleEntity
import com.mushind.mind.data.local.entity.RestrictedAppEntity
import com.mushind.mind.data.local.entity.RestrictedAppWithRule
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRulesDao {
    @Transaction
    @Query("SELECT * FROM restricted_apps ORDER BY displayName COLLATE NOCASE")
    fun observeAll(): Flow<List<RestrictedAppWithRule>>

    @Transaction
    @Query("SELECT * FROM restricted_apps WHERE packageName = :packageName")
    suspend fun get(packageName: String): RestrictedAppWithRule?

    @Upsert
    suspend fun upsertApp(app: RestrictedAppEntity)

    @Upsert
    suspend fun upsertRule(rule: AppRuleEntity)

    @Transaction
    suspend fun save(app: RestrictedAppEntity, rule: AppRuleEntity?) {
        upsertApp(app)
        if (rule != null) upsertRule(rule)
    }
}


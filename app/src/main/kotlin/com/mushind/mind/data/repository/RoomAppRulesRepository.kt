package com.mushind.mind.data.repository

import com.mushind.mind.data.local.dao.AppRulesDao
import com.mushind.mind.data.local.entity.AppRuleEntity
import com.mushind.mind.data.local.entity.RestrictedAppEntity
import com.mushind.mind.data.local.entity.RestrictedAppWithRule
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.RestrictedApp
import com.mushind.mind.domain.repository.AppRulesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomAppRulesRepository @Inject constructor(
    private val dao: AppRulesDao,
) : AppRulesRepository {
    override fun observeRestrictedApps(): Flow<List<RestrictedApp>> =
        dao.observeAll().map { apps -> apps.map(RestrictedAppWithRule::toDomain) }

    override suspend fun getRestrictedApp(packageName: String): RestrictedApp? =
        dao.get(packageName)?.toDomain()

    override suspend fun saveRestrictedApp(app: RestrictedApp) =
        dao.save(app.toEntity(), app.rule?.toEntity())
}

private fun RestrictedAppWithRule.toDomain() = RestrictedApp(
    packageName = app.packageName,
    displayName = app.displayName,
    isEnabled = app.isEnabled,
    isCritical = app.isCritical,
    rule = rule?.let {
        AppRule(it.packageName, it.type, it.costPoints, it.durationMinutes, it.createdAt, it.updatedAt)
    },
    createdAt = app.createdAt,
    updatedAt = app.updatedAt,
)

private fun RestrictedApp.toEntity() = RestrictedAppEntity(
    packageName, displayName, isEnabled, isCritical, createdAt, updatedAt,
)

private fun AppRule.toEntity() = AppRuleEntity(
    packageName, type, costPoints, durationMinutes, createdAt, updatedAt,
)


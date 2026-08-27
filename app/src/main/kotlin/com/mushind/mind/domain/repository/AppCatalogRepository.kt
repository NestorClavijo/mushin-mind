package com.mushind.mind.domain.repository

import com.mushind.mind.domain.model.InstalledApplication
import com.mushind.mind.domain.model.RestrictedApp
import kotlinx.coroutines.flow.Flow

interface AppCatalogRepository {
    val ownPackageName: String
    suspend fun getInstalledApps(): List<InstalledApplication>
}

interface AppRulesRepository {
    fun observeRestrictedApps(): Flow<List<RestrictedApp>>
    suspend fun getRestrictedApp(packageName: String): RestrictedApp?
    suspend fun saveRestrictedApp(app: RestrictedApp)
}

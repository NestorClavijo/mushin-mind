package com.mushind.mind.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager
import com.mushind.mind.domain.model.InstalledApplication
import com.mushind.mind.domain.repository.AppCatalogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidAppCatalogRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AppCatalogRepository {
    override val ownPackageName: String = context.packageName

    override suspend fun getInstalledApps(): List<InstalledApplication> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }
        val defaultDialer = context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage

        activities.asSequence()
            .mapNotNull { resolveInfo ->
                val info = resolveInfo.activityInfo?.applicationInfo ?: return@mapNotNull null
                val packageName = info.packageName
                if (packageName == ownPackageName) return@mapNotNull null
                InstalledApplication(
                    packageName = packageName,
                    displayName = packageManager.getApplicationLabel(info).toString(),
                    isSystemApp = info.flags and ApplicationInfo.FLAG_SYSTEM != 0,
                    isCritical = packageName == defaultDialer ||
                        packageName == Settings.AUTHORITY ||
                        packageName == "com.android.settings" ||
                        packageName.contains("authenticator", ignoreCase = true),
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.displayName.lowercase() }
            .toList()
    }
}

package com.mushind.mind.platform.accessibility

class ForegroundAppPolicy(
    private val ownPackageName: String,
    private val homePackages: Set<String>,
    private val exemptPackages: Set<String> = emptySet(),
) {
    fun shouldEvaluate(packageName: String): Boolean =
            packageName != ownPackageName &&
            packageName !in homePackages &&
            packageName !in exemptPackages &&
            packageName !in ALWAYS_IGNORED_PACKAGES

    fun shouldDismissShield(packageName: String): Boolean =
        packageName in homePackages || packageName in exemptPackages || packageName in ALWAYS_IGNORED_PACKAGES

    private companion object {
        val ALWAYS_IGNORED_PACKAGES = setOf(
            "android",
            "com.android.systemui",
            "com.google.android.permissioncontroller",
            "com.android.permissioncontroller",
            "com.android.server.telecom",
        )
    }
}

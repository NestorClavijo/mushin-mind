package com.mushind.mind.domain.model

import java.time.Instant

const val MIN_RULE_POINTS = 5
const val MAX_RULE_POINTS = 500
const val MIN_RULE_MINUTES = 5
const val MAX_RULE_MINUTES = 240

data class AppRule(
    val packageName: String,
    val type: AppRuleType,
    val costPoints: Int,
    val durationMinutes: Int? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(packageName.isNotBlank()) { "Package name cannot be blank" }
        require(costPoints in MIN_RULE_POINTS..MAX_RULE_POINTS) {
            "Cost must be between $MIN_RULE_POINTS and $MAX_RULE_POINTS points"
        }
        when (type) {
            AppRuleType.UNTIL_END_OF_DAY -> require(durationMinutes == null) {
                "Until-end-of-day rules cannot have a duration"
            }
            AppRuleType.TEMPORARY_SESSION,
            AppRuleType.PURCHASABLE_TIME,
            -> require(durationMinutes in MIN_RULE_MINUTES..MAX_RULE_MINUTES) {
                "Duration must be between $MIN_RULE_MINUTES and $MAX_RULE_MINUTES minutes"
            }
        }
    }
}

enum class AppRuleType {
    TEMPORARY_SESSION,
    UNTIL_END_OF_DAY,
    PURCHASABLE_TIME,
}

data class RestrictedApp(
    val packageName: String,
    val displayName: String,
    val isEnabled: Boolean,
    val isCritical: Boolean,
    val rule: AppRule?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class InstalledApplication(
    val packageName: String,
    val displayName: String,
    val isSystemApp: Boolean,
    val isCritical: Boolean,
    val restriction: RestrictedApp? = null,
)

enum class RuleStrictness {
    STRICTER,
    EQUIVALENT,
    MORE_PERMISSIVE,
}


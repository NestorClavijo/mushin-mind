package com.mushind.mind.domain.usecase

import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.InstalledApplication
import com.mushind.mind.domain.model.RestrictedApp
import com.mushind.mind.domain.model.RuleStrictness
import com.mushind.mind.domain.repository.AppCatalogRepository
import com.mushind.mind.domain.repository.AppRulesRepository
import javax.inject.Inject

class GetInstalledApps @Inject constructor(
    private val catalogRepository: AppCatalogRepository,
) {
    suspend operator fun invoke(query: String = ""): List<InstalledApplication> {
        val normalizedQuery = query.trim()
        return catalogRepository.getInstalledApps()
            .asSequence()
            .filterNot { it.packageName == catalogRepository.ownPackageName }
            .filter { normalizedQuery.isEmpty() || it.displayName.contains(normalizedQuery, ignoreCase = true) }
            .sortedBy { it.displayName.lowercase() }
            .toList()
    }
}

class CompareRuleStrictness @Inject constructor() {
    operator fun invoke(previous: AppRule, proposed: AppRule): RuleStrictness {
        if (previous == proposed || sameConfiguration(previous, proposed)) return RuleStrictness.EQUIVALENT

        if (previous.type != proposed.type) {
            return compareDifferentTypes(previous, proposed)
        }

        val costIsNotLower = proposed.costPoints >= previous.costPoints
        val costIsHigher = proposed.costPoints > previous.costPoints
        val durationIsNotLonger = when {
            previous.durationMinutes == null && proposed.durationMinutes == null -> true
            previous.durationMinutes != null && proposed.durationMinutes != null ->
                proposed.durationMinutes <= previous.durationMinutes
            else -> false
        }
        val durationIsShorter = previous.durationMinutes != null &&
            proposed.durationMinutes != null &&
            proposed.durationMinutes < previous.durationMinutes

        return if (costIsNotLower && durationIsNotLonger && (costIsHigher || durationIsShorter)) {
            RuleStrictness.STRICTER
        } else {
            RuleStrictness.MORE_PERMISSIVE
        }
    }

    private fun compareDifferentTypes(previous: AppRule, proposed: AppRule): RuleStrictness {
        val dailyToTemporary = previous.type == AppRuleType.UNTIL_END_OF_DAY &&
            proposed.type == AppRuleType.TEMPORARY_SESSION
        return if (dailyToTemporary && proposed.costPoints >= previous.costPoints) {
            RuleStrictness.STRICTER
        } else {
            // Mixed or incomparable changes require the same protection as a relaxation.
            RuleStrictness.MORE_PERMISSIVE
        }
    }

    private fun sameConfiguration(first: AppRule, second: AppRule): Boolean =
        first.type == second.type &&
            first.costPoints == second.costPoints &&
            first.durationMinutes == second.durationMinutes
}

sealed interface RuleChangeResult {
    data object Saved : RuleChangeResult
    data object RequiresChallenge : RuleChangeResult
}

class CreateAppRule @Inject constructor(
    private val repository: AppRulesRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(
        app: InstalledApplication,
        type: AppRuleType,
        costPoints: Int,
        durationMinutes: Int?,
    ): RuleChangeResult {
        val existing = repository.getRestrictedApp(app.packageName)
        if (existing?.rule != null) return RuleChangeResult.RequiresChallenge
        val now = clock.now()
        val rule = AppRule(app.packageName, type, costPoints, durationMinutes, now, now)
        repository.saveRestrictedApp(
            RestrictedApp(app.packageName, app.displayName, true, app.isCritical, rule, now, now),
        )
        return RuleChangeResult.Saved
    }
}

class UpdateAppRule @Inject constructor(
    private val repository: AppRulesRepository,
    private val compare: CompareRuleStrictness,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(current: RestrictedApp, proposed: AppRule): RuleChangeResult {
        val previous = current.rule ?: return RuleChangeResult.RequiresChallenge
        return when (compare(previous, proposed)) {
            RuleStrictness.STRICTER,
            RuleStrictness.EQUIVALENT,
            -> {
                val now = clock.now()
                repository.saveRestrictedApp(
                    current.copy(
                        isEnabled = true,
                        rule = proposed.copy(createdAt = previous.createdAt, updatedAt = now),
                        updatedAt = now,
                    ),
                )
                RuleChangeResult.Saved
            }
            RuleStrictness.MORE_PERMISSIVE -> RuleChangeResult.RequiresChallenge
        }
    }
}

class EnableRestriction @Inject constructor(
    private val createAppRule: CreateAppRule,
) {
    suspend operator fun invoke(
        app: InstalledApplication,
        type: AppRuleType,
        costPoints: Int,
        durationMinutes: Int?,
    ) = createAppRule(app, type, costPoints, durationMinutes)
}

class DisableRestriction @Inject constructor() {
    operator fun invoke(restrictedApp: RestrictedApp): RuleChangeResult =
        if (restrictedApp.isEnabled) RuleChangeResult.RequiresChallenge else RuleChangeResult.Saved
}

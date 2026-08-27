package com.mushind.mind.platform.accessibility

import com.mushind.mind.domain.model.AccessDecision
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.RestrictedApp

sealed interface ShieldState {
    val app: RestrictedApp

    data class Blocked(
        override val app: RestrictedApp,
        val costPoints: Int,
        val balance: Int,
        val missingPoints: Int,
        val accessDescription: String,
        val canPurchase: Boolean,
    ) : ShieldState

    data class Confirming(
        override val app: RestrictedApp,
        val rule: AppRule,
        val balance: Int,
        val accessDescription: String,
    ) : ShieldState

    data class Starting(override val app: RestrictedApp) : ShieldState
    data class Error(override val app: RestrictedApp, val message: String) : ShieldState
}

fun shieldStateFor(app: RestrictedApp, decision: AccessDecision): ShieldState? = when (decision) {
    is AccessDecision.Allowed -> null
    is AccessDecision.BlockedInsufficientPoints -> ShieldState.Blocked(
        app = app,
        costPoints = decision.costPoints,
        balance = decision.balance,
        missingPoints = decision.missingPoints,
        accessDescription = requireNotNull(app.rule).accessDescription(),
        canPurchase = false,
    )
    is AccessDecision.BlockedPurchasable -> ShieldState.Blocked(
        app = app,
        costPoints = decision.rule.costPoints,
        balance = decision.balance,
        missingPoints = 0,
        accessDescription = decision.rule.accessDescription(),
        canPurchase = true,
    )
}

fun AppRule.accessDescription(): String = when (type) {
    AppRuleType.TEMPORARY_SESSION -> "$durationMinutes minutos"
    AppRuleType.UNTIL_END_OF_DAY -> "Hasta terminar el día"
    AppRuleType.PURCHASABLE_TIME -> "$durationMinutes minutos acumulables"
}


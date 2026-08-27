package com.mushind.mind.platform.accessibility

import com.mushind.mind.domain.model.AccessDecision
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.RestrictedApp
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShieldPolicyTest {
    private val now = Instant.parse("2026-08-26T15:00:00Z")
    private val rule = AppRule(
        "com.example.social",
        AppRuleType.TEMPORARY_SESSION,
        30,
        20,
        now,
        now,
    )
    private val app = RestrictedApp(
        rule.packageName, "Social", true, false, rule, now, now,
    )

    @Test
    fun `policy evaluates ordinary apps only`() {
        val policy = ForegroundAppPolicy(
            ownPackageName = "com.mushind.mind",
            homePackages = setOf("com.example.launcher"),
            exemptPackages = setOf("com.example.dialer"),
        )

        assertTrue(policy.shouldEvaluate("com.example.social"))
        assertFalse(policy.shouldEvaluate("com.mushind.mind"))
        assertFalse(policy.shouldEvaluate("com.example.launcher"))
        assertFalse(policy.shouldEvaluate("com.example.dialer"))
        assertFalse(policy.shouldEvaluate("com.android.systemui"))
    }

    @Test
    fun `launcher system UI and calls dismiss shield`() {
        val policy = ForegroundAppPolicy(
            "com.mushind.mind",
            setOf("com.example.launcher"),
            setOf("com.example.dialer"),
        )

        assertTrue(policy.shouldDismissShield("com.example.launcher"))
        assertTrue(policy.shouldDismissShield("com.example.dialer"))
        assertTrue(policy.shouldDismissShield("com.android.systemui"))
        assertFalse(policy.shouldDismissShield("com.example.social"))
    }

    @Test
    fun `insufficient decision exposes balance and missing points`() {
        val state = shieldStateFor(
            app,
            AccessDecision.BlockedInsufficientPoints(30, 18, 12),
        ) as ShieldState.Blocked

        assertEquals(30, state.costPoints)
        assertEquals(18, state.balance)
        assertEquals(12, state.missingPoints)
        assertFalse(state.canPurchase)
    }

    @Test
    fun `purchasable decision enables confirmation with rule summary`() {
        val state = shieldStateFor(app, AccessDecision.BlockedPurchasable(rule, 40))
            as ShieldState.Blocked

        assertTrue(state.canPurchase)
        assertEquals("20 minutos", state.accessDescription)
    }

    @Test
    fun `allowed decision does not create shield`() {
        assertNull(shieldStateFor(app, AccessDecision.Allowed()))
    }
}


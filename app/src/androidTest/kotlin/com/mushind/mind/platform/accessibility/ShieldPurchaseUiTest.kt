package com.mushind.mind.platform.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.RestrictedApp
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShieldPurchaseUiTest {
    @Test
    fun confirmingPurchaseForwardsTheConfiguredRuleOnce() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = TestAccessibilityService().apply { attach(context) }
        val now = Instant.parse("2026-09-01T10:00:00Z")
        val rule = AppRule("com.example.focus", AppRuleType.TEMPORARY_SESSION, 30, 15, now, now)
        val app = RestrictedApp(rule.packageName, "Focus", true, false, rule, now, now)
        var purchased: AppRule? = null
        lateinit var root: View

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val controller = ShieldOverlayController(service, { purchased = it }, {}, { _, _ -> }, {}, {})
            root = controller.buildView(ShieldState.Confirming(app, rule, 50, "15 min"))
            root.findButton("Confirmar gasto").performClick()
        }

        assertEquals(rule, purchased)
        assertEquals(false, root.findButton("Confirmar gasto").isEnabled)
    }

    private fun View.findButton(label: String): Button {
        if (this is Button && text.toString() == label) return this
        if (this is ViewGroup) {
            for (index in 0 until childCount) {
                runCatching { return getChildAt(index).findButton(label) }
            }
        }
        error("Button not found: $label")
    }

    private class TestAccessibilityService : AccessibilityService() {
        fun attach(context: Context) = attachBaseContext(context)
        override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
        override fun onInterrupt() = Unit
    }
}

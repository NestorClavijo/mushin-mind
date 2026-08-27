package com.mushind.mind.platform.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.TelecomManager
import android.view.accessibility.AccessibilityEvent
import com.mushind.mind.domain.model.AccessDecision
import com.mushind.mind.domain.model.RestrictedApp
import com.mushind.mind.domain.repository.AppRulesRepository
import com.mushind.mind.domain.repository.SessionPurchaseResult
import com.mushind.mind.domain.usecase.CanUnlockApp
import com.mushind.mind.domain.usecase.PurchaseUnlock
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppRestrictionAccessibilityService : AccessibilityService() {
    @Inject lateinit var appRulesRepository: AppRulesRepository
    @Inject lateinit var canUnlockApp: CanUnlockApp
    @Inject lateinit var purchaseUnlock: PurchaseUnlock

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var foregroundPolicy: ForegroundAppPolicy
    private lateinit var overlayController: ShieldOverlayController
    private var evaluationJob: Job? = null
    private var shieldedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        foregroundPolicy = ForegroundAppPolicy(
            ownPackageName = packageName,
            homePackages = resolveHomePackages(),
            exemptPackages = setOfNotNull(
                getSystemService(TelecomManager::class.java)?.defaultDialerPackage,
            ),
        )
        overlayController = ShieldOverlayController(
            service = this,
            onPurchase = ::purchase,
            onExit = ::exitToHome,
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val foregroundPackage = event.packageName?.toString()?.takeIf(String::isNotBlank) ?: return
        if (!::foregroundPolicy.isInitialized) return
        if (foregroundPackage == packageName) {
            hideShield()
            return
        }

        if (foregroundPolicy.shouldDismissShield(foregroundPackage)) {
            hideShield()
            return
        }
        if (!foregroundPolicy.shouldEvaluate(foregroundPackage)) return
        if (shieldedPackage == foregroundPackage && overlayController.isShowing) return

        evaluationJob?.cancel()
        evaluationJob = serviceScope.launch { evaluate(foregroundPackage) }
    }

    override fun onInterrupt() = hideShield()

    override fun onDestroy() {
        if (::overlayController.isInitialized) overlayController.hide()
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun evaluate(foregroundPackage: String) {
        var restriction: RestrictedApp? = null
        try {
            restriction = appRulesRepository.getRestrictedApp(foregroundPackage)
            val currentRestriction = restriction
            if (currentRestriction == null || !currentRestriction.isEnabled) {
                hideShield()
                return
            }
            when (val decision = canUnlockApp(currentRestriction)) {
                is AccessDecision.Allowed -> hideShield()
                else -> shieldStateFor(currentRestriction, decision)?.let {
                    shieldedPackage = foregroundPackage
                    overlayController.show(it)
                }
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            val currentRestriction = restriction ?: run {
                hideShield()
                return
            }
            shieldedPackage = foregroundPackage
            overlayController.show(
                ShieldState.Error(currentRestriction, "Inténtalo de nuevo o vuelve al inicio."),
            )
        }
    }

    private fun purchase(rule: com.mushind.mind.domain.model.AppRule) {
        evaluationJob?.cancel()
        evaluationJob = serviceScope.launch {
            val app = appRulesRepository.getRestrictedApp(rule.packageName) ?: run {
                hideShield()
                return@launch
            }
            overlayController.show(ShieldState.Starting(app))
            val purchaseResult = try {
                purchaseUnlock(rule)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                null
            }
            when (val result = purchaseResult) {
                is SessionPurchaseResult.Purchased -> {
                    delay(250)
                    hideShield()
                }
                is SessionPurchaseResult.InsufficientPoints -> {
                    overlayController.show(
                        ShieldState.Blocked(
                            app = app,
                            costPoints = result.costPoints,
                            balance = result.balance,
                            missingPoints = result.costPoints - result.balance,
                            accessDescription = rule.accessDescription(),
                            canPurchase = false,
                        ),
                    )
                }
                null -> overlayController.show(
                    ShieldState.Error(app, "La compra no pudo completarse. No se descontaron puntos."),
                )
            }
        }
    }

    private fun exitToHome() {
        hideShield()
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    private fun hideShield() {
        shieldedPackage = null
        if (::overlayController.isInitialized) overlayController.hide()
    }

    private fun resolveHomePackages(): Set<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val homes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(homeIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(homeIntent, 0)
        }
        return homes
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }
}

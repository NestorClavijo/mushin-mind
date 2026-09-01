package com.mushind.mind.platform.accessibility

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Space
import android.widget.TextView
import android.widget.EditText
import android.view.MotionEvent
import android.os.SystemClock
import androidx.core.graphics.ColorUtils
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.RestrictedApp
import com.mushind.mind.domain.usecase.HoldConfirmationGate

class ShieldOverlayController(
    private val service: AccessibilityService,
    private val onPurchase: (AppRule) -> Unit,
    private val onEmergencyRequested: (RestrictedApp) -> Unit,
    private val onEmergencyConfirmed: (RestrictedApp, String?) -> Unit,
    private val onEmergencyCancelled: (String) -> Unit,
    private val onExit: () -> Unit,
) {
    private val windowManager = service.getSystemService(WindowManager::class.java)
    private var overlay: View? = null
    val isShowing: Boolean get() = overlay != null

    fun show(state: ShieldState) {
        hide()
        val view = buildView(state)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.FILL }
        runCatching { windowManager.addView(view, params) }
            .onSuccess {
                overlay = view
                view.requestFocus()
            }
    }

    fun hide() {
        overlay?.let { runCatching { windowManager.removeViewImmediate(it) } }
        overlay = null
    }

    private fun buildView(state: ShieldState): View {
        val root = FrameLayout(service).apply {
            setBackgroundColor(Color.rgb(18, 20, 17))
            isFocusableInTouchMode = true
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    onExit()
                    true
                } else {
                    false
                }
            }
        }
        val content = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(28), dp(48), dp(28), dp(32))
        }
        root.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )

        content.addView(Space(service), LinearLayout.LayoutParams(1, 0, 1f))
        val icon = ImageView(service).apply {
            contentDescription = "Icono de ${state.app.displayName}"
            setImageDrawable(runCatching { service.packageManager.getApplicationIcon(state.app.packageName) }.getOrNull())
        }
        content.addView(icon, LinearLayout.LayoutParams(dp(72), dp(72)))
        content.addText(state.app.displayName, 22f, true, top = 18)

        when (state) {
            is ShieldState.Blocked -> addBlocked(content, state)
            is ShieldState.Confirming -> addConfirmation(content, state)
            is ShieldState.Starting -> {
                content.addText("Iniciando sesión…", 24f, true, top = 24)
                content.addView(ProgressBar(service), marginParams(top = 22))
            }
            is ShieldState.Error -> {
                content.addText("No pudimos iniciar el acceso", 24f, true, top = 24)
                content.addText(state.message, 16f, false, top = 12)
            }
            is ShieldState.EmergencyWarning -> addEmergencyWarning(content, state)
            is ShieldState.EmergencyConfirmation -> addEmergencyConfirmation(content, state)
        }

        content.addView(Space(service), LinearLayout.LayoutParams(1, 0, 1f))
        if (state !is ShieldState.Starting) {
            content.addView(button("Volver", primary = false) { onExit() }, fullWidthParams(top = 16))
        }
        return root
    }

    private fun addBlocked(content: LinearLayout, state: ShieldState.Blocked) {
        content.addText("Bloqueada", 28f, true, top = 24)
        content.addText("${state.costPoints} pts · ${state.accessDescription}", 18f, false, top = 12)
        content.addText("Saldo actual: ${state.balance} pts", 16f, false, top = 18)
        if (state.missingPoints > 0) {
            content.addText("Te faltan ${state.missingPoints} pts", 16f, true, top = 8, color = Color.rgb(255, 180, 171))
        }
        if (state.canPurchase) {
            content.addView(
                button("Desbloquear por ${state.costPoints} pts", primary = true) {
                    val rule = requireNotNull(state.app.rule)
                    show(ShieldState.Confirming(state.app, rule, state.balance, state.accessDescription))
                },
                fullWidthParams(top = 24),
            )
        }
        content.addView(
            button("Usar desbloqueo de emergencia", primary = false) {
                onEmergencyRequested(state.app)
            },
            fullWidthParams(top = 12),
        )
    }

    private fun addEmergencyWarning(content: LinearLayout, state: ShieldState.EmergencyWarning) {
        content.addText("Desbloqueo de emergencia", 26f, true, top = 24)
        content.addText(
            "Esta acción omitirá temporalmente la regla y quedará registrada.",
            16f,
            false,
            top = 14,
        )
        content.addText("Duración: ${state.preview.policy.durationMinutes} min", 17f, false, top = 18)
        content.addText(
            "Penalización: ${state.preview.appliedPenaltyPoints} pts",
            17f,
            false,
            top = 8,
        )
        content.addView(
            button("Continuar", primary = true) {
                show(ShieldState.EmergencyConfirmation(state.app, state.preview))
            },
            fullWidthParams(top = 22),
        )
        content.addView(
            button("Cancelar", primary = false) { onEmergencyCancelled(state.app.packageName) },
            fullWidthParams(top = 10),
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addEmergencyConfirmation(content: LinearLayout, state: ShieldState.EmergencyConfirmation) {
        content.addText("Mantén pulsado para desbloquear", 24f, true, top = 24)
        val reason = EditText(service).apply {
            hint = "Motivo opcional"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(160, 165, 160))
            maxLines = 3
        }
        content.addView(reason, fullWidthParams(top = 18))
        val gate = HoldConfirmationGate()
        val holdLabel = service.getString(com.mushind.mind.R.string.emergency_hold_button)
        val hold = button(holdLabel, primary = true) { }
        hold.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    gate.start(SystemClock.elapsedRealtime())
                    (view as Button).text = service.getString(com.mushind.mind.R.string.emergency_hold_progress)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val confirmed = gate.release(SystemClock.elapsedRealtime())
                    (view as Button).text = holdLabel
                    if (confirmed) {
                        view.performClick()
                        view.isEnabled = false
                        onEmergencyConfirmed(state.app, reason.text?.toString())
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    gate.cancel()
                    (view as Button).text = holdLabel
                    true
                }
                else -> true
            }
        }
        content.addView(hold, fullWidthParams(top = 18))
        content.addView(
            button("Cancelar", primary = false) { onEmergencyCancelled(state.app.packageName) },
            fullWidthParams(top = 10),
        )
    }

    private fun addConfirmation(content: LinearLayout, state: ShieldState.Confirming) {
        content.addText("Confirmar acceso", 28f, true, top = 24)
        content.addText("${state.rule.costPoints} pts · ${state.accessDescription}", 18f, false, top = 12)
        content.addText(
            "Saldo: ${state.balance} → ${state.balance - state.rule.costPoints} pts",
            16f,
            false,
            top = 18,
        )
        val confirmButton = button("Confirmar gasto", primary = true) { }
        confirmButton.setOnClickListener {
            it.isEnabled = false
            onPurchase(state.rule)
        }
        content.addView(confirmButton, fullWidthParams(top = 24))
        content.addView(
            button("Cancelar", primary = false) {
                show(
                    ShieldState.Blocked(
                        state.app,
                        state.rule.costPoints,
                        state.balance,
                        0,
                        state.accessDescription,
                        true,
                    ),
                )
            },
            fullWidthParams(top = 10),
        )
    }

    private fun button(label: String, primary: Boolean, onClick: () -> Unit) = Button(service).apply {
        text = label
        textSize = 16f
        isAllCaps = false
        setTextColor(if (primary) Color.WHITE else Color.rgb(200, 210, 202))
        backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (primary) Color.rgb(64, 94, 81) else ColorUtils.setAlphaComponent(Color.WHITE, 24),
        )
        setOnClickListener { onClick() }
    }

    private fun LinearLayout.addText(
        value: String,
        size: Float,
        bold: Boolean,
        top: Int,
        color: Int = Color.rgb(228, 229, 224),
    ) {
        addView(
            TextView(service).apply {
                text = value
                textSize = size
                gravity = Gravity.CENTER
                setTextColor(color)
                if (bold) setTypeface(typeface, Typeface.BOLD)
            },
            marginParams(top = top),
        )
    }

    private fun fullWidthParams(top: Int) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
    ).apply { topMargin = dp(top) }

    private fun marginParams(top: Int) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
    ).apply { topMargin = dp(top) }

    private fun dp(value: Int): Int = (value * service.resources.displayMetrics.density).toInt()
}

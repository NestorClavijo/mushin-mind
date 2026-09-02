package com.mushind.mind.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mushind.mind.core.design.AppTheme
import com.mushind.mind.domain.model.AccentPalette
import com.mushind.mind.domain.model.ThemeMode

@Preview(name = "Claro estrecho", widthDp = 320, showBackground = true)
@Preview(
    name = "Oscuro texto grande",
    widthDp = 320,
    fontScale = 1.5f,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun DesignComponentsPreview() {
    AppTheme {
        ComponentSample()
    }
}

@Preview(name = "Acento océano", widthDp = 360, showBackground = true)
@Composable
private fun OceanAccentPreview() {
    AppTheme(themeMode = ThemeMode.LIGHT, accentPalette = AccentPalette.OCEAN) { ComponentSample() }
}

@Preview(name = "Acento ciruela oscuro", widthDp = 360, showBackground = true)
@Composable
private fun PlumAccentPreview() {
    AppTheme(themeMode = ThemeMode.DARK, accentPalette = AccentPalette.PLUM) { ComponentSample() }
}

@Composable
private fun ComponentSample() {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProgressHeader("Plan de hoy", "3 de 5 tareas completadas", 0.6f)
        PointBadge(30)
        TaskRow("Preparar presentación", "Completada", trailing = "+20 pts")
        AppRuleCard("Video", "20 puntos por 15 minutos")
        PrimaryButton("Continuar", {}, Modifier.fillMaxWidth())
        SecondaryButton("Cancelar", {}, Modifier.fillMaxWidth())
    }
}

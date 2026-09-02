package com.mushind.mind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mushind.mind.navigation.MindApp
import com.mushind.mind.core.design.AppTheme
import com.mushind.mind.domain.model.AppearanceSettings
import com.mushind.mind.domain.repository.AppearanceRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var appearanceRepository: AppearanceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appearance by appearanceRepository.settings.collectAsStateWithLifecycle(
                initialValue = AppearanceSettings(),
            )
            AppTheme(
                themeMode = appearance.themeMode,
                accentPalette = appearance.accentPalette,
            ) {
                MindApp()
            }
        }
    }
}

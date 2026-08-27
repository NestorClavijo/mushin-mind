package com.mushind.mind.feature.apps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mushind.mind.R
import com.mushind.mind.core.design.component.EmptyState
import com.mushind.mind.core.design.component.ScreenHeader

@Composable
fun AppsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.apps_title))
        EmptyState(
            title = stringResource(R.string.apps_empty),
            explanation = stringResource(R.string.apps_empty_explanation),
            actionLabel = stringResource(R.string.choose_apps),
            onAction = { },
        )
    }
}


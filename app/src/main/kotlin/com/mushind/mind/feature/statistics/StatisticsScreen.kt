package com.mushind.mind.feature.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mushind.mind.R
import com.mushind.mind.core.design.component.EmptyState
import com.mushind.mind.core.design.component.ScreenHeader

@Composable
fun StatisticsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.statistics_title))
        EmptyState(
            title = stringResource(R.string.statistics_empty),
            explanation = stringResource(R.string.statistics_empty_explanation),
        )
    }
}


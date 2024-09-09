package com.erdees.foodcostcalc.ui.composables.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SearchFieldTransition(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it * 2 }),
        exit = slideOutVertically(targetOffsetY = { -it * 2 }),
    ) { content() }
}
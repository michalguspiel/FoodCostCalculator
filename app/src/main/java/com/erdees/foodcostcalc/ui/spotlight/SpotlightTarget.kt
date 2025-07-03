package com.erdees.foodcostcalc.ui.spotlight

import androidx.annotation.StringRes
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class SpotlightTarget(
    val order: Int,
    @StringRes val info: Int,
    val rect: Rect? = null,
    val shape: SpotlightShape = SpotlightShape.RoundedRectangle,
    val cornerRadius: Dp = 16.dp,
    val hasNextButton: Boolean,
    val onClickAction: (() -> Unit)? = null,
    val scrollToElement: (suspend () -> Unit)? = null
)
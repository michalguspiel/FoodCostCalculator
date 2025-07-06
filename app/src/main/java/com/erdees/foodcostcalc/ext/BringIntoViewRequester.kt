package com.erdees.foodcostcalc.ext

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.toSize

suspend fun BringIntoViewRequester.bringIntoViewWithOffset(
    layoutCoordinates: LayoutCoordinates?,
    offsetPx: Int = 32
) {

    layoutCoordinates?.let {
        val rect = it.size.toSize().toRect().copy(
            bottom = it.size.height.toFloat() + offsetPx
        )
        this.bringIntoView(rect)
    }
}
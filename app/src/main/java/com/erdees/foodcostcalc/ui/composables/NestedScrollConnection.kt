package com.erdees.foodcostcalc.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

const val ScrollThreshold = 10

@Composable
fun rememberNestedScrollConnection(
    onVisibilityChange: (Boolean) -> Unit
): NestedScrollConnection {
    return remember {
        var isCurrentlyVisible = true

        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Scrolling down — hide
                if (available.y < -ScrollThreshold && isCurrentlyVisible) {
                    isCurrentlyVisible = false
                    onVisibilityChange(false)
                }

                // Scrolling up — show
                if (available.y > ScrollThreshold && !isCurrentlyVisible) {
                    isCurrentlyVisible = true
                    onVisibilityChange(true)
                }

                return Offset.Zero
            }
        }
    }
}

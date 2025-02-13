package com.erdees.foodcostcalc.ui.composables.rows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ButtonRow(
    modifier: Modifier = Modifier,
    primaryButton: @Composable () -> Unit,
    secondaryButton: @Composable (() -> Unit)? = null
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        if (secondaryButton != null) {
            secondaryButton()
            Spacer(modifier = Modifier.size(16.dp))
        }
        primaryButton()
    }
}
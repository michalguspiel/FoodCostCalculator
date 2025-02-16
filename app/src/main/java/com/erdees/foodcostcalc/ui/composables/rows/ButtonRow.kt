package com.erdees.foodcostcalc.ui.composables.rows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ButtonRow(
    modifier: Modifier = Modifier,
    primaryButton: @Composable () -> Unit,
    applyDefaultPadding: Boolean = true,
    secondaryButton: @Composable (() -> Unit)? = null,
    tertiaryButton: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = if(applyDefaultPadding) 24.dp else 0.dp),
        horizontalArrangement = Arrangement.End
    ) {
        if (tertiaryButton != null) {
            tertiaryButton()
            Spacer(modifier = Modifier.size(16.dp))
        }
        if (secondaryButton != null) {
            secondaryButton()
            Spacer(modifier = Modifier.size(16.dp))
        }
        primaryButton()
    }
}
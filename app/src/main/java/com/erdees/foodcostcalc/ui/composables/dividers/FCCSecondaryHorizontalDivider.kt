package com.erdees.foodcostcalc.ui.composables.dividers

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FCCSecondaryHorizontalDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
    )
}

@Composable
fun FCCThickSecondaryHorizontalDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier.fillMaxWidth(),
        thickness = (1.5).dp,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
    )
}

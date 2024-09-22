package com.erdees.foodcostcalc.ui.composables.dividers

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FCCPrimaryHorizontalDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier.fillMaxWidth(),
        thickness = (1.25).dp,
        color = MaterialTheme.colorScheme.primary
    )
}


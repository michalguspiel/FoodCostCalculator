package com.erdees.foodcostcalc.ui.composables.dividers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun FCCDecorativeCircle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(4.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
    )
}

@Preview
@Composable
private fun FCCDecorativeCirclePreview() {
    FCCTheme {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            FCCDecorativeCircle(
                modifier = Modifier
            )
        }
    }
}
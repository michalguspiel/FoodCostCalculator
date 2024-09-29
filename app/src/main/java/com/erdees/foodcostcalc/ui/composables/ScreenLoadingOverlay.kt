package com.erdees.foodcostcalc.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun ScreenLoadingOverlay(modifier: Modifier = Modifier, loadingText: String? = null) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            loadingText?.let {
                Text(
                    text = loadingText,
                    Modifier.padding(bottom = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
            CircularProgressIndicator()
        }
    }
}


@Preview
@Composable
private fun ScreenLoadingOverlayPreview() {
    FCCTheme {
        ScreenLoadingOverlay(
            modifier = Modifier.fillMaxSize(),
            "Database backup operation in progress.\nDo not close application."
        )
    }
}

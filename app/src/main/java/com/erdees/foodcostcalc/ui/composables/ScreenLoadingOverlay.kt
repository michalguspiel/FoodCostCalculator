package com.erdees.foodcostcalc.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun ScreenLoadingOverlay(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Gray.copy(alpha = 0.5f)),
    contentAlignment = androidx.compose.ui.Alignment.Center
  ) {
    CircularProgressIndicator()
  }
}


@Preview
@Composable
private fun ScreenLoadingOverlayPreview() {
  FCCTheme {
    ScreenLoadingOverlay(modifier = Modifier.fillMaxSize())
  }
}

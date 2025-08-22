package com.erdees.foodcostcalc.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun HeroImage(painter: Painter, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.45f),
                        primaryColor.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null,
            colorFilter = ColorFilter.tint(primaryColor)
        )
    }
}

@Preview
@Composable
private fun HeroImagePreview() {
    FCCTheme{
        Surface {
            HeroImage(painter = painterResource(R.drawable.cooking))
        }
    }
}
package com.erdees.foodcostcalc.ui.screens.limitreached

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.theme.FCCTheme

/**
 * Full-screen overlay displayed when a free user hits their content creation limit.
 * Shows an encouraging, aspirational message rather than a restrictive one.
 *
 * @param subHeadline Dynamic message about the specific limit reached (e.g., "You've successfully created 20 dishes—the maximum for the free plan.")
 * @param onDismiss Callback invoked when the close button is tapped
 * @param onSeePremium Callback invoked when the "See Premium Features" button is tapped
 * @param modifier Optional modifier for the composable
 */
@Composable
fun LimitReachedScreen(
    subHeadline: String,
    onDismiss: () -> Unit,
    onSeePremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp, top = 56.dp), // Top padding to account for close button
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Illustration
            Image(
                painter = painterResource(R.drawable.cooking),
                contentDescription = null,
                modifier = Modifier.size(200.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Headline
            Text(
                text = "Wow, You're a Power User!",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Sub-headline (dynamic)
            Text(
                text = subHeadline,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Teaser/Value Proposition
            Text(
                text = "Upgrade to Premium to unlock unlimited dishes, cloud backup, and all professional features.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Spacer to push CTA button to bottom
            Spacer(modifier = Modifier.weight(1f))
            
            // Call to Action Button
            FCCPrimaryButton(
                text = "See Premium Features",
                modifier = Modifier.fillMaxWidth(),
                onClick = onSeePremium
            )
        }
        
        // Close Button - Top Right Corner
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(name = "Limit Reached - Dishes", showBackground = true)
@Composable
private fun LimitReachedScreenDishesPreview() {
    FCCTheme {
        LimitReachedScreen(
            subHeadline = "You've successfully created 20 dishes—the maximum for the free plan.",
            onDismiss = {},
            onSeePremium = {}
        )
    }
}

@Preview(name = "Limit Reached - Half-Products", showBackground = true)
@Composable
private fun LimitReachedScreenHalfProductsPreview() {
    FCCTheme {
        LimitReachedScreen(
            subHeadline = "You've successfully created 2 half-products—the maximum for the free plan.",
            onDismiss = {},
            onSeePremium = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun LimitReachedScreenPreviewLightDark() {
    FCCTheme {
        LimitReachedScreen(
            subHeadline = "You've successfully created 20 dishes—the maximum for the free plan.",
            onDismiss = {},
            onSeePremium = {}
        )
    }
}
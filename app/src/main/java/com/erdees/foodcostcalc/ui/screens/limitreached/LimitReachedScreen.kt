package com.erdees.foodcostcalc.ui.screens.limitreached

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
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.composables.HeroImage
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.theme.FCCTheme

/**
 * Full-screen overlay displayed when a free user hits their content creation limit.
 * Shows an encouraging, aspirational message.
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
    modifier: Modifier = Modifier,
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
                .padding(bottom = 24.dp, top = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            HeroImage(
                painter = painterResource(R.drawable.cooking),
                modifier = Modifier.size(190.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.limit_reached_title),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subHeadline,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = stringResource(R.string.limit_reached_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            FCCPrimaryButton(
                text = stringResource(R.string.limit_reached_button_text),
                modifier = Modifier.fillMaxWidth(),
                onClick = onSeePremium
            )
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Sharp.Close,
                contentDescription = stringResource(R.string.limit_reached_close_description),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LimitReachedScreenPreviewLightDark() {
    FCCTheme {
        Surface {
            LimitReachedScreen(
                subHeadline = "You've successfully created 20 dishes - the maximum for the free plan.",
                onDismiss = {},
                onSeePremium = {}
            )
        }
    }
}
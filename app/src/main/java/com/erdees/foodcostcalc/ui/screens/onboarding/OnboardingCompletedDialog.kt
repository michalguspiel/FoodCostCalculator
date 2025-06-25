package com.erdees.foodcostcalc.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.ui.theme.FCCTheme

/**
 * Dialog shown when onboarding is completed successfully
 */
@Composable
fun OnboardingCompletedDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Onboarding Complete!",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "You've completed the guided tour of Food Cost Calculator. " +
                           "Feel free to explore the app and create your own dishes!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "You can always add more ingredients to your example dish or create new dishes from scratch.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Got it!")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun OnboardingCompletedDialogPreview() {
    FCCTheme {
        OnboardingCompletedDialog(
            onDismiss = {}
        )
    }
}

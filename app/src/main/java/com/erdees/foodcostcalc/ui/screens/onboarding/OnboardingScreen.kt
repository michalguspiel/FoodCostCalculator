package com.erdees.foodcostcalc.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.ui.navigation.FCCScreen

@Composable
fun OnboardingScreen(navController: NavController, viewModel: OnboardingViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        (uiState as? OnboardingUiState.Success)?.let {
            navController.navigate(FCCScreen.Dishes) {
                popUpTo(FCCScreen.Onboarding) { inclusive = true }
            }
            viewModel.resetUiState() // Reset after navigation
        }
    }

    OnboardingScreenContent(
        uiState = uiState,
        onShowExampleClick = { viewModel.createSampleDishAndNavigate() },
    )
}

@Composable
fun OnboardingScreenContent(
    uiState: OnboardingUiState,
    onShowExampleClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Food Cost Calculator!",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "See how the app works by viewing an example dish.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
        )
        Button(onClick = onShowExampleClick) {
            Text("Show Me an Example Dish")
        }
        if (uiState is OnboardingUiState.Loading) {
            Text("Loading example...", modifier = Modifier.padding(top = 16.dp))
        }
        if (uiState is OnboardingUiState.Error) {
            Text(uiState.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenContentPreview_Idle() {
    OnboardingScreenContent(
        uiState = OnboardingUiState.Idle,
        onShowExampleClick = {},
    )
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenContentPreview_Loading() {
    OnboardingScreenContent(
        uiState = OnboardingUiState.Loading,
        onShowExampleClick = {},
    )
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenContentPreview_Error() {
    OnboardingScreenContent(
        uiState = OnboardingUiState.Error("Something went wrong!"),
        onShowExampleClick = {},
    )
}

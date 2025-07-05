package com.erdees.foodcostcalc.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.spotlight.Spotlight
import com.erdees.foodcostcalc.ui.spotlight.SpotlightStep
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun OnboardingScreen(
    navController: NavController,
    spotlight: Spotlight,
    viewModel: OnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is OnboardingUiState.Success -> {
                spotlight.start(SpotlightStep.entries.map { it.toSpotlightTarget() })
                navController.navigate(FCCScreen.Dishes) {
                    popUpTo(FCCScreen.Onboarding) { inclusive = true }
                }
                viewModel.resetUiState()
            }

            is OnboardingUiState.Skipped -> {
                navController.navigate(FCCScreen.Products) {
                    popUpTo(FCCScreen.Onboarding) { inclusive = true }
                }
                viewModel.resetUiState()
            }

            else -> {}
        }
    }

    OnboardingScreenContent(
        uiState = uiState,
        onShowExampleClick = { viewModel.startOnboardingCreateSampleDishAndNavigate() },
        onSkipClick = { viewModel.onboardingSkipped() }
    )
}

@Composable
fun OnboardingScreenContent(
    uiState: OnboardingUiState,
    modifier: Modifier = Modifier,
    onShowExampleClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.chef_hat_24px),
            contentDescription = stringResource(id = R.string.onboarding_icon_content_description),
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(id = R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.onboarding_welcome_subtitle),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(64.dp))
        FCCPrimaryButton(
            text = stringResource(id = R.string.onboarding_see_example),
            onClick = onShowExampleClick
        )
        Spacer(modifier = Modifier.height(8.dp))
        FCCTextButton(
            text = stringResource(id = R.string.onboarding_skip),
            onClick = onSkipClick
        )
        if (uiState is OnboardingUiState.Loading) {
            Text("Loading example...", modifier = Modifier.padding(top = 16.dp))
        }
        if (uiState is OnboardingUiState.Error) {
            Text(uiState.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingScreenContentPreview_Idle() {
    FCCTheme {
        Scaffold { padding ->
            OnboardingScreenContent(
                modifier = Modifier.padding(padding),
                uiState = OnboardingUiState.Idle,
                onShowExampleClick = {},
                onSkipClick = {}
            )
        }
    }
}

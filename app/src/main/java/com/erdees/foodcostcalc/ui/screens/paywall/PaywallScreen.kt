package com.erdees.foodcostcalc.ui.screens.paywall

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.premiumSubscription.Plan
import com.erdees.foodcostcalc.ext.getActivity
import com.erdees.foodcostcalc.ui.composables.HeroImage
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTopAppBarNavIconButton
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun PaywallScreen(
    navController: NavController,
    viewModel: PaywallViewModel = viewModel(),
) {
    val context = LocalContext.current
    val activity = context.getActivity()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PaywallScreenContent(
        navController = navController,
        uiState = uiState,
        onPlanSelected = viewModel::selectPlan,
        onUpgradeClicked = { viewModel.onUpgradeClicked(activity) },
        onRestorePurchases = viewModel::onRestorePurchases,
        onTermsAndPrivacyClicked = { launchPrivacyPolicySite(context) },
        onErrorAcknowledged = viewModel::acknowledgeError
    )

    LaunchedEffect(uiState.restoreState) {
        when (val state = uiState.restoreState) {
            is RestoreState.Success -> {
                Toast.makeText(context, "Premium access restored!", Toast.LENGTH_LONG).show()
                viewModel.resetRestoreState()
            }
            is RestoreState.NoPurchases -> {
                Toast.makeText(context, "No active subscriptions found.", Toast.LENGTH_LONG).show()
                viewModel.resetRestoreState()
            }
            is RestoreState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetRestoreState()
            }
            is RestoreState.Idle -> { /* Do nothing */ }
        }
    }
}

private fun launchPrivacyPolicySite(context: Context){
    val link = "https://michalguspiel.github.io/Mobile-Privacy-Policy/"
    val intent = Intent(Intent.ACTION_VIEW, link.toUri())
    context.startActivity(intent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaywallScreenContent(
    navController: NavController,
    uiState: PaywallUiState,
    onPlanSelected: (Plan) -> Unit,
    onUpgradeClicked: () -> Unit,
    onRestorePurchases: () -> Unit,
    onTermsAndPrivacyClicked: () -> Unit,
    onErrorAcknowledged: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.premium)) },
                navigationIcon = {
                    FCCTopAppBarNavIconButton(navController = navController)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Hero Visual Section
                HeroSection()

                // Benefits Section
                BenefitsSection()

                // Plan Selector Section
                if (uiState.monthlyPlan != null && uiState.yearlyPlan != null) {
                    PlanSelectorSection(
                        monthlyPlan = uiState.monthlyPlan,
                        yearlyPlan = uiState.yearlyPlan,
                        selectedPlan = uiState.selectedPlan,
                        onPlanSelected = onPlanSelected
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // CTA Section
                CTASection(
                    selectedPlan = uiState.selectedPlan,
                    onUpgradeClicked = onUpgradeClicked,
                    onRestorePurchases = onRestorePurchases,
                    onTermsAndPrivacyClicked = onTermsAndPrivacyClicked
                )
            }

            // Loading Overlay
            if (uiState.isLoading) {
                ScreenLoadingOverlay()
            }

            // Error handling could be added here
            uiState.error?.let { error ->
                // For now, just acknowledge the error automatically
                // In a production app, you might show a snackbar or dialog
                onErrorAcknowledged()
            }
        }
    }
}

@Composable
private fun HeroSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroImage(
            painter = painterResource(R.drawable.chef),
            modifier = Modifier.size(80.dp),
        )

        Text(
            text = stringResource(R.string.paywall_headline),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(R.string.paywall_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun BenefitsSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BenefitItem(
            title = stringResource(R.string.paywall_benefit_unlimited_title),
            description = stringResource(R.string.paywall_benefit_unlimited_desc)
        )

        BenefitItem(
            title = stringResource(R.string.paywall_benefit_export_title),
            description = stringResource(R.string.paywall_benefit_export_desc),
            benefitComingSoon = true
        )

        BenefitItem(
            title = stringResource(R.string.paywall_benefit_ad_free_title),
            description = stringResource(R.string.paywall_benefit_ad_free_desc)
        )
    }
}

@Composable
private fun BenefitItem(
    title: String,
    description: String,
    benefitComingSoon: Boolean = false,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!benefitComingSoon) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val comingSoon = stringResource(R.string.coming_soon)
                val comingSoonDescription = buildAnnotatedString {
                    append(description)
                    append(" ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(comingSoon)
                    }
                }
                Text(
                    text = comingSoonDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanSelectorSection(
    monthlyPlan: Plan,
    yearlyPlan: Plan,
    selectedPlan: Plan?,
    onPlanSelected: (Plan) -> Unit,
) {
    val savePercent = yearlyPlan.priceInMicros.let { yearlyPrice ->
        val monthlyPrice = monthlyPlan.priceInMicros
        if (monthlyPrice > 0) {
            ((1 - (yearlyPrice / 12f) / monthlyPrice) * 100).toInt()
        } else {
            0
        }
    }
    val savePercentString = stringResource(R.string.save_x_percent, savePercent.toString())
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            SegmentedButton(
                modifier = Modifier
                    .weight(1f)           // Equal widths
                    .fillMaxHeight(),      // Match tallest
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                onClick = { onPlanSelected(monthlyPlan) },
                selected = selectedPlan?.id == monthlyPlan.id
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.monthly),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = monthlyPlan.formattedPrice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SegmentedButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                onClick = { onPlanSelected(yearlyPlan) },
                selected = selectedPlan?.id == yearlyPlan.id
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.best_value),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    Text(
                        text = stringResource(R.string.yearly),
                        style = MaterialTheme.typography.labelLarge
                    )

                    Text(
                        text = yearlyPlan.formattedPrice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = savePercentString,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontStyle = FontStyle.Italic
                        )
                    )
                }
            }
        }
    }
}


@Composable
private fun CTASection(
    selectedPlan: Plan?,
    onUpgradeClicked: () -> Unit,
    onRestorePurchases: () -> Unit,
    onTermsAndPrivacyClicked: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        FCCPrimaryButton(
            text = when (selectedPlan?.billingPeriod) {
                "P1M" -> stringResource(
                    R.string.paywall_upgrade_monthly,
                    selectedPlan.formattedPrice
                )

                "P1Y" -> stringResource(
                    R.string.paywall_upgrade_yearly,
                    selectedPlan.formattedPrice
                )

                else -> stringResource(R.string.subscribe)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedPlan != null,
            onClick = onUpgradeClicked
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FCCTextButton(
                text = stringResource(R.string.paywall_restore_purchases),
                onClick = onRestorePurchases
            )

            Text(
                text = "•",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FCCTextButton(
                text = stringResource(R.string.paywall_terms_privacy),
                onClick = onTermsAndPrivacyClicked
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PaywallScreenContentPreview() {
    val mockMonthlyPlan = Plan(
        id = "monthly",
        offerIdToken = "monthly_token",
        billingPeriod = "P1M",
        formattedPrice = "€4.99",
        currencyCode = "EUR",
        priceInMicros = 4990000
    )

    val mockYearlyPlan = Plan(
        id = "yearly",
        offerIdToken = "yearly_token",
        billingPeriod = "P1Y",
        formattedPrice = "€39.99",
        currencyCode = "EUR",
        priceInMicros = 39990000
    )

    val mockUiState = PaywallUiState(
        availablePlans = listOf(mockMonthlyPlan, mockYearlyPlan),
        selectedPlan = mockYearlyPlan,
        isLoading = false,
        error = null
    )

    FCCTheme {
        PaywallScreenContent(
            navController = rememberNavController(),
            uiState = mockUiState,
            onPlanSelected = {},
            onUpgradeClicked = {},
            onRestorePurchases = {},
            onTermsAndPrivacyClicked = {},
            onErrorAcknowledged = {}
        )
    }
}
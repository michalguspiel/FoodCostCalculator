package com.erdees.foodcostcalc.ui.screens.paywall

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
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
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.premiumSubscription.Plan
import com.erdees.foodcostcalc.domain.model.premiumSubscription.PremiumPlanType
import com.erdees.foodcostcalc.domain.model.premiumSubscription.PremiumSubscription
import com.erdees.foodcostcalc.ext.getActivity
import com.erdees.foodcostcalc.ui.composables.HeroImage
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTopAppBarNavIconButton
import com.erdees.foodcostcalc.ui.composables.dividers.FCCDecorativeCircle
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.Constants
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSubscriptionScreen(
    navController: NavController,
    viewModel: PaywallViewModel = viewModel(),
) {
    val context = LocalContext.current
    val activity = context.getActivity()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState != null) {
                when (uiState.currentActivePremiumPlanType) {
                    PremiumPlanType.UNLIMITED_PREMIUM,
                    PremiumPlanType.LEGACY,
                        -> ActiveSubscriptionContent(
                        modifier = Modifier.padding(top = 8.dp),
                        uiState = uiState,
                        planType = uiState.currentActivePremiumPlanType,
                        onDone = { navController.popBackStack() }
                    )

                    null -> PaywallScreenContent(
                        modifier = Modifier.padding(top = 8.dp),
                        uiState = uiState,
                        onPlanSelected = viewModel::selectPlan,
                        onUpgradeClick = { viewModel.onUpgradeClicked(activity) },
                        onRestorePurchases = viewModel::onRestorePurchases,
                        onTermsAndPrivacyClick = { launchPrivacyPolicySite(context) }
                    )
                }
            } else {
                ScreenLoadingOverlay(Modifier.fillMaxSize())
            }

            FCCTopAppBarNavIconButton(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
        }
    }

    LaunchedEffect(uiState?.restoreState) {
        when (val state = uiState?.restoreState) {
            is RestoreState.Success -> {
                Toast.makeText(context, context.getString(R.string.premium_access_restored), Toast.LENGTH_LONG).show()
                viewModel.resetRestoreState()
            }

            is RestoreState.NoPurchases -> {
                Toast.makeText(context, context.getString(R.string.no_active_subscriptions), Toast.LENGTH_LONG)
                    .show()
                viewModel.resetRestoreState()
            }

            is RestoreState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetRestoreState()
            }

            else -> { /* Do nothing */
            }
        }
    }
}

private fun launchPrivacyPolicySite(context: Context) {
    val link = Constants.Links.PRIVACY_POLICY
    val intent = Intent(Intent.ACTION_VIEW, link.toUri())
    context.startActivity(intent)
}

private fun onManageSubscription(context: Context, currentPremiumPlanType: PremiumPlanType) {
    val link =
        "https://play.google.com/store/account/subscriptions?sku=${currentPremiumPlanType.productId}&package=com.erdees.foodcostcalc"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = link.toUri()
        setPackage("com.android.vending")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    context.startActivity(intent)
}

@Composable
private fun ActiveSubscriptionContent(
    uiState: PaywallUiState,
    planType: PremiumPlanType,
    modifier: Modifier = Modifier,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            HeroImage(
                painter = painterResource(R.drawable.chef),
                modifier = Modifier.size(80.dp),
            )

            ActiveSubscriptionSection(planType)

            BenefitsSection(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.weight(1f))

            ButtonRow(secondaryButton = {
                FCCTextButton(
                    text = stringResource(R.string.manage_subscription),
                    onClick = {
                        onManageSubscription(context, planType)
                    }
                )
            }, primaryButton = {
                FCCPrimaryButton(
                    text = stringResource(R.string.done),
                    onClick = { onDone() }
                )
            })


        }

        if (uiState.screenLaunchedWithoutSubscription && uiState.currentActivePremiumPlanType != null) {
            val party = Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(0.5, 0.3)
            )

            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(party),
            )
        }
    }
}

@Composable
private fun ActiveSubscriptionSection(
    premiumPlanType: PremiumPlanType,
    modifier: Modifier = Modifier,
) {
    val activeSubscriptionText = stringResource(id = R.string.active_subscription)
    val thankYou = stringResource(id = R.string.happy_cooking)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(5) {
                Icon(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(vertical = 4.dp),
                    imageVector = Icons.Filled.Star,
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = stringResource(id = R.string.star)
                )
            }
        }

        Text(
            text = buildAnnotatedString {
                withStyle(style = ParagraphStyle()) {
                    append(activeSubscriptionText)
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                    append(thankYou)
                }
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (premiumPlanType == PremiumPlanType.LEGACY) {
            Spacer(Modifier.size(12.dp))
            Text(
                text = stringResource(R.string.loyalty_reward_headline),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = stringResource(R.string.loyalty_reward_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaywallScreenContent(
    uiState: PaywallUiState,
    onPlanSelected: (Plan) -> Unit,
    onUpgradeClick: () -> Unit,
    onRestorePurchases: () -> Unit,
    onTermsAndPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        if (uiState.premiumSubscription != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    HeroSection()

                    BenefitsSection(modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.size(16.dp))
                PlanSelectorSection(
                    monthlyPlan = uiState.premiumSubscription.monthlyPlan,
                    yearlyPlan = uiState.premiumSubscription.yearlyPlan,
                    selectedPlan = uiState.selectedPlan,
                    onPlanSelected = onPlanSelected
                )
                Spacer(modifier = Modifier.size(16.dp))
                CTASection(
                    selectedPlan = uiState.selectedPlan,
                    onUpgradeClick = onUpgradeClick,
                    onRestorePurchases = onRestorePurchases,
                    onTermsAndPrivacyClick = onTermsAndPrivacyClick
                )
            }
        } else {
            SomethingWentWrongContent(modifier = Modifier.fillMaxSize())
        }

        if (uiState.isLoading) {
            ScreenLoadingOverlay()
        }
    }
}

@Composable
private fun HeroSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
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
private fun BenefitsSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
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

@Composable
private fun CTASection(
    selectedPlan: Plan?,
    onUpgradeClick: () -> Unit,
    onRestorePurchases: () -> Unit,
    onTermsAndPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
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
            onClick = onUpgradeClick
        )
        Spacer(modifier = Modifier.size(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FCCTextButton(
                modifier = Modifier.weight(0.5f),
                text = stringResource(R.string.paywall_restore_purchases),
                onClick = onRestorePurchases
            )
            FCCDecorativeCircle()
            FCCTextButton(
                modifier = Modifier.weight(0.5f),
                text = stringResource(R.string.paywall_terms_privacy),
                onClick = onTermsAndPrivacyClick
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
        premiumSubscription = PremiumSubscription(
            id = "test",
            title = "Test Subscription",
            description = "This is a test subscription",
            monthlyPlan = mockMonthlyPlan,
            yearlyPlan = mockYearlyPlan
        ),
        currentActivePremiumPlanType = null,
        screenLaunchedWithoutSubscription = false,
        selectedPlan = mockYearlyPlan,
        isLoading = false,
        error = null
    )

    FCCTheme {
        Surface {
            PaywallScreenContent(
                uiState = mockUiState,
                onPlanSelected = {},
                onUpgradeClick = {},
                onRestorePurchases = {},
                onTermsAndPrivacyClick = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ActiveSubscriptionContentPreview() {
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
        premiumSubscription = PremiumSubscription(
            id = "test",
            title = "Premium Mode",
            description = "Premium features unlocked",
            monthlyPlan = mockMonthlyPlan,
            yearlyPlan = mockYearlyPlan
        ),
        currentActivePremiumPlanType = PremiumPlanType.LEGACY,
        screenLaunchedWithoutSubscription = true,
        selectedPlan = mockYearlyPlan,
        isLoading = false,
        error = null
    )

    FCCTheme {
        Surface {
            ActiveSubscriptionContent(uiState = mockUiState, planType = PremiumPlanType.LEGACY) {}
        }
    }
}
package com.erdees.foodcostcalc.ui.screens.subscriptionScreen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.premiumSubscription.Plan
import com.erdees.foodcostcalc.domain.model.premiumSubscription.PremiumSubscription
import com.erdees.foodcostcalc.ext.getActivity
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTopAppBarNavIconButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.theme.FCCTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(navController: NavController) {

    val viewModel: SubscriptionViewModel = viewModel()
    val context = LocalContext.current
    val activity = context.getActivity()

    val screenState by viewModel.screenState.collectAsState()
    val capturedScreenState = screenState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.subscription)) },
                navigationIcon = {
                    FCCTopAppBarNavIconButton(navController = navController)
                }
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues), contentAlignment = Alignment.Center) {

            if (capturedScreenState.premiumSubscription != null) {
                PremiumSubscriptionOffer(
                    subscription = capturedScreenState.premiumSubscription,
                    selectedPlan = capturedScreenState.selectedPlan,
                    onPlanSelected = { viewModel.onPlanSelected(it) },
                    onSubscribeClicked = {
                        viewModel.onSubscribeClicked(activity)
                    },
                    onManageSubscription = {
                        viewModel.onManageSubscription(context = context)
                    }
                )
            }
        }
        when {
            capturedScreenState.isLoading -> {
                ScreenLoadingOverlay()
            }

            (capturedScreenState.error != null) -> {
                val errorMessage = capturedScreenState.error.message
                ErrorDialog(
                    title = stringResource(id = R.string.error),
                    content = errorMessage
                        ?: stringResource(id = R.string.something_went_wrong),
                ) {
                    viewModel.acknowledgeError()
                }
            }
        }
    }
}

@Composable
fun PremiumSubscriptionOffer(
    subscription: PremiumSubscription,
    userAlreadySubscribes: Boolean = false,
    selectedPlan: Plan?,
    modifier: Modifier = Modifier,
    titleOverride: String? = null,
    onPlanSelected: (Plan) -> Unit = {},
    onSubscribeClicked: () -> Unit = {},
    onManageSubscription: () -> Unit = {}
) {
    Log.i("SettingScreen", "BrowseProductsModal")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            style = MaterialTheme.typography.headlineLarge,
            lineHeight = 30.sp,
            textAlign = TextAlign.Center,
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(titleOverride?.uppercase() ?: subscription.title.uppercase())
                }
                append("\n")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                    append("Subscription".uppercase())
                }
            })
        Text(
            text = subscription.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                top = 8.dp,
                start = 8.dp,
                end = 8.dp,
                bottom = 8.dp
            )
        )

        Row(Modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PlanDetails(
                modifier = Modifier.weight(1f),
                plan = subscription.monthlyPlan,
                selectedPlanId = selectedPlan?.id
            ) { onPlanSelected(subscription.monthlyPlan) }
            PlanDetails(
                modifier = Modifier.weight(1f),
                plan = subscription.yearlyPlan,
                selectedPlanId = selectedPlan?.id
            ) { onPlanSelected(subscription.yearlyPlan) }
        }

        Spacer(modifier = Modifier.size(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {

            if (userAlreadySubscribes) {
                FCCTextButton(
                    modifier = Modifier.padding(end = 16.dp),
                    text = "Manage Subscription"
                ) { onManageSubscription() }
            }

            FCCPrimaryButton(text = "Subscribe", enabled = !userAlreadySubscribes) {
                onSubscribeClicked()
            }
        }
    }
}

@Composable
private fun PlanDetails(
    plan: Plan,
    selectedPlanId: String?,
    modifier: Modifier = Modifier,
    onSelected: () -> Unit = {}
) {

    val isSelected = plan.id == selectedPlanId

    val planPeriod = when (plan.billingPeriod) {
        "P1M" -> stringResource(id = R.string.monthly)
        "P1Y" -> stringResource(id = R.string.yearly)
        else -> stringResource(id = R.string.yearly)
    }

    val description = when (plan.billingPeriod) {
        "P1M" -> stringResource(id = R.string.try_it_out)
        "P1Y" -> stringResource(id = R.string.best_value)
        else -> stringResource(id = R.string.best_value)
    }

    val cancelAnytime = stringResource(id = R.string.cancel_anytime)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = if (isSelected) 1.dp else 0.7.dp,
            color = if (isSelected) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.surfaceTint
        )
    ) {
        Column(
            modifier = Modifier.clickable { onSelected() },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Spacer(modifier = Modifier.size(24.dp))
                Text(
                    modifier = Modifier,
                    text = planPeriod.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (isSelected) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(id = R.string.content_description_selected_plan)
                    )
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }
            }

            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                        append(plan.formattedPrice)
                    }
                    withStyle(style = ParagraphStyle(lineHeight = 16.sp)) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                            append(description)
                            append("\n")
                            append(cancelAnytime)
                        }
                    }
                },
            )
        }
    }
}

@Preview(backgroundColor = 0xFF999999, showBackground = true)
@Composable
fun PremiumSubscriptionOfferModalPreview() {
    val fakePremiumSubscription = PremiumSubscription(
        id = "food.cost.calculator.premium.account",
        title = "Premium Mode (Food Cost Calculator)",
        description = "Premium mode. Ad-Free Experience: Removes all ads for uninterrupted workflow.",
        monthlyPlan = Plan(
            id = "premium-mode-monthly-plan",
            offerIdToken = "fakeOfferIdTokenMonthly",
            billingPeriod = "P1M",
            formattedPrice = "PLN 9.99",
            currencyCode = "PLN"
        ),
        yearlyPlan = Plan(
            id = "premium-mode-yearly-plan",
            offerIdToken = "fakeOfferIdTokenYearly",
            billingPeriod = "P1Y",
            formattedPrice = "PLN 59.99",
            currencyCode = "PLN"
        )
    )
    FCCTheme {
        PremiumSubscriptionOffer(
            fakePremiumSubscription,
            titleOverride = "Premium",
            selectedPlan = fakePremiumSubscription.monthlyPlan
        ) {}
    }
}
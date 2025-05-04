package com.erdees.foodcostcalc.ui.screens.subscriptionScreen

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun SubscriptionScreen(
    navController: NavController,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context.getActivity()
    val lifecycleOwner = LocalLifecycleOwner.current
    val screenState by viewModel.screenState.collectAsState()

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.updateSubscriptionStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SubscriptionScreenContent(
        navController = navController,
        screenState = screenState,
        onErrorAck = viewModel::acknowledgeError,
        onPlanSelect = viewModel::onPlanSelected,
        onSubscribeClick = { viewModel.onSubscribeClicked(activity) },
        onManageSubscription = { viewModel.onManageSubscription(context) }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SubscriptionScreenContent(
    navController: NavController,
    screenState: SubscriptionScreenState?,
    onSubscribeClick: () -> Unit,
    onManageSubscription: () -> Unit,
    onErrorAck: () -> Unit,
    onPlanSelect: (Plan) -> Unit
) {

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

            if (screenState != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {


                    val subscription = screenState.premiumSubscription
                    if (subscription != null) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Title(subscription = subscription)
                            // Description
                            Text(
                                text = subscription.description,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(
                                    top = 8.dp,
                                    start = 12.dp,
                                    end = 12.dp,
                                    bottom = 8.dp
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (!screenState.userAlreadySubscribes) {
                                PlanSection(
                                    subscription = subscription,
                                    selectedPlan = screenState.selectedPlan,
                                    onPlanSelected = { onPlanSelect(it) }
                                )
                            } else {
                                ActiveSubscriptionSection()
                            }

                            Text(
                                text = stringResource(id = R.string.subscription_extra_desc),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(
                                    top = 8.dp,
                                    start = 12.dp,
                                    end = 12.dp,
                                    bottom = 8.dp
                                )
                            )
                        }


                        ButtonRow(
                            userAlreadySubscribes = screenState.userAlreadySubscribes,
                            onSubscribeClick = {
                                onSubscribeClick()
                            },
                            onManageSubscription = {
                                onManageSubscription()
                            }
                        )
                    } else {
                        SomethingWentWrongContent()
                    }
                }


                if (screenState.screenLaunchedWithoutSubscription && screenState.userAlreadySubscribes) {
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
                ScreenStateOverlay(screenState) { onErrorAck() }
            } else {
                ScreenLoadingOverlay()
            }
        }
    }
}

@Composable
private fun ActiveSubscriptionSection(modifier: Modifier = Modifier) {
    val activeSubscriptionText = stringResource(id = R.string.active_subscription)
    val thankYou = stringResource(id = R.string.active_subscription_thank_you)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(5) {
                Icon(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(vertical = 4.dp),
                    imageVector = Icons.Filled.Star,
                    tint = Color.Yellow,
                    contentDescription = stringResource(
                        id = R.string.star
                    )
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
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding()
        )
    }
}

@Composable
private fun SomethingWentWrongContent(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(id = R.string.something_went_wrong),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding()
        )

        Text(
            text = stringResource(id = R.string.products_missing_error),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding()
        )
    }
}

@Composable
private fun ButtonRow(
    userAlreadySubscribes: Boolean,
    modifier: Modifier = Modifier,
    onManageSubscription: () -> Unit,
    onSubscribeClick: () -> Unit
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        if (userAlreadySubscribes) {
            FCCTextButton(
                modifier = Modifier.padding(end = 16.dp),
                text = "Manage Subscription"
            ) { onManageSubscription() }
        }

        FCCPrimaryButton(text = "Subscribe", enabled = !userAlreadySubscribes) {
            onSubscribeClick()
        }
    }
}

@Composable
private fun Title(
    subscription: PremiumSubscription,
    modifier: Modifier = Modifier,
    titleOverride: String? = null
) {
    Text(
        modifier = modifier,
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
}

@Composable
fun PlanSection(
    subscription: PremiumSubscription,
    selectedPlan: Plan?,
    modifier: Modifier = Modifier,
    onPlanSelected: (Plan) -> Unit
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
}

@Composable
private fun PlanDetails(
    plan: Plan,
    selectedPlanId: String?,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit = {}
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
            color = if (isSelected) MaterialTheme.colorScheme.surfaceTint
            else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.clickable { onSelect() },
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

@Composable
private fun ScreenStateOverlay(
    capturedScreenState: SubscriptionScreenState,
    onErrorAck: () -> Unit
) {
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
                onErrorAck()
            }
        }
    }
}

@Preview(backgroundColor = 0xFF999999, showBackground = true)
@Composable
private fun SubscriptionScreenContentPreview1() {
    val fakePremiumSubscription = PremiumSubscription(
        id = "food.cost.calculator.premium.account",
        title = "Premium Mode",
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
        SubscriptionScreenContent(
            navController = rememberNavController(),
            screenState = SubscriptionScreenState(
                premiumSubscription = fakePremiumSubscription,
                selectedPlan = fakePremiumSubscription.monthlyPlan,
                userAlreadySubscribes = false,
                screenLaunchedWithoutSubscription = true
            ),
            onErrorAck = {},
            onPlanSelect = {},
            onSubscribeClick = {},
            onManageSubscription = {}
        )
    }
}

@Preview(backgroundColor = 0xFF999999, showBackground = true)
@Composable
private fun SubscriptionScreenContentPreview2() {
    val fakePremiumSubscription = PremiumSubscription(
        id = "food.cost.calculator.premium.account",
        title = "Premium Mode",
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
        SubscriptionScreenContent(
            navController = rememberNavController(),
            screenState = SubscriptionScreenState(
                premiumSubscription = fakePremiumSubscription,
                selectedPlan = fakePremiumSubscription.monthlyPlan,
                userAlreadySubscribes = true,
                screenLaunchedWithoutSubscription = true
            ),
            onErrorAck = {},
            onPlanSelect = {},
            onSubscribeClick = {},
            onManageSubscription = {}
        )
    }
}

@Preview(backgroundColor = 0xFF999999, showBackground = true)
@Composable
private fun SubscriptionScreenContentPreviewError() {

    FCCTheme {
        SubscriptionScreenContent(
            navController = rememberNavController(),
            screenState = SubscriptionScreenState(
                premiumSubscription = null,
                userAlreadySubscribes = true,
                screenLaunchedWithoutSubscription = true
            ),
            onErrorAck = {},
            onPlanSelect = {},
            onSubscribeClick = {},
            onManageSubscription = {}
        )
    }
}
package com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishSummary

import android.icu.util.Currency
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.product.ProductAddedToDish
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.ui.composables.Ingredients
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCDialog
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.navigation.ConfirmPopUp
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.CreateDishV2ViewModel
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.SingleServing
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.Utils

@Composable
fun CreateDishSummaryScreen(
    navController: NavController,
    viewModel: CreateDishV2ViewModel,
) {
    val screenState = viewModel.screenState.collectAsState().value

    // Handle different screen states
    when (screenState) {
        is ScreenState.Interaction -> {
            when (val interaction = screenState.interaction) {
                is InteractionType.SaveDefaultSettings -> {
                    SaveDefaultSettingsDialog(
                        margin = interaction.margin,
                        tax = interaction.tax,
                        onSaveAsDefault = { viewModel.saveAsDefaultSettings() },
                        onDismiss = { viewModel.dismissDefaultSettingsPrompt() }
                    )
                }

                else -> {}
            }
        }
        else -> { }
    }

    CreateDishSummaryContent(
        CreateDishSummaryScreenState(
            dishName = viewModel.dishName.collectAsState().value,
            addedComponents = viewModel.addedComponents.collectAsState().value,
            foodCost = viewModel.foodCost.collectAsState().value,
            marginPercent = viewModel.marginPercentInput.collectAsState().value,
            taxPercent = viewModel.taxPercentInput.collectAsState().value,
            finalSellingPrice = viewModel.finalSellingPrice.collectAsState().value,
            currency = viewModel.currency.collectAsState().value,
            isLoading = viewModel.isLoading.collectAsState().value,
            errorRes = viewModel.errorRes.collectAsState().value,
            successfullySavedDishId = viewModel.saveDishSuccess.collectAsState().value
        ),
        onBackClick = { navController.popBackStack() },
        onSaveDishClick = { viewModel.onSaveDishClick() },
        onMarginChange = { viewModel.updateMarginPercentInput(it) },
        onTaxChange = { viewModel.updateTaxPercentInput(it) },
        onErrorDismiss = { viewModel.dismissError() },
        successNavigate = {
            navController.navigate(FCCScreen.DishDetails(it)) {
                popUpTo(FCCScreen.Dishes) {
                    inclusive = false
                }
            }
            viewModel.resetSaveDishSuccess()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDishSummaryContent(
    state: CreateDishSummaryScreenState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveDishClick: () -> Unit = {},
    onMarginChange: (String) -> Unit = {},
    onTaxChange: (String) -> Unit = {},
    onErrorDismiss: () -> Unit = {},
    successNavigate: (Long) -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dish_cost_and_price, state.dishName)) },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            Icons.AutoMirrored.Sharp.ArrowBack,
                            contentDescription = stringResource(
                                id = R.string.back
                            )
                        )
                    }
                })
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Ingredients(
                state.addedComponents,
                emptyList(),
                servings = SingleServing,
                currency = state.currency,
                showPrices = false,
                spacious = true
            )

            CalculationCard(
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = interactionSource
                ) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                state = state,
                onMarginChange = { onMarginChange(it) },
                onTaxChange = { onTaxChange(it) }
            )

            ButtonRow(
                modifier = Modifier.fillMaxWidth(),
                primaryButton = {
                    FCCPrimaryButton(
                        stringResource(R.string.save_dish),
                        enabled = !state.isLoading
                    ) {
                        onSaveDishClick()
                    }
                },
                secondaryButton = {
                    FCCTextButton(
                        stringResource(R.string.add_more_ingredients),
                        enabled = !state.isLoading
                    ) {
                        onBackClick()
                    }
                },
            )
        }
        AnimatedVisibility(state.isLoading, enter = fadeIn(), exit = fadeOut()) {
            ScreenLoadingOverlay(Modifier.fillMaxSize())
        }

        if (state.errorRes != null) {
            ErrorDialog(
                content = stringResource(state.errorRes),
                onDismiss = { onErrorDismiss() },
            )
        }

        ConfirmPopUp(
            visible = state.successfullySavedDishId != null,
            actionAfter = {
                state.successfullySavedDishId?.let { successNavigate(it) }
            }
        )
    }
}

@Composable
fun CalculationCard(
    state: CreateDishSummaryScreenState,
    modifier: Modifier = Modifier,
    onMarginChange: (String) -> Unit = {},
    onTaxChange: (String) -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FoodCost(state)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                FCCTextField(
                    title = stringResource(R.string.margin),
                    value = state.marginPercent,
                    onValueChange = onMarginChange,
                    suffix = { Text("%") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.size(12.dp))

                FCCTextField(
                    title = stringResource(R.string.tax),
                    value = state.taxPercent,
                    onValueChange = onTaxChange,
                    suffix = { Text("%") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            FinalPrice(state)
        }
    }
}

@Composable
private fun FinalPrice(state: CreateDishSummaryScreenState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(R.string.final_sell_price),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = Utils.formatPrice(state.finalSellingPrice, state.currency),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FoodCost(state: CreateDishSummaryScreenState) {
    Text(
        text = buildAnnotatedString {
            append(stringResource(R.string.create_dish_food_cost))
            append(" ")
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            ) {
                append(Utils.formatPrice(state.foodCost, state.currency))
            }
        },
        style = MaterialTheme.typography.headlineSmall
    )
}

@Preview
@PreviewLightDark
@Composable
private fun CreateDishSummaryScreenPreview() {
    FCCTheme {
        val sampleProduct1 = ProductAddedToDish(
            item = ProductDomain(
                id = 0L,
                name = "Lemon",
                pricePerUnit = 0.30,
                unit = MeasurementUnit.KILOGRAM,
                waste = 35.0,
                tax = 0.0
            ),
            quantity = 50.0,
            quantityUnit = (MeasurementUnit.GRAM)
        )
        val sampleProduct2 = ProductAddedToDish(
            item = ProductDomain(
                id = 2L,
                name = "Sugar",
                pricePerUnit = 0.15,
                unit = MeasurementUnit.KILOGRAM,
                waste = 0.0,
                tax = 0.0
            ),
            quantity = 30.0,
            quantityUnit = (MeasurementUnit.GRAM)
        )

        val previewState = CreateDishSummaryScreenState(
            dishName = "Lemonade",
            addedComponents = listOf(sampleProduct1, sampleProduct2),
            foodCost = 0.75,
            marginPercent = "300",
            taxPercent = "20",
            finalSellingPrice = 3.60,
            currency = Currency.getInstance("GBP"),
            isLoading = false,
            errorRes = null,
            successfullySavedDishId = 1L,
        )

        CreateDishSummaryContent(
            state = previewState,
            onBackClick = {},
            onSaveDishClick = {},
            onMarginChange = {},
            onTaxChange = {},
            successNavigate = {}
        )
    }
}

@Composable
fun SaveDefaultSettingsDialog(
    margin: String,
    tax: String,
    onSaveAsDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    FCCDialog(
        title = stringResource(R.string.save_default_settings_title),
        subtitle = stringResource(R.string.save_default_settings_message, margin, tax),
        onDismiss = onDismiss,
        primaryActionButton = {
            FCCTextButton(text = stringResource(R.string.save_as_default)) {
                onSaveAsDefault()
            }
        },
        secondaryActionButton = {
            FCCTextButton(text = stringResource(R.string.no_thanks)) {
                onDismiss()
            }
        }
    )
}

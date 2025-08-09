package com.erdees.foodcostcalc.ui.screens.products.createIngredient

import android.icu.util.Currency
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTopAppBarNavIconButton
import com.erdees.foodcostcalc.ui.composables.buttons.TwoOptionToggle
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.fields.UnitField
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.ui.screens.products.EditableProductUiState
import com.erdees.foodcostcalc.ui.screens.products.PackagePriceState
import com.erdees.foodcostcalc.ui.screens.products.UnitPriceState
import com.erdees.foodcostcalc.ui.screens.products.createProduct.CalculateWasteDialog
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.Utils
import java.util.Locale

data class CreateIngredientScreenActions(
    val onNameChange: (String) -> Unit = {},
    val onTaxChange: (String) -> Unit = {},
    val onWasteChange: (String) -> Unit = {},
    val onPackagePriceChange: (String) -> Unit = {},
    val onPackageQuantityChange: (String) -> Unit = {},
    val onPackageUnitChange: (MeasurementUnit) -> Unit = {},
    val onUnitPriceChange: (String) -> Unit = {},
    val onUnitPriceUnitChange: (MeasurementUnit) -> Unit = {},
    val onTogglePriceMode: () -> Unit = {},
    val onSaveIngredient: () -> Unit = {},
    val onCalculateWaste: () -> Unit = {},
    val onCalculateWasteResult: (Double?, Double?) -> Unit = { _, _ -> },
    val onResetScreenState: () -> Unit = {},
)

@Screen
@Composable
fun CreateIngredientScreen(
    navController: NavController,
    viewModel: CreateIngredientViewModel = viewModel(),
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val units by viewModel.units.collectAsState()
    val showTaxField by viewModel.showTaxField.collectAsState()
    val isSaveButtonEnabled by viewModel.isSaveButtonEnabled.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(screenState) {
        when (screenState) {
            is ScreenState.Success<*> -> {
                val addedProduct = (screenState as? ScreenState.Success<*>)?.data as? String
                val message = context.getString(R.string.item_added, addedProduct)
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
                viewModel.resetScreenState()
            }

            else -> {}
        }
    }

    val actions = CreateIngredientScreenActions(
        onNameChange = viewModel::onNameChanged,
        onTaxChange = viewModel::onTaxChanged,
        onWasteChange = viewModel::onWasteChanged,
        onPackagePriceChange = viewModel::onPackagePriceChanged,
        onPackageQuantityChange = viewModel::onPackageQuantityChanged,
        onPackageUnitChange = viewModel::onPackageUnitChanged,
        onUnitPriceChange = viewModel::onUnitPriceChanged,
        onUnitPriceUnitChange = viewModel::onUnitPriceUnitChanged,
        onTogglePriceMode = viewModel::togglePriceMode,
        onSaveIngredient = viewModel::saveIngredient,
        onCalculateWaste = viewModel::onCalculateWaste,
        onCalculateWasteResult = viewModel::calculateWaste,
        onResetScreenState = viewModel::resetScreenState,
    )

    CreateIngredientScreenContent(
        navController = navController,
        uiState = uiState,
        screenState = screenState,
        units = units,
        currency = currency,
        showTaxField = showTaxField,
        isSaveButtonEnabled = isSaveButtonEnabled,
        snackbarHostState = snackbarHostState,
        actions = actions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateIngredientScreenContent(
    navController: NavController,
    uiState: EditableProductUiState,
    screenState: ScreenState,
    units: Set<MeasurementUnit>,
    currency: Currency?,
    showTaxField: Boolean,
    isSaveButtonEnabled: Boolean,
    snackbarHostState: SnackbarHostState,
    actions: CreateIngredientScreenActions,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val wasInitialFocusRequested = rememberSaveable { mutableStateOf(false) }
    val ingredientNameFocusRequester = remember { FocusRequester() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.create_ingredient)) },
                navigationIcon = {
                    FCCTopAppBarNavIconButton(navController = navController)
                }
            )
        },
        bottomBar = {
            Column(
                Modifier.padding(horizontal = 16.dp)
            ) {
                SnackbarHost(snackbarHostState)
                FCCPrimaryButton(
                    enabled = isSaveButtonEnabled,
                    onClick = actions.onSaveIngredient,
                    text = stringResource(id = R.string.save_ingredient),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FCCTextField(
                    modifier = Modifier
                        .focusRequester(ingredientNameFocusRequester)
                        .onGloballyPositioned {
                            if (!wasInitialFocusRequested.value) {
                                wasInitialFocusRequested.value = true
                                ingredientNameFocusRequester.requestFocus()
                            }
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    title = stringResource(id = R.string.product_name),
                    value = uiState.name,
                    onValueChange = actions.onNameChange
                )

                TwoOptionToggle(
                    option1Text = stringResource(id = R.string.by_package),
                    option2Text = stringResource(id = R.string.by_unit),
                    selectedIndex = if (uiState is PackagePriceState) 0 else 1,
                    onSelectionChange = { index ->
                        val shouldToggle = (index == 0 && uiState !is PackagePriceState) ||
                                         (index == 1 && uiState is PackagePriceState)
                        if (shouldToggle) {
                            actions.onTogglePriceMode()
                        }
                    }
                )

                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "FormContent",
                    contentKey = { state -> state::class }
                ) { state ->
                    when (state) {
                        is PackagePriceState -> {
                            PackagePriceForm(
                                state = state,
                                units = units,
                                currency = currency,
                                onPackagePriceChange = actions.onPackagePriceChange,
                                onPackageQuantityChange = actions.onPackageQuantityChange,
                                onPackageUnitChange = actions.onPackageUnitChange,
                            )
                        }

                        is UnitPriceState -> {
                            UnitPriceForm(
                                state = state,
                                units = units,
                                onUnitPriceChange = actions.onUnitPriceChange,
                                onUnitPriceUnitChange = actions.onUnitPriceUnitChange
                            )
                        }
                    }
                }

                WasteField(
                    waste = uiState.waste,
                    showTaxField = showTaxField,
                    onWasteChange = actions.onWasteChange,
                    onCalculateWaste = actions.onCalculateWaste
                )

                if (showTaxField) {
                    FCCTextField(
                        title = stringResource(id = R.string.tax_percent),
                        value = uiState.tax,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        onValueChange = actions.onTaxChange
                    )
                }
            }

            ScreenStateHandler(
                screenState = screenState,
                onResetScreenState = actions.onResetScreenState,
                onCalculateWasteResult = actions.onCalculateWasteResult
            )
        }
    }
}

@Composable
private fun PackagePriceForm(
    state: PackagePriceState,
    currency: Currency?,
    units: Set<MeasurementUnit>,
    onPackagePriceChange: (String) -> Unit,
    onPackageQuantityChange: (String) -> Unit,
    onPackageUnitChange: (MeasurementUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FCCTextField(
            title = stringResource(id = R.string.package_price),
            value = state.packagePrice,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            onValueChange = onPackagePriceChange
        )

        FCCTextField(
            title = stringResource(
                id = R.string.package_quantity,
                stringResource(state.packageUnit.symbolRes)
            ),
            value = state.packageQuantity,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            onValueChange = onPackageQuantityChange
        )

        UnitField(
            units = units,
            selectedUnit = state.packageUnit,
            selectUnit = onPackageUnitChange,
            label = stringResource(R.string.package_unit)
        )

        if (state.canonicalPrice != null && state.canonicalUnit != null) {
            Text(
                text = stringResource(
                    id = R.string.calculated_unit_price,
                    Utils.formatPrice(state.canonicalPrice, currency),
                    stringResource(id = state.canonicalUnit.displayNameRes)
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun UnitPriceForm(
    state: UnitPriceState,
    units: Set<MeasurementUnit>,
    onUnitPriceChange: (String) -> Unit,
    onUnitPriceUnitChange: (MeasurementUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FCCTextField(
            title = stringResource(id = R.string.unit_price),
            value = state.unitPrice,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            onValueChange = onUnitPriceChange
        )

        UnitField(
            units = units,
            selectedUnit = state.unitPriceUnit,
            selectUnit = onUnitPriceUnitChange,
            label = stringResource(R.string.unit_price_unit)
        )
    }
}

@Composable
private fun WasteField(
    waste: String,
    showTaxField: Boolean,
    onWasteChange: (String) -> Unit,
    onCalculateWaste: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        FCCTextField(
            modifier = Modifier.weight(1f),
            title = stringResource(id = R.string.percent_of_waste),
            value = waste,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = if (showTaxField) ImeAction.Next else ImeAction.Done
            ),
            onValueChange = onWasteChange
        )

        FCCTextButton(
            text = stringResource(id = R.string.calculate),
            onClick = onCalculateWaste
        )
    }
}

@Composable
private fun ScreenStateHandler(
    screenState: ScreenState,
    onResetScreenState: () -> Unit,
    onCalculateWasteResult: (Double?, Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (screenState) {
            is ScreenState.Loading<*> -> {
                ScreenLoadingOverlay()
            }

            is ScreenState.Interaction -> {
                when (screenState.interaction) {
                    InteractionType.CalculateWaste -> {
                        CalculateWasteDialog(
                            onDismiss = onResetScreenState,
                            onSave = { totalQuantity, wasteQuantity ->
                                onCalculateWasteResult(totalQuantity, wasteQuantity)
                            }
                        )
                    }

                    else -> {}
                }
            }

            else -> {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateIngredientScreenPreview() {
    val navController = rememberNavController()

    FCCTheme {
        CreateIngredientScreenContent(
            navController = navController,
            uiState = PackagePriceState(
                name = "Sample Ingredient",
                packagePrice = "5.99",
                packageQuantity = "2.5",
                packageUnit = MeasurementUnit.KILOGRAM,
                canonicalPrice = 2.40,
                canonicalUnit = MeasurementUnit.KILOGRAM
            ),
            screenState = ScreenState.Idle,
            units = setOf(
                MeasurementUnit.KILOGRAM,
                MeasurementUnit.GRAM,
                MeasurementUnit.POUND
            ),
            showTaxField = false,
            isSaveButtonEnabled = true,
            snackbarHostState = remember { SnackbarHostState() },
            actions = CreateIngredientScreenActions(),
            currency = Currency.getInstance(Locale.getDefault())
        )
    }
}

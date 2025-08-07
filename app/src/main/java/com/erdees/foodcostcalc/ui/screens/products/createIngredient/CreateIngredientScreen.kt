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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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

    CreateIngredientScreenContent(
        navController = navController,
        uiState = uiState,
        screenState = screenState,
        units = units,
        currency = currency,
        showTaxField = showTaxField,
        isSaveButtonEnabled = isSaveButtonEnabled,
        snackbarHostState = snackbarHostState,
        onNameChanged = viewModel::onNameChanged,
        onTaxChanged = viewModel::onTaxChanged,
        onWasteChanged = viewModel::onWasteChanged,
        onPackagePriceChanged = viewModel::onPackagePriceChanged,
        onPackageQuantityChanged = viewModel::onPackageQuantityChanged,
        onPackageUnitChanged = viewModel::onPackageUnitChanged,
        onUnitPriceChanged = viewModel::onUnitPriceChanged,
        onUnitPriceUnitChanged = viewModel::onUnitPriceUnitChanged,
        onTogglePriceMode = viewModel::togglePriceMode,
        onSaveIngredient = viewModel::saveIngredient,
        onCalculateWaste = viewModel::onCalculateWaste,
        onCalculateWasteResult = viewModel::calculateWaste,
        onResetScreenState = viewModel::resetScreenState,
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
    onNameChanged: (String) -> Unit,
    onTaxChanged: (String) -> Unit,
    onWasteChanged: (String) -> Unit,
    onPackagePriceChanged: (String) -> Unit,
    onPackageQuantityChanged: (String) -> Unit,
    onPackageUnitChanged: (MeasurementUnit) -> Unit,
    onUnitPriceChanged: (String) -> Unit,
    onUnitPriceUnitChanged: (MeasurementUnit) -> Unit,
    onTogglePriceMode: () -> Unit,
    onSaveIngredient: () -> Unit,
    onCalculateWaste: () -> Unit,
    onCalculateWasteResult: (Double?, Double?) -> Unit,
    onResetScreenState: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }

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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FCCTextField(
                modifier = Modifier.focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                title = stringResource(id = R.string.product_name),
                value = uiState.name,
                onValueChange = onNameChanged
            )

            PriceModeToggle(
                isPackageMode = uiState is PackagePriceState,
                onToggle = onTogglePriceMode
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
                            onPackagePriceChanged = onPackagePriceChanged,
                            onPackageQuantityChanged = onPackageQuantityChanged,
                            onPackageUnitChanged = onPackageUnitChanged,
                        )
                    }

                    is UnitPriceState -> {
                        UnitPriceForm(
                            state = state,
                            units = units,
                            onUnitPriceChanged = onUnitPriceChanged,
                            onUnitPriceUnitChanged = onUnitPriceUnitChanged
                        )
                    }
                }
            }

            WasteField(
                waste = uiState.waste,
                onWasteChanged = onWasteChanged,
                onCalculateWaste = onCalculateWaste
            )

            if (showTaxField) {
                FCCTextField(
                    title = stringResource(id = R.string.tax_percent),
                    value = uiState.tax,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    onValueChange = onTaxChanged
                )
            }

            // Save button
            FCCPrimaryButton(
                enabled = isSaveButtonEnabled,
                onClick = onSaveIngredient,
                text = stringResource(id = R.string.save_ingredient),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }

    ScreenStateHandler(
        screenState = screenState,
        onResetScreenState = onResetScreenState,
        onCalculateWasteResult = onCalculateWasteResult
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceModeToggle(
    isPackageMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by remember(isPackageMode) {
        mutableIntStateOf(if (isPackageMode) 0 else 1)
    }

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            onClick = {
                if (selectedIndex != 0) {
                    selectedIndex = 0
                    onToggle()
                }
            },
            selected = selectedIndex == 0
        ) {
            Text(stringResource(id = R.string.by_package))
        }
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            onClick = {
                if (selectedIndex != 1) {
                    selectedIndex = 1
                    onToggle()
                }
            },
            selected = selectedIndex == 1
        ) {
            Text(stringResource(id = R.string.by_unit))
        }
    }
}

@Composable
private fun PackagePriceForm(
    state: PackagePriceState,
    currency: Currency?,
    units: Set<MeasurementUnit>,
    onPackagePriceChanged: (String) -> Unit,
    onPackageQuantityChanged: (String) -> Unit,
    onPackageUnitChanged: (MeasurementUnit) -> Unit,
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
            onValueChange = onPackagePriceChanged
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
            onValueChange = onPackageQuantityChanged
        )

        UnitField(
            units = units,
            selectedUnit = state.packageUnit,
            selectUnit = onPackageUnitChanged,
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
    onUnitPriceChanged: (String) -> Unit,
    onUnitPriceUnitChanged: (MeasurementUnit) -> Unit,
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
            onValueChange = onUnitPriceChanged
        )

        UnitField(
            units = units,
            selectedUnit = state.unitPriceUnit,
            selectUnit = onUnitPriceUnitChanged,
            label = stringResource(R.string.unit_price_unit)
        )
    }
}

@Composable
private fun WasteField(
    waste: String,
    onWasteChanged: (String) -> Unit,
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
                imeAction = ImeAction.Next
            ),
            onValueChange = onWasteChanged
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
            onNameChanged = {},
            onTaxChanged = {},
            onWasteChanged = {},
            onPackagePriceChanged = {},
            onPackageQuantityChanged = {},
            onPackageUnitChanged = {},
            onUnitPriceChanged = {},
            onUnitPriceUnitChanged = {},
            onTogglePriceMode = {},
            onSaveIngredient = {},
            onCalculateWaste = {},
            onCalculateWasteResult = { _, _ -> },
            onResetScreenState = {},
            currency = Currency.getInstance(Locale.getDefault())
        )
    }
}

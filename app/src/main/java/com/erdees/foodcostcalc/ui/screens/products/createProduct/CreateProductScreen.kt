package com.erdees.foodcostcalc.ui.screens.products.createProduct

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTopAppBarNavIconButton
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.fields.UnitField
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Screen
@Composable
fun CreateProductScreen(
    navController: NavController,
    viewModel: CreateProductScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    val productName by viewModel.productName.collectAsState()
    val productPrice by viewModel.productPrice.collectAsState()
    val productTax by viewModel.productTax.collectAsState()
    val productWaste by viewModel.productWaste.collectAsState()
    val units by viewModel.units.collectAsState()
    val selectedUnit by viewModel.selectedUnit.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val addButtonEnabled by viewModel.addButtonEnabled.collectAsState()
    val countPiecePriceEnabled by viewModel.countPiecePriceEnabled.collectAsState()
    val showTaxPercent by viewModel.showTaxPercent.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.getUnits(context.resources)
    }

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

    CreateProductScreenContent(
        navController = navController,
        state = CreateProductScreenUiState(
            productName,
            productPrice,
            productTax,
            productWaste,
            units,
            selectedUnit,
            screenState,
            addButtonEnabled,
            countPiecePriceEnabled,
            showTaxPercent,
            snackbarHostState
        ),
        actions = CreateProductScreenActions(
            addProduct = viewModel::addProduct,
            onCalculateWaste = viewModel::onCalculateWaste,
            onCalculatePiecePrice = viewModel::onCalculatePiecePrice,
            resetScreenState = viewModel::resetScreenState,
            selectUnit = viewModel::selectUnit,
            updateProductName = viewModel::updateProductName,
            updateProductPrice = viewModel::updateProductPrice,
            updateProductTax = viewModel::updateProductTax,
            updateProductWaste = viewModel::updateProductWaste,
            calculateWaste = viewModel::calculateWaste,
            calculatePricePerPiece = viewModel::calculatePricePerPiece
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateProductScreenContent(
    navController: NavController,
    state: CreateProductScreenUiState,
    actions: CreateProductScreenActions,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    var textFieldLoaded by rememberSaveable { mutableStateOf(false) }

    with(state) {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.create_product)) },
                    navigationIcon = {
                        FCCTopAppBarNavIconButton(navController = navController)
                    }
                )
            },
        ) { paddingValues ->
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp)
                    .verticalScroll(scrollState)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FCCTextField(
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onGloballyPositioned {
                                if (!textFieldLoaded) {
                                    focusRequester.requestFocus()
                                    textFieldLoaded = true
                                }
                            },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        title = stringResource(id = R.string.product_name),
                        value = productName,
                        onValueChange = { actions.updateProductName(it) })
                    FCCTextField(
                        title = stringResource(id = R.string.price),
                        value = productPrice,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        onValueChange = { actions.updateProductPrice(it) })
                    if (showTaxPercent) {
                        FCCTextField(
                            title = stringResource(id = R.string.tax_percent),
                            value = productTax,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            onValueChange = { actions.updateProductTax(it) })
                    }
                    FCCTextField(
                        title = stringResource(id = R.string.percent_of_waste),
                        value = productWaste,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        onValueChange = { actions.updateProductWaste(it) })

                    UnitField(
                        units = units,
                        selectedUnit = selectedUnit,
                        selectUnit = { actions.selectUnit(it) })
                }

                Column {
                    SnackbarHost(snackbarHostState)

                    CreateProductScreenButtonRow(
                        addButtonEnabled = isAddButtonEnabled,
                        countPiecePriceEnabled = isCountPiecePriceEnabled,
                        onAdd = { actions.addProduct() },
                        onCalculatePiecePrice = { actions.onCalculatePiecePrice() },
                        onCalculateWaste = { actions.onCalculateWaste() },
                    )
                }
            }
        }
        ScreenStateHandler(
            screenState,
            { actions.resetScreenState() },
            { num1, num2 -> actions.calculateWaste(num1, num2) },
            { num1, num2 -> actions.calculatePricePerPiece(num1, num2) },
        )
    }
}

@Composable
private fun ScreenStateHandler(
    screenState: ScreenState,
    resetScreenState: () -> Unit,
    calculateWaste: (Double?, Double?) -> Unit,
    calculatePricePerPiece: (Double?, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (screenState) {
            is ScreenState.Success<*> -> {}
            is ScreenState.Error -> {}
            is ScreenState.Loading<*> -> {
                ScreenLoadingOverlay()
            }

            is ScreenState.Interaction -> {
                when (screenState.interaction) {
                    InteractionType.CalculateWaste -> {
                        CalculateWasteDialog(
                            onDismiss = { resetScreenState() },
                            onSave = { totalQuantity, wasteQuantity ->
                                calculateWaste(totalQuantity, wasteQuantity)
                            }
                        )
                    }

                    InteractionType.CalculatePiecePrice -> {
                        CalculatePiecePriceDialog(
                            onDismiss = { resetScreenState() },
                            onSave = { boxPrice, quantityInBox ->
                                calculatePricePerPiece(boxPrice, quantityInBox)
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

@Composable
private fun CreateProductScreenButtonRow(
    addButtonEnabled: Boolean,
    countPiecePriceEnabled: Boolean,
    onAdd: () -> Unit,
    onCalculatePiecePrice: () -> Unit,
    onCalculateWaste: () -> Unit,
    modifier: Modifier = Modifier
) {
    ButtonRow(
        applyDefaultPadding = false,
        modifier = modifier
            .padding(bottom = 24.dp, top = 12.dp)
            .fillMaxWidth(),
        primaryButton = {
            FCCPrimaryButton(
                enabled = addButtonEnabled,
                onClick = {
                    onAdd()
                },
                text = stringResource(id = R.string.add)
            )
        }, secondaryButton = {
            FCCTextButton(
                text = stringResource(id = R.string.count_piece_price),
                enabled = countPiecePriceEnabled
            ) {
                onCalculatePiecePrice()
            }
        },
        tertiaryButton = {
            FCCTextButton(text = stringResource(id = R.string.count_waste)) {
                onCalculateWaste()
            }
        }
    )
}

@Preview
@Composable
private fun PreviewButtonRow() {
    CreateProductScreenButtonRow(
        modifier = Modifier,
        addButtonEnabled = true,
        countPiecePriceEnabled = true,
        onAdd = { },
        onCalculatePiecePrice = { },
        onCalculateWaste = { })
}

@Preview(showBackground = true, name = "CreateProductScreen Content - Default")
@Composable
private fun CreateProductScreenContentPreview() {
    val navController = rememberNavController()

    // Sample data for the UI State
    val sampleUiState = CreateProductScreenUiState(
        productName = "Delicious Cake",
        productPrice = "15.99",
        productTax = "10",
        productWaste = "5",
        units = setOf("kg", "g", "lbs", "oz", "pcs", "slice"),
        selectedUnit = "kg",
        screenState = ScreenState.Idle, // Or ScreenState.Success if you want to see the snackbar effect
        isAddButtonEnabled = true,
        isCountPiecePriceEnabled = true,
        showTaxPercent = true,
        snackbarHostState = remember { SnackbarHostState() } // Important for SnackbarHost
    )

    // Dummy actions - they don't need to do much in a UI preview
    val sampleActions = CreateProductScreenActions(
        addProduct = { println("Preview: Add Product Clicked") },
        onCalculateWaste = { println("Preview: Calculate Waste Clicked") },
        onCalculatePiecePrice = { println("Preview: Calculate Piece Price Clicked") },
        resetScreenState = { println("Preview: Reset Screen State") },
        selectUnit = { unit -> println("Preview: Unit Selected - $unit") },
        updateProductName = { name -> println("Preview: Product Name Updated - $name") },
        updateProductPrice = { price -> println("Preview: Product Price Updated - $price") },
        updateProductTax = { tax -> println("Preview: Product Tax Updated - $tax") },
        updateProductWaste = { waste -> println("Preview: Product Waste Updated - $waste") },
        calculateWaste = { total, waste -> println("Preview: Calculate Waste - Total: $total, Waste: $waste") },
        calculatePricePerPiece = { boxPrice, quantity -> println("Preview: Calculate Piece Price - Price: $boxPrice, Qty: $quantity") }
    )

    // You might want to wrap this in your app's theme for accurate visual representation
    // FCCTheme {
    CreateProductScreenContent(
        navController = navController,
        state = sampleUiState,
        actions = sampleActions
    )
    // }
}

@Preview(showBackground = true, name = "CreateProductScreen Content - Tax Hidden")
@Composable
private fun CreateProductScreenContentTaxHiddenPreview() {
    val navController = rememberNavController()
    val sampleUiState = CreateProductScreenUiState(
        productName = "Simple Bread",
        productPrice = "3.50",
        productTax = "0", // Will be hidden
        productWaste = "2",
        units = setOf("loaf", "slice", "g"),
        selectedUnit = "loaf",
        screenState = ScreenState.Idle,
        isAddButtonEnabled = true,
        isCountPiecePriceEnabled = false, // Example: piece price not relevant
        showTaxPercent = false, // Key difference for this preview
        snackbarHostState = remember { SnackbarHostState() }
    )
    val sampleActions = CreateProductScreenActions(
        addProduct = {}, onCalculateWaste = {}, onCalculatePiecePrice = {},
        resetScreenState = {}, selectUnit = {}, updateProductName = {},
        updateProductPrice = {}, updateProductTax = {}, updateProductWaste = {},
        calculateWaste = { _, _ -> }, calculatePricePerPiece = { _, _ -> }
    )

    // FCCTheme {
    CreateProductScreenContent(
        navController = navController,
        state = sampleUiState,
        actions = sampleActions
    )
    // }
}

@Preview(showBackground = true, name = "CreateProductScreen Content - Loading State")
@Composable
private fun CreateProductScreenContentLoadingPreview() {
    val navController = rememberNavController()
    val sampleUiState = CreateProductScreenUiState(
        productName = "",
        productPrice = "",
        productTax = "",
        productWaste = "",
        units = setOf("kg", "g"),
        selectedUnit = "kg",
        screenState = ScreenState.Loading<Nothing>(),
        isAddButtonEnabled = false,
        isCountPiecePriceEnabled = false,
        showTaxPercent = true,
        snackbarHostState = remember { SnackbarHostState() }
    )
    val sampleActions = CreateProductScreenActions(
        addProduct = {}, onCalculateWaste = {}, onCalculatePiecePrice = {},
        resetScreenState = {}, selectUnit = {}, updateProductName = {},
        updateProductPrice = {}, updateProductTax = {}, updateProductWaste = {},
        calculateWaste = { _, _ -> }, calculatePricePerPiece = { _, _ -> }
    )

    FCCTheme {
        CreateProductScreenContent(
            navController = navController,
            state = sampleUiState,
            actions = sampleActions
        )
    }
}
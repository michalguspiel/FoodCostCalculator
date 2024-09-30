package com.erdees.foodcostcalc.ui.screens.products.createProduct

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.fields.UnitField
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTopAppBarNavIconButton
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCDialog
import com.erdees.foodcostcalc.utils.onIntegerValueChange
import com.erdees.foodcostcalc.utils.onNumericValueChange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductScreen(modifier: Modifier = Modifier, navController: NavController) {

    val context = LocalContext.current
    val viewModel: CreateProductScreenViewModel = viewModel()

    val productName by viewModel.productName.collectAsState()
    val productPrice by viewModel.productPrice.collectAsState()
    val productTax by viewModel.productTax.collectAsState()
    val productWaste by viewModel.productWaste.collectAsState()
    val units by viewModel.units.collectAsState()
    val selectedUnit by viewModel.selectedUnit.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val addButtonEnabled by viewModel.addButtonEnabled.collectAsState()
    val countPiecePriceEnabled by viewModel.countPiecePriceEnabled.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val focusRequester = remember { FocusRequester() }
    // Inside the CreateProductScreen Composable function
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.getUnits(context.resources)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(screenState) {
        when (screenState) {
            is ScreenState.Success -> {
                snackbarHostState.showSnackbar("Item added.", duration = SnackbarDuration.Short)
                viewModel.resetScreenState()
            }

            else -> {}
        }
    }


    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Create Product") },
                navigationIcon = {
                    FCCTopAppBarNavIconButton(navController = navController)
                }
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp)
                    .padding(horizontal = 12.dp)
                    .verticalScroll(scrollState)
            ) {

                Column(
                    Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FCCTextField(
                        modifier = Modifier.focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        title = "Product name",
                        value = productName,
                        onValueChange = { viewModel.updateProductName(it) })
                    FCCTextField(
                        title = "Price",
                        value = productPrice,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        onValueChange = { viewModel.updateProductPrice(it) })
                    FCCTextField(
                        title = "Tax %",
                        value = productTax,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        onValueChange = { viewModel.updateProductTax(it) })
                    FCCTextField(
                        title = "% of waste",
                        value = productWaste,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        onValueChange = { viewModel.updateProductWaste(it) })

                    UnitField(
                        units = units,
                        selectedUnit = selectedUnit,
                        selectUnit = { viewModel.selectUnit(it) }
                    )
                }

                ButtonRow(
                    addButtonEnabled = addButtonEnabled,
                    countPiecePriceEnabled = countPiecePriceEnabled,
                    onAdd = viewModel::addProduct,
                    onCalculatePiecePrice = viewModel::onCalculatePiecePrice,
                    onCalculateWaste = viewModel::onCalculateWaste
                )
            }


            when (screenState) {
                is ScreenState.Success -> {}
                is ScreenState.Error -> {}
                is ScreenState.Loading -> {
                    ScreenLoadingOverlay()
                }

                is ScreenState.Interaction -> {
                    when ((screenState as ScreenState.Interaction).interaction) {
                        InteractionType.CalculateWaste -> {
                            CalculateWasteDialog(
                                onDismiss = { viewModel.resetScreenState() },
                                onSave = { totalQuantity, wasteQuantity ->
                                    viewModel.calculateWaste(
                                        totalQuantity = totalQuantity,
                                        wasteQuantity = wasteQuantity
                                    )
                                }
                            )
                        }

                        InteractionType.CalculatePiecePrice -> {
                            CalculatePiecePriceDialog(
                                onDismiss = { viewModel.resetScreenState() },
                                onSave = { boxPrice, quantityInBox ->
                                    viewModel.calculatePricePerPiece(
                                        boxPrice = boxPrice,
                                        quantityInBox = quantityInBox
                                    )
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
}

@Composable
fun CalculateWasteDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSave: (totalQuantity: Double?, wasteQuantity: Double?) -> Unit
) {
    var totalQuantity by remember {
        mutableStateOf("")
    }

    var wasteQuantity by remember {
        mutableStateOf("")
    }

    FCCDialog(
        modifier = modifier,
        title = "Count waste",
        onDismiss = { onDismiss() },
        onPrimaryButtonClicked = { onSave(totalQuantity.toDoubleOrNull(), wasteQuantity.toDoubleOrNull()) }) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            FCCTextField(
                title = "Quantity before processing",
                value = totalQuantity,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            ) {
                totalQuantity = onNumericValueChange(oldValue = totalQuantity, newValue = it)
            }

            FCCTextField(
                title = "Waste quantity",
                value = wasteQuantity,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            ) {
                wasteQuantity = onNumericValueChange(oldValue = wasteQuantity, newValue = it)
            }

        }
    }
}

@Composable
fun CalculatePiecePriceDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSave: (boxPrice: Double?, quantityInBox: Int?) -> Unit
) {
    var boxPrice by remember {
        mutableStateOf("")
    }

    var quantityInBox by remember {
        mutableStateOf("")
    }

    FCCDialog(
        modifier = modifier,
        title = "Count waste",
        onDismiss = { onDismiss() },
        onPrimaryButtonClicked = { onSave(boxPrice.toDoubleOrNull(), quantityInBox.toIntOrNull()) }) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            FCCTextField(
                title = "Box price", value = boxPrice,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            ) {
                boxPrice = onNumericValueChange(oldValue = boxPrice, newValue = it)
            }

            FCCTextField(
                title = "Box quantity", value = quantityInBox,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            ) {
                quantityInBox = onIntegerValueChange(oldValue = quantityInBox, newValue = it)
            }

        }
    }
}


@Composable
private fun ButtonRow(
    addButtonEnabled: Boolean,
    countPiecePriceEnabled: Boolean,
    modifier: Modifier = Modifier,
    onAdd: () -> Unit,
    onCalculatePiecePrice: () -> Unit,
    onCalculateWaste: () -> Unit
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        FCCTextButton(text = "Count waste") {
            onCalculateWaste()
        }
        Spacer(modifier = Modifier.width(8.dp))
        FCCTextButton(text = "Count piece price", enabled = countPiecePriceEnabled) {
            onCalculatePiecePrice()
        }
        Spacer(modifier = Modifier.width(8.dp))
        FCCPrimaryButton(
            enabled = addButtonEnabled,
            onClick = {
                onAdd()
            },
            text = "Add"
        )
    }
}

@Preview
@Composable
private fun PreviewButtonRow() {
    ButtonRow(
        addButtonEnabled = true,
        countPiecePriceEnabled = true,
        onAdd = { },
        onCalculatePiecePrice = { }) {

    }
}
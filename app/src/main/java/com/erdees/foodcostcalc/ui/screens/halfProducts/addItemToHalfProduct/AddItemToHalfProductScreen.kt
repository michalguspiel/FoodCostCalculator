package com.erdees.foodcostcalc.ui.screens.halfProducts.addItemToHalfProduct

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.fields.AddItemFields
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel
import com.erdees.foodcostcalc.ui.screens.dishes.addItemToDish.SelectedTab
import com.erdees.foodcostcalc.utils.UnitsUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemToHalfProductScreen(
    navController: NavController,
    halfProductId: Long,
    halfProductName: String,
    halfProductUnit: String
) {

    val viewModel: AddItemToHalfProductViewModel = viewModel()
    val products by viewModel.products.collectAsState()
    val units by viewModel.units.collectAsState()
    val selectedUnit by viewModel.selectedUnit.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val pieceWeight by viewModel.pieceWeight.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val addButtonEnabled by viewModel.addButtonEnabled.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()

    val itemAddedText = stringResource(id = R.string.item_added)

    LaunchedEffect(halfProductUnit) {
        viewModel.initializeWith(halfProductUnit)
    }

    LaunchedEffect(screenState) {
        when (screenState) {
            is ScreenState.Success -> {
                snackbarHostState.showSnackbar(itemAddedText, duration = SnackbarDuration.Short)
                viewModel.resetScreenState()
            }

            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(title = {
                Text(text = halfProductName)
            }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Sharp.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            }, actions = {
                IconButton(onClick = {
                    scope.launch {
                        tooltipState.show(MutatePriority.PreventUserInput)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Sharp.Info,
                        contentDescription = stringResource(id = R.string.content_description_half_product_tip)
                    )
                }
            }
            )
        }) { paddingValues: PaddingValues ->
        Box(Modifier.padding(paddingValues), contentAlignment = Alignment.Center) {

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp)
                    .padding(horizontal = 12.dp)
            ) {
                AddItemFields(
                    items = products,
                    units = units,
                    quantity = quantity,
                    selectedTab = SelectedTab.ADD_PRODUCT,
                    selectedItem = selectedProduct,
                    selectedUnit = selectedUnit,
                    selectItem = { viewModel.selectProduct(it as ProductDomain) },
                    selectUnit = viewModel::selectUnit,
                    setQuantity = viewModel::setQuantity,
                    extraField = {
                        if (viewModel.pieceQuantityNeeded()) {
                            PieceWeightField(
                                modifier = Modifier.fillMaxWidth(),
                                halfProductUnit = halfProductUnit,
                                pieceWeight = pieceWeight,
                                setPieceWeight = viewModel::setPieceWeight
                            )
                        }
                    }
                )

                TooltipBox(
                    positionProvider =
                    TooltipDefaults.rememberPlainTooltipPositionProvider(), tooltip = {
                        RichTooltip(
                            title = { Text(stringResource(id = R.string.tip)) },
                            text = { Text(text = stringResource(id = R.string.half_product_tip_content)) }
                        )
                    }, state = tooltipState
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        FCCPrimaryButton(
                            enabled = addButtonEnabled,
                            onClick = { viewModel.addHalfProduct(halfProductId) },
                            text = stringResource(id = R.string.add)
                        )
                    }
                }
            }

            when (screenState) {
                is ScreenState.Loading -> ScreenLoadingOverlay()
                is ScreenState.Error -> {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetScreenState() },
                        title = { Text(stringResource(id = R.string.error)) },
                        text = { Text(stringResource(id = R.string.something_went_wrong)) },
                        confirmButton = {
                            Button(onClick = { viewModel.resetScreenState() }) {
                                Text(stringResource(id = R.string.okay))
                            }
                        }
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
fun PieceWeightField(
    halfProductUnit: String,
    pieceWeight: String,
    modifier: Modifier = Modifier,
    setPieceWeight: (String) -> Unit,
) {
    Column(modifier) {
        FieldLabel(
            text = UnitsUtils.getPerUnitAsDescription(halfProductUnit),
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = pieceWeight,
            singleLine = true,
            onValueChange = { newValue ->
                setPieceWeight(newValue)
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.End
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}


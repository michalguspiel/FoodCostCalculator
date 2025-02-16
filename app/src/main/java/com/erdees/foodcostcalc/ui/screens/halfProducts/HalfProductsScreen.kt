package com.erdees.foodcostcalc.ui.screens.halfProducts

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.Ad
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ItemPresentationState
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.ui.composables.Ad
import com.erdees.foodcostcalc.ui.composables.ExpandedIcon
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.animations.SearchFieldTransition
import com.erdees.foodcostcalc.ui.composables.buttons.FCCAnimatedFAB
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog
import com.erdees.foodcostcalc.ui.composables.dividers.FCCPrimaryHorizontalDivider
import com.erdees.foodcostcalc.ui.composables.dividers.FCCSecondaryHorizontalDivider
import com.erdees.foodcostcalc.ui.composables.fields.SearchField
import com.erdees.foodcostcalc.ui.composables.fields.UnitField
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel
import com.erdees.foodcostcalc.ui.composables.rememberNestedScrollConnection
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.composables.rows.IngredientRow
import com.erdees.foodcostcalc.ui.composables.rows.PriceRow
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.UnitsUtils.getPerUnitAbbreviation
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.onNumericValueChange

@Screen
@Composable
fun HalfProductsScreen(
    navController: NavController,
    viewModel: HalfProductsScreenViewModel = viewModel()
) {

    val adItems by viewModel.filteredHalfProductsInjectedWithAds.collectAsState()
    val searchKey by viewModel.searchKey.collectAsState()
    val isVisible = remember { mutableStateOf(true) }
    val nestedScrollConnection = rememberNestedScrollConnection(isVisible)
    val screenState by viewModel.screenState.collectAsState()

    Scaffold(modifier = Modifier, floatingActionButton = {
        FCCAnimatedFAB(
            isVisible = isVisible.value,
            contentDescription = stringResource(id = R.string.content_description_create_half_product)
        ) {
            viewModel.updateScreenState(ScreenState.Interaction(InteractionType.CreateHalfProduct))
        }
    }) { paddingValues ->
        Box(
            contentAlignment = Alignment.TopCenter, modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(
                Modifier
                    .nestedScroll(nestedScrollConnection)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = (36 + 8 + 8).dp)
            ) {
                items(adItems) { item ->

                    when (item) {
                        is Ad -> {
                            Ad(
                                modifier = Modifier.padding(vertical = 8.dp),
                                adUnitId = if (BuildConfig.DEBUG) Constants.Ads.ADMOB_TEST_AD_UNIT_ID
                                else Constants.Ads.ADMOB_HALF_PRODUCTS_AD_UNIT_ID,
                                onAdFailedToLoad = viewModel::onAdFailedToLoad
                            )
                        }

                        is HalfProductDomain -> {
                            key(item) {
                                val itemPresentationState =
                                    viewModel.listPresentationStateHandler.itemsPresentationState.collectAsState().value[item.id]
                                        ?: ItemPresentationState()
                                HalfProductItem(halfProductDomain = item,
                                    isExpanded = itemPresentationState.isExpanded,
                                    quantity = itemPresentationState.quantity,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    onExpandToggle = {
                                        viewModel.listPresentationStateHandler.onExpandToggle(item)
                                    },
                                    onEditQuantity = {
                                        viewModel.onEditQuantity(item.id)
                                    },
                                    onAddItemsClick = {
                                        navController.navigate(
                                            FCCScreen.AddItemToHalfProduct(
                                                item.id, item.name, item.halfProductUnit
                                            )
                                        )
                                    },
                                    onEditClick = {
                                        navController.navigate(
                                            FCCScreen.EditHalfProduct(
                                                halfProductId = item.id
                                            )
                                        )
                                    })
                            }
                        }
                    }
                }
            }
        }

        SearchFieldTransition(isVisible = isVisible.value) {
            SearchField(
                modifier = Modifier, value = searchKey, onValueChange = viewModel::updateSearchKey
            )
        }

        when (screenState) {

            is ScreenState.Loading -> {
                ScreenLoadingOverlay()
            }

            is ScreenState.Error -> {
                ErrorDialog {
                    viewModel.resetScreenState()
                }
            }

            is ScreenState.Interaction -> {
                when (val interactionType = (screenState as ScreenState.Interaction).interaction) {

                    InteractionType.CreateHalfProduct -> {
                        CreateHalfProductDialog(
                            units = Utils.getUnitsSet(
                                LocalContext.current.resources, viewModel.preferences
                            ), onSave = { name, unit ->
                                viewModel.addHalfProduct(name = name, unit = unit)
                            }, onDismiss = viewModel::resetScreenState
                        )
                    }

                    is InteractionType.EditQuantity -> {
                        val itemId = interactionType.itemId
                        val itemPresentationState =
                            viewModel.listPresentationStateHandler.itemsPresentationState.collectAsState().value[itemId]
                                ?: ItemPresentationState()
                        val editableQuantity =
                            remember { mutableStateOf(itemPresentationState.quantity.toString()) }
                        ValueEditDialog(title = stringResource(id = R.string.edit_quantity),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            value = editableQuantity.value,
                            updateValue = { newValue ->
                                editableQuantity.value =
                                    onNumericValueChange(editableQuantity.value, newValue)
                            },
                            onSave = {
                                viewModel.listPresentationStateHandler.updatePresentationQuantity(
                                    itemId, editableQuantity.value
                                )
                            },
                            onDismiss = {
                                viewModel.resetScreenState()
                            })
                    }

                    else -> {
                        // Nothing special here
                    }
                }
            }

            else -> {
                // Nothing special here
            }

        }
    }
}

@Composable
fun HalfProductItem(
    halfProductDomain: HalfProductDomain,
    isExpanded: Boolean,
    quantity: Double,
    modifier: Modifier = Modifier,
    onExpandToggle: () -> Unit,
    onEditQuantity: () -> Unit,
    onAddItemsClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier
            .fillMaxWidth()
            .clickable { onExpandToggle() }, content = {
            Column(Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {

                TitleRow(halfProductDomain, isExpanded)

                if (isExpanded) {
                    Ingredients(quantity, halfProductDomain, onEditQuantity, context)
                }

                PriceSummary(halfProductDomain, context, quantity)

                FCCPrimaryHorizontalDivider(Modifier.padding(top = 8.dp, bottom = 12.dp))

                ButtonRow(applyDefaultPadding = false, secondaryButton = {
                    FCCTextButton(text = stringResource(id = R.string.edit)) { onEditClick() }
                }, primaryButton = {
                    FCCPrimaryButton(text = stringResource(id = R.string.add_items)) { onAddItemsClick() }
                })
            }
        })
}

@Composable
private fun TitleRow(
    halfProductDomain: HalfProductDomain,
    isExpanded: Boolean
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = halfProductDomain.name, style = MaterialTheme.typography.titleLarge)
        ExpandedIcon(isExpanded = isExpanded)
    }
}

@Composable
private fun Ingredients(
    quantity: Double,
    halfProductDomain: HalfProductDomain,
    onChangeQuantityDialogOpen: () -> Unit,
    context: Context
) {
    Column(
        Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FCCTextButton(
            text = stringResource(
                id = R.string.recipe_per_quantity,
                quantity.toString(),
                getPerUnitAbbreviation(halfProductDomain.halfProductUnit)
            )
        ) {
            onChangeQuantityDialogOpen()
        }

        halfProductDomain.products.forEach {
            IngredientRow(
                modifier = Modifier.padding(bottom = 4.dp),
                description = it.item.name,
                quantity = stringResource(
                    id = R.string.formatted_quantity,
                    it.formatQuantityForTargetServing(
                        baseQuantity = halfProductDomain.totalQuantity,
                        targetQuantity = quantity
                    ),
                    UnitsUtils.getUnitAbbreviation(unit = it.quantityUnit)
                ),
                price = it.formattedTotalPriceForTargetQuantity(
                    context = context,
                    targetQuantity = quantity,
                    baseQuantity = halfProductDomain.totalQuantity
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            FCCSecondaryHorizontalDivider()
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PriceSummary(
    halfProductDomain: HalfProductDomain,
    context: Context,
    quantity: Double
) {
    PriceRow(
        description = stringResource(id = R.string.price_per_recipe),
        price = halfProductDomain.formattedPricePresentedRecipe(
            targetQuantity = quantity,
            baseQuantity = halfProductDomain.totalQuantity,
            context = context
        )
    )
    Spacer(modifier = Modifier.height(4.dp))
    PriceRow(
        description = stringResource(
            id = R.string.price_per_unit, halfProductDomain.halfProductUnit
        ), price = halfProductDomain.formattedPricePerUnit(context)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHalfProductDialog(
    units: Set<String>,
    modifier: Modifier = Modifier,
    onSave: (name: String, unit: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }

    var selectedUnit by remember {
        mutableStateOf(units.firstOrNull() ?: "")
    }

    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = { onDismiss() },
    ) {
        Column(
            Modifier
                .background(
                    MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(id = R.string.create_half_product),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.size(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                Column {
                    FieldLabel(
                        text = stringResource(id = R.string.name),
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = name, onValueChange = { value ->
                            name = value
                        }, singleLine = true, maxLines = 1, keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words,
                        )
                    )
                }
                UnitField(units = units,
                    selectedUnit = selectedUnit,
                    selectUnit = { selectedUnit = it })
            }

            Spacer(modifier = Modifier.size(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FCCTextButton(text = stringResource(id = R.string.save)) {
                    onSave(name, selectedUnit)
                }
            }
        }
    }
}

@Preview
@Composable
private fun HalfProductsItemPreview() {
    FCCTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HalfProductItem(halfProductDomain = HalfProductDomain(
                id = 1,
                name = "Mayonnaise",
                halfProductUnit = "per kilogram",
                products = listOf(
                    UsedProductDomain(
                        1,
                        2,
                        ProductDomain(1, "Egg", 2.0, 0.5, 5.0, "kg"),
                        1.0,
                        "pcs",
                        1.0,
                    ), UsedProductDomain(
                        1,
                        2,
                        ProductDomain(2, "Oil", 15.0, 0.5, 5.0, "kg"),
                        100.0,
                        "g",
                        null,
                    )
                )
            ),
                isExpanded = true,
                quantity = 1.0,
                onExpandToggle = { },
                onEditQuantity = { },
                onAddItemsClick = { }) {}
            HalfProductItem(halfProductDomain = HalfProductDomain(
                id = 1, name = "Ketchup", halfProductUnit = "kg", products = emptyList()
            ),
                isExpanded = false,
                quantity = 2.0,
                onExpandToggle = { },
                onEditQuantity = { },
                onAddItemsClick = { }) {}
        }
    }
}
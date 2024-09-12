package com.erdees.foodcostcalc.ui.screens.halfProducts

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.HalfProductCardViewBinding
import com.erdees.foodcostcalc.domain.model.Ad
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ItemPresentationState
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.ui.composables.Ad
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.SearchField
import com.erdees.foodcostcalc.ui.composables.UnitField
import com.erdees.foodcostcalc.ui.composables.animations.SearchFieldTransition
import com.erdees.foodcostcalc.ui.composables.buttons.FCCAnimatedFAB
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel
import com.erdees.foodcostcalc.ui.composables.rememberNestedScrollConnection
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.screens.halfProducts.listViewAdapter.HalfProductDetailedListViewAdapter
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.UnitsUtils.getPerUnitAbbreviation
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.Utils.formatPrice
import com.erdees.foodcostcalc.utils.Utils.getBasicRecipeAsPercentageOfTargetRecipe
import com.erdees.foodcostcalc.utils.Utils.getPriceForHundredPercentOfRecipe
import com.erdees.foodcostcalc.utils.ViewUtils.getListSize
import com.erdees.foodcostcalc.utils.onNumericValueChange

@Composable
fun HalfProductsScreen(navController: NavController) {

    val viewModel: HalfProductsScreenViewModel = viewModel()
    val adItems by viewModel.filteredHalfProductsInjectedWithAds.collectAsState()
    val searchKey by viewModel.searchKey.collectAsState()
    val isVisible = remember { mutableStateOf(true) }
    val nestedScrollConnection = rememberNestedScrollConnection(isVisible)
    val screenState by viewModel.screenState.collectAsState()

    Scaffold(
        modifier = Modifier,
        floatingActionButton = {
            FCCAnimatedFAB(isVisible = isVisible.value) {
                viewModel.updateScreenState(ScreenState.Interaction(InteractionType.CreateHalfProduct))
            }
        }) { paddingValues ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(paddingValues)
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
                                adUnitId = if (BuildConfig.DEBUG) Constants.Ads.ADMOB_TEST_AD_UNIT_ID
                                else Constants.Ads.ADMOB_HALF_PRODUCTS_AD_UNIT_ID
                            )
                        }

                        is HalfProductDomain -> {
                            key(item) {
                                val itemPresentationState =
                                    viewModel
                                        .listPresentationStateHandler
                                        .itemsPresentationState
                                        .collectAsState()
                                        .value[item.id] ?: ItemPresentationState()
                                HalfProductItem(
                                    halfProductDomain = item,
                                    navController = navController,
                                    isExpanded = itemPresentationState.isExpanded,
                                    quantity = itemPresentationState.quantity,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp),
                                    onExpandToggle = {
                                        viewModel.listPresentationStateHandler.onExpandToggle(item)
                                    },
                                    onChangeQuantityDialogOpened = {
                                        viewModel.updateScreenState(
                                            ScreenState.Interaction(
                                                InteractionType.EditQuantity(item.id)
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        SearchFieldTransition(isVisible = isVisible.value) {
            SearchField(
                modifier = Modifier,
                value = searchKey,
                onValueChange = viewModel::updateSearchKey
            )
        }

        when (screenState) {

            ScreenState.Loading -> {
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
                                LocalContext.current.resources,
                                viewModel.preferences
                            ),
                            onSave = { name, unit ->
                                viewModel.addHalfProduct(name = name, unit = unit)
                            },
                            onDismiss = viewModel::resetScreenState
                        )
                    }

                    is InteractionType.EditQuantity -> {
                        val itemId = interactionType.itemId
                        val itemPresentationState =
                            viewModel
                                .listPresentationStateHandler
                                .itemsPresentationState
                                .collectAsState()
                                .value[itemId] ?: ItemPresentationState()
                        val editableQuantity =
                            remember { mutableStateOf(itemPresentationState.quantity.toString()) }
                        ValueEditDialog(
                            title = stringResource(id = R.string.edit_quantity),
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
                            }, onDismiss = {
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
    navController: NavController,
    isExpanded: Boolean,
    quantity: Double,
    modifier: Modifier = Modifier,
    onExpandToggle: () -> Unit,
    onChangeQuantityDialogOpened: () -> Unit
) {
    val context = LocalContext.current
    AndroidViewBinding(
        modifier = modifier,
        factory = HalfProductCardViewBinding::inflate,
        onReset = {},
        onRelease = {},
        update = {

            this.editButtonInDishAdapter.setOnClickListener {
                navController.navigate(
                    FCCScreen.EditHalfProduct(
                        halfProductDomain
                    )
                )
            }
            this.addProductToHalfproductButton.setOnClickListener {
                navController.navigate(
                    FCCScreen.AddItemToHalfProduct(
                        halfProductDomain
                    )
                )
            }
            this.linearLayoutDishCard.setOnClickListener {
                onExpandToggle()
            }

            this.quantityOfDataTv.setOnClickListener {
                onChangeQuantityDialogOpened()
            }

            this.halfProductNameInAdapter.text = halfProductDomain.name
            this.pricePerUnitTextView.text =
                context.getString(R.string.price_per_unit, halfProductDomain.halfProductUnit)
            this.priceOfHalfProductPerUnit.text = halfProductDomain.formattedPricePerUnit(context)
            this.priceOfHalfProductPerRecipe.text =
                halfProductDomain.formattedPricePerRecipe(context)
            this.quantityOfDataTv.text = context.getString(
                R.string.recipe_per_quantity,
                quantity.toString(),
                getPerUnitAbbreviation(halfProductDomain.halfProductUnit)
            )
            this.updatePriceOfHalfProductPerRecipe(halfProductDomain, quantity, context)

            if (isExpanded) {
                showCardElements(halfProductDomain, context, quantity)
            } else {
                hideCardElements()
            }
        })
}


private fun HalfProductCardViewBinding.hideCardElements() {
    this.quantityOfDataTv.visibility = View.GONE
    this.listView.adapter = null
    this.listView.layoutParams =
        LinearLayout.LayoutParams(this.listView.layoutParams.width, 0)
}

private fun HalfProductCardViewBinding.showCardElements(
    halfProductDomain: HalfProductDomain,
    context: Context,
    quantity: Double
) {
    this.quantityOfDataTv.visibility = View.VISIBLE
    val adapter = HalfProductDetailedListViewAdapter(
        context = context,
        halfProductDomain = halfProductDomain,
        quantity = quantity,
    )
    listView.adapter = adapter
    listView.layoutParams =
        LinearLayout.LayoutParams(
            listView.layoutParams.width,
            getListSize(
                halfProductDomain.products.indices.toList(),
                this.listView
            )
        )
}

private fun HalfProductCardViewBinding.updatePriceOfHalfProductPerRecipe(
    halfProductDomain: HalfProductDomain,
    quantity: Double,
    context: Context
) {
    val quantityPercent =
        getBasicRecipeAsPercentageOfTargetRecipe(
            quantity,
            halfProductDomain.totalQuantity
        )
    val pricePerRecipeForGivenQuantity =
        getPriceForHundredPercentOfRecipe(halfProductDomain.totalPrice, quantityPercent)
    this.priceOfHalfProductPerRecipe.text =
        formatPrice(pricePerRecipeForGivenQuantity, context)
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
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp)
        ) {
            Text(text = "Create half product", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.size(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                Column {
                    FieldLabel(
                        text = "Name",
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { value ->
                            name = value
                        },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words,
                        )
                    )
                }
                UnitField(
                    units = units,
                    selectedUnit = selectedUnit,
                    selectUnit = { selectedUnit = it }
                )
            }

            Spacer(modifier = Modifier.size(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FCCTextButton(text = "Save") {
                    onSave(name, selectedUnit)
                }
            }
        }
    }
}
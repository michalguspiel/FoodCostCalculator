package com.erdees.foodcostcalc.ui.screens.dishes

import android.icu.util.Currency
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
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
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.ui.composables.Ad
import com.erdees.foodcostcalc.ui.composables.DetailItem
import com.erdees.foodcostcalc.ui.composables.ExpandedIcon
import com.erdees.foodcostcalc.ui.composables.Ingredients
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.animations.SearchFieldTransition
import com.erdees.foodcostcalc.ui.composables.buttons.FCCAnimatedFAB
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog
import com.erdees.foodcostcalc.ui.composables.dividers.FCCPrimaryHorizontalDivider
import com.erdees.foodcostcalc.ui.composables.fields.SearchField
import com.erdees.foodcostcalc.ui.composables.rememberNestedScrollConnection
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.composables.rows.PriceRow
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.onIntegerValueChange
import java.util.Locale

@Composable
@Screen
fun DishesScreen(navController: NavController, viewModel: DishesFragmentViewModel = viewModel()) {

    val isVisible = rememberSaveable { mutableStateOf(true) }
    val nestedScrollConnection = rememberNestedScrollConnection { isVisible.value = it }
    val searchKey by viewModel.searchKey.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val adItems by viewModel.filteredDishesInjectedWithAds.collectAsState()
    val currency by viewModel.currency.collectAsState()

    Scaffold(modifier = Modifier, floatingActionButton = {
        FCCAnimatedFAB(
            isVisible = isVisible.value,
            contentDescription = stringResource(id = R.string.content_description_create_dish)
        ) {
            navController.navigate(FCCScreen.CreateDish)
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
                                else Constants.Ads.ADMOB_DISHES_AD_UNIT_ID,
                                onAdFailedToLoad = viewModel::onAdFailedToLoad
                            )
                        }

                        is DishDomain -> {
                            key(item.id) {
                                val itemPresentationState =
                                    viewModel.listPresentationStateHandler.itemsPresentationState.collectAsState().value[item.id]
                                        ?: ItemPresentationState()
                                DishItem(modifier = Modifier.padding(vertical = 8.dp),
                                    dishDomain = item,
                                    isExpanded = itemPresentationState.isExpanded,
                                    servings = itemPresentationState.quantity,
                                    currency = currency,
                                    onExpandToggle = {
                                        viewModel.listPresentationStateHandler.onExpandToggle(item)
                                    },
                                    onChangeServingsClick = {
                                        viewModel.onChangeServingsClick(item.id)
                                    },
                                    onAddItemsClick = {
                                        navController.navigate(
                                            FCCScreen.AddItemsToDish(
                                                item.id, item.name
                                            )
                                        )
                                    },
                                    onEditClick = {
                                        navController.navigate(FCCScreen.EditDish(item.id))
                                    })
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
                is ScreenState.Error -> {
                    ErrorDialog {
                        viewModel.resetScreenState()
                    }
                }

                is ScreenState.Loading -> {
                    ScreenLoadingOverlay()
                }

                is ScreenState.Interaction -> {
                    when (val interactionType =
                        (screenState as ScreenState.Interaction).interaction) {
                        is InteractionType.EditQuantity -> {
                            val itemId = interactionType.itemId
                            val itemPresentationState =
                                viewModel.listPresentationStateHandler.itemsPresentationState.collectAsState().value[itemId]
                                    ?: ItemPresentationState()
                            val editableQuantity = remember {
                                mutableStateOf(
                                    itemPresentationState.quantity.toInt().toString()
                                )
                            }
                            ValueEditDialog(title = stringResource(id = R.string.edit_displayed_servings),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                value = editableQuantity.value,
                                updateValue = { newValue ->
                                    editableQuantity.value =
                                        onIntegerValueChange(editableQuantity.value, newValue)
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

                        else -> {}
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun DishItem(
    dishDomain: DishDomain,
    isExpanded: Boolean,
    servings: Double,
    currency: Currency?,
    modifier: Modifier = Modifier,
    onExpandToggle: () -> Unit,
    onChangeServingsClick: () -> Unit,
    onAddItemsClick: () -> Unit,
    onEditClick: () -> Unit
) {

    Card(
        modifier
            .fillMaxWidth()
            .clickable { onExpandToggle() }, content = {
            Column(Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
                TitleRow(dishDomain, isExpanded)

                DishDetails(dishDomain, onChangeServingsClick, servings)

                Spacer(modifier = Modifier.height(6.dp))

                if (isExpanded) {
                    Ingredients(dishDomain, servings, currency)
                }

                PriceSummary(dishDomain = dishDomain, servings = servings.toInt(), currency = currency)

                FCCPrimaryHorizontalDivider(Modifier.padding(top = 8.dp, bottom = 12.dp))

                ButtonRow(applyDefaultPadding = false, primaryButton = {
                    FCCPrimaryButton(text = stringResource(id = R.string.add_items)) { onAddItemsClick() }
                }, secondaryButton = {
                    FCCTextButton(text = stringResource(id = R.string.details)) { onEditClick() }
                })
            }
        })
}

@Composable
private fun TitleRow(
    dishDomain: DishDomain,
    isExpanded: Boolean
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = dishDomain.name, style = MaterialTheme.typography.titleLarge)
        ExpandedIcon(isExpanded = isExpanded)
    }
}

@Composable
private fun DishDetails(
    dishDomain: DishDomain,
    onChangeServingsClicked: () -> Unit,
    servings: Double
) {
    Row(Modifier, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        DetailItem(
            divider = false,
            label = stringResource(id = R.string.margin),
            value = stringResource(
                id = R.string.margin_value, dishDomain.marginPercent.toString()
            )
        )
        DetailItem(
            divider = false,
            label = stringResource(id = R.string.tax),
            value = stringResource(
                id = R.string.tax_value, dishDomain.taxPercent.toString()
            )
        )
        DetailItem(
            modifier = Modifier.clickable { onChangeServingsClicked() },
            divider = false,
            label = stringResource(id = R.string.portions),
            value = servings.toInt().toString()
        )
    }
}

@Composable
private fun PriceSummary(dishDomain: DishDomain, servings: Int, currency: Currency?, modifier: Modifier = Modifier) {
    Column(modifier) {
        PriceRow(
            description = stringResource(id = R.string.food_cost),
            price = dishDomain.formattedFoodCostPerServings(servings, currency)
        )
        Spacer(modifier = Modifier.height(4.dp))
        PriceRow(
            description = stringResource(id = R.string.final_price),
            price = dishDomain.formattedTotalPricePerServing(servings, currency)
        )
    }
}

@Preview
@Composable
private fun DishItemPreview() {
    FCCTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DishItem(dishDomain = DishDomain(
                id = 1,
                name = "Burger",
                marginPercent = 320.0,
                taxPercent = 23.0,
                halfProducts = emptyList(),
                products = listOf(
                    UsedProductDomain(
                        1,
                        2,
                        ProductDomain(1, "Bun", 2.0, 0.5, 5.0, "kg"),
                        1.0,
                        "pcs",
                        1.0,
                    ), UsedProductDomain(
                        1,
                        2,
                        ProductDomain(2, "Meat Patty", 15.0, 0.5, 5.0, "kg"),
                        100.0,
                        "g",
                        null,
                    )
                ),
                recipe = null
            ),
                servings = 1.0,
                currency = Currency.getInstance(Locale.getDefault()),
                isExpanded = true,
                onExpandToggle = { },
                onChangeServingsClick = { },
                onAddItemsClick = { }) {}

            DishItem(dishDomain = DishDomain(
                id = 1,
                name = "Salmon with chips",
                marginPercent = 320.0,
                taxPercent = 23.0,
                halfProducts = emptyList(),
                products = listOf(),
                recipe = null
            ),
                servings = 1.0,
                currency = Currency.getInstance(Locale.getDefault()),
                isExpanded = false,
                onExpandToggle = { },
                onChangeServingsClick = { },
                onAddItemsClick = { }) {}
        }
    }
}
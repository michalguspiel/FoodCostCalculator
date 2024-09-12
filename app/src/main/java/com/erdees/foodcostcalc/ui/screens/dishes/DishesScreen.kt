package com.erdees.foodcostcalc.ui.screens.dishes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.DishCardViewBinding
import com.erdees.foodcostcalc.databinding.ListviewDishRowBinding
import com.erdees.foodcostcalc.domain.model.Ad
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ItemPresentationState
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.ui.composables.Ad
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.SearchField
import com.erdees.foodcostcalc.ui.composables.animations.SearchFieldTransition
import com.erdees.foodcostcalc.ui.composables.buttons.FCCAnimatedFAB
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog
import com.erdees.foodcostcalc.ui.composables.rememberNestedScrollConnection
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.ViewUtils.makeGone
import com.erdees.foodcostcalc.utils.ViewUtils.makeVisible
import com.erdees.foodcostcalc.utils.onIntegerValueChange

@Composable
fun DishesScreen(navController: NavController) {

    val viewModel: DishesFragmentViewModel = viewModel()
    val isVisible = remember { mutableStateOf(true) }
    val nestedScrollConnection = rememberNestedScrollConnection(isVisible)
    val searchKey by viewModel.searchKey.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val adItems by viewModel.filteredDishesInjectedWithAds.collectAsState()

    Scaffold(
        modifier = Modifier,
        floatingActionButton = {
            FCCAnimatedFAB(isVisible = isVisible.value) {
                navController.navigate(FCCScreen.CreateDish)
            }
        }
    ) { paddingValues ->
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

                        is DishDomain -> {
                            key(item) {
                                val itemPresentationState =
                                    viewModel
                                        .listPresentationStateHandler
                                        .itemsPresentationState
                                        .collectAsState()
                                        .value[item.id] ?: ItemPresentationState()
                                DishItem(
                                    modifier = Modifier
                                        .padding(vertical = 8.dp),
                                    dishDomain = item,
                                    navController = navController,
                                    isExpanded = itemPresentationState.isExpanded,
                                    servings = itemPresentationState.quantity,
                                    onExpandToggle = {
                                        viewModel.listPresentationStateHandler.onExpandToggle(item)
                                    },
                                    onChangeServingsClicked = {
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

                ScreenState.Loading -> {
                    ScreenLoadingOverlay()
                }

                is ScreenState.Interaction -> {
                    when (val interactionType =
                        (screenState as ScreenState.Interaction).interaction) {
                        is InteractionType.EditQuantity -> {
                            val itemId = interactionType.itemId
                            val itemPresentationState =
                                viewModel
                                    .listPresentationStateHandler
                                    .itemsPresentationState
                                    .collectAsState()
                                    .value[itemId] ?: ItemPresentationState()
                            val editableQuantity =
                                remember {
                                    mutableStateOf(
                                        itemPresentationState.quantity.toInt().toString()
                                    )
                                }
                            ValueEditDialog(
                                title = stringResource(id = R.string.serving_amount),
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
                                }, onDismiss = {
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
fun DishItem(
    dishDomain: DishDomain,
    navController: NavController,
    isExpanded: Boolean,
    servings: Double,
    modifier: Modifier = Modifier,
    onExpandToggle: () -> Unit,
    onChangeServingsClicked: () -> Unit
) {
    val context = LocalContext.current
    AndroidViewBinding(
        modifier = modifier,
        factory = DishCardViewBinding::inflate,
        onReset = {},
        onRelease = {},
        update = {
            this.dishNameInAdapter.text = dishDomain.name
            this.dishMarginTvInAdapter.text = (context.getString(
                R.string.dish_x_margin, String.format(
                    dishDomain.marginPercent.toString()
                )
            ))
            this.dishTaxTvInAdapter.text = context.getString(
                R.string.dish_x_tax, String.format(
                    dishDomain.taxPercent.toString()
                )
            )
            this.totalPriceDishCardView.text =
                Utils.formatPrice(dishDomain.foodCost * servings, context)
            this.totalPriceWithMarginDishCardView.text =
                Utils.formatPrice(dishDomain.totalPrice * servings, context)

            this.howManyServingsTextView.text = if (servings == 1.0) {
                context.getString(R.string.data_per_serving)
            } else {
                context.getString(R.string.data_per_x_servings, servings.toInt().toString())
            }

            this.howManyServingsTextView.setOnClickListener {
                onChangeServingsClicked()
            }

            this.editButtonInDishAdapter.setOnClickListener {
                navController.navigate(
                    FCCScreen.EditDish(dishDomain)
                )
            }
            this.addProductToDishButton.setOnClickListener {
                navController.navigate(
                    FCCScreen.AddItemsToDish(dishId = dishDomain.id, dishName = dishDomain.name)
                )
            }

            this.linearLayoutDishCard.setOnClickListener {
                onExpandToggle()
            }

            if (isExpanded) {
                this.showDishCardElements(dishDomain, servings.toInt(), context)
            } else {
                this.hideDishCardElements()
            }
        })
}

private fun DishCardViewBinding.showDishCardElements(
    dishDomain: DishDomain,
    servings: Int,
    context: Context
) {
    this.ingredientList.makeVisible()
    this.howManyServingsTextView.makeVisible()
    setIngredientList(dishDomain, servings, context)
}

private fun DishCardViewBinding.hideDishCardElements() {
    this.ingredientList.makeGone()
    this.howManyServingsTextView.makeGone()
}

private fun DishCardViewBinding.setIngredientList(
    dishDomain: DishDomain,
    servings: Int,
    context: Context
) {
    if (this.ingredientList.isGone) return
    this.ingredientList.removeAllViews()
    val ingredients = (dishDomain.products + dishDomain.halfProducts)
    ingredients.forEachIndexed { index, ingredient ->
        val row = ListviewDishRowBinding.inflate(LayoutInflater.from(context))
        when (ingredient) {
            is UsedHalfProductDomain -> {
                setRowAsHalfProduct(row, ingredient, servings, context)
            }

            is UsedProductDomain -> setRowAsProduct(ingredient, row, servings, context)
        }
        this.ingredientList.addView(
            row.root
        )
        if (index < ingredients.size - 1) {
            val divider = View(context)
            divider.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_200))
            divider.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
            this.ingredientList.addView(
                divider
            )
        }
    }
}

private fun setRowAsProduct(
    product: UsedProductDomain,
    view: ListviewDishRowBinding,
    servings: Int,
    context: Context
) {
    view.productNameInDishRow.text =
        product.item.name
    view.productWeightInDishRow.text =
        Utils.formatPriceOrWeight(product.quantity * servings)
    view.productPriceInDishRow.text = Utils.formatPrice(product.totalPrice * servings, context)
    view.productWeightUnitInDishRow.text =
        UnitsUtils.getUnitAbbreviation(product.quantityUnit)
}

private fun setRowAsHalfProduct(
    view: ListviewDishRowBinding,
    halfProduct: UsedHalfProductDomain,
    servings: Int,
    context: Context
) {
    view.productNameInDishRow.text =
        halfProduct.item.name
    view.productWeightInDishRow.text =
        (halfProduct.quantity * servings).toString()
    view.productWeightUnitInDishRow.text =
        UnitsUtils.getUnitAbbreviation(halfProduct.quantityUnit)
    setHalfProductRowPrice(servings, halfProduct, view.productPriceInDishRow, context)
}

private fun setHalfProductRowPrice(
    servings: Int,
    halfProduct: UsedHalfProductDomain,
    productPriceTextView: TextView,
    context: Context
) {
    productPriceTextView.text = Utils.formatPrice(halfProduct.totalPrice * servings, context)
}
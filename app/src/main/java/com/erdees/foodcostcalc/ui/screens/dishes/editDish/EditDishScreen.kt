package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.content.Context
import android.icu.util.Currency
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.ui.composables.DetailItem
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCOutlinedButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.Utils

data class EditDishScreenCallbacks(
    val saveDish: () -> Unit,
    val shareDish: (Context) -> Unit,
    val setInteraction: (InteractionType) -> Unit,
    val deleteDish: (Long) -> Unit,
    val removeItem: (UsedItem) -> Unit,
    val updateQuantity: (String) -> Unit,
    val saveQuantity: () -> Unit,
    val updateTax: (String) -> Unit,
    val saveTax: () -> Unit,
    val updateMargin: (String) -> Unit,
    val saveMargin: () -> Unit,
    val updateName: (String) -> Unit,
    val saveName: () -> Unit,
    val updateTotalPrice: (String) -> Unit,
    val saveTotalPrice: () -> Unit,
    val resetScreenState: () -> Unit
)

data class EditDishScreenState(
    val dishId: Long,
    val usedItems: List<UsedItem>,
    val modifiedDishDomain: DishDomain?,
    val editableQuantity: String,
    val editableTax: String,
    val editableMargin: String,
    val editableName: String,
    val editableTotalPrice: String,
    val currency: Currency?,
    val screenState: ScreenState,
)

@Screen
@Composable
fun EditDishScreen(
    dishId: Long, navController: NavController, viewModel: DishDetailsViewModel = viewModel()
) {
    val screenState by viewModel.screenState.collectAsState()
    val usedItems: List<UsedItem> by viewModel.items.collectAsState()
    val modifiedDishDomain by viewModel.dish.collectAsStateWithLifecycle()
    val editableQuantity by viewModel.editableQuantity.collectAsState()
    val editableTax by viewModel.editableTax.collectAsState()
    val editableMargin by viewModel.editableMargin.collectAsState()
    val editableName by viewModel.editableName.collectAsState()
    val editableTotalPrice by viewModel.editableTotalPrice.collectAsState()
    val currency by viewModel.currency.collectAsState()

    LaunchedEffect(screenState) {
        when (screenState) {
            is ScreenState.Success -> {
                viewModel.resetScreenState()
                navController.popBackStack()
            }

            else -> {}
        }
    }

    EditDishScreenContent(
        navController = navController,
        state = EditDishScreenState(
            dishId = dishId,
            usedItems = usedItems,
            modifiedDishDomain = modifiedDishDomain,
            editableQuantity = editableQuantity,
            editableTax = editableTax,
            editableMargin = editableMargin,
            editableName = editableName,
            editableTotalPrice = editableTotalPrice,
            currency = currency,
            screenState = screenState
        ),
        callbacks = EditDishScreenCallbacks(
            saveDish = viewModel::saveDish,
            shareDish = viewModel::shareDish,
            setInteraction = viewModel::setInteraction,
            deleteDish = viewModel::deleteDish,
            removeItem = viewModel::removeItem,
            updateQuantity = viewModel::updateQuantity,
            saveQuantity = viewModel::updateItemQuantity,
            updateTax = viewModel::updateTax,
            saveTax = viewModel::saveDishTax,
            updateMargin = viewModel::updateMargin,
            saveMargin = viewModel::saveDishMargin,
            updateName = viewModel::updateName,
            saveName = viewModel::saveDishName,
            updateTotalPrice = viewModel::updateTotalPrice,
            saveTotalPrice = viewModel::saveDishTotalPrice,
            resetScreenState = viewModel::resetScreenState
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDishScreenContent(
    state: EditDishScreenState,
    navController: NavController,
    callbacks: EditDishScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    with(state) {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(title = {
                    Text(
                        text = modifiedDishDomain?.name ?: dishId.toString(),
                        modifier = Modifier.clickable {
                            callbacks.setInteraction(InteractionType.EditName)
                        })
                }, actions = {
                    IconButton(onClick = { callbacks.deleteDish(dishId) }) {
                        Icon(
                            imageVector = Icons.Sharp.Delete,
                            contentDescription = stringResource(R.string.remove_dish)
                        )
                    }
                }, navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Sharp.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                })
            }) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                Column {
                    LazyColumn(Modifier.weight(fill = true, weight = 1f)) {
                        items(usedItems, key = { item ->
                            item::class.simpleName + item.id.toString()
                        }) { item ->
                            UsedItem(
                                usedItem = item,
                                onRemove = { callbacks.removeItem(it) },
                                onEdit = {
                                    callbacks.setInteraction(
                                        InteractionType.EditItem(it)
                                    )
                                })
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                thickness = 1.dp
                            )
                        }
                    }

                    Column(Modifier) {
                        modifiedDishDomain?.let {
                            DishDetails(it, currency, onTaxClick = {
                                callbacks.setInteraction(InteractionType.EditTax)
                            }, onMarginClick = {
                                callbacks.setInteraction(InteractionType.EditMargin)
                            }, onTotalPriceClick = {
                                callbacks.setInteraction(InteractionType.EditTotalPrice)
                            })
                        }

                        Buttons(
                            modifier = Modifier.padding(top = 16.dp),
                            saveDish = { callbacks.saveDish() },
                            shareDish = { callbacks.shareDish(it) },
                            navigate = {
                                navController.navigate(FCCScreen.Recipe)
                            }
                        )
                    }
                }

                when (screenState) {
                    is ScreenState.Loading -> ScreenLoadingOverlay()
                    is ScreenState.Success -> {} // NOTHING

                    is ScreenState.Error -> {
                        ErrorDialog {
                            callbacks.resetScreenState()
                        }
                    }

                    is ScreenState.Interaction -> {
                        when (screenState.interaction) {
                            InteractionType.EditTax -> {
                                ValueEditDialog(
                                    title = stringResource(R.string.edit_tax),
                                    value = editableTax,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    updateValue = { callbacks.updateTax(it) },
                                    onSave = { callbacks.saveTax() },
                                    onDismiss = { callbacks.resetScreenState() }
                                )
                            }

                            InteractionType.EditMargin -> {
                                ValueEditDialog(
                                    title = stringResource(R.string.edit_margin),
                                    value = editableMargin,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    updateValue = { callbacks.updateMargin(it) },
                                    onSave = { callbacks.saveMargin() },
                                    onDismiss = { callbacks.resetScreenState() }
                                )
                            }

                            InteractionType.EditTotalPrice -> {
                                ValueEditDialog(
                                    title = stringResource(R.string.edit_total_price),
                                    value = editableTotalPrice,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    updateValue = { callbacks.updateTotalPrice(it) },
                                    onSave = { callbacks.saveTotalPrice() },
                                    onDismiss = { callbacks.resetScreenState() }
                                )
                            }

                            InteractionType.EditName -> {
                                ValueEditDialog(
                                    title = stringResource(R.string.edit_name),
                                    value = editableName,
                                    updateValue = { callbacks.updateName(it) },
                                    onSave = { callbacks.saveName() },
                                    onDismiss = { callbacks.resetScreenState() },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        capitalization = KeyboardCapitalization.Words
                                    )
                                )
                            }

                            is InteractionType.EditItem -> {
                                ValueEditDialog(
                                    title = stringResource(R.string.edit_quantity),
                                    value = editableQuantity,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        capitalization = KeyboardCapitalization.Words
                                    ),
                                    updateValue = { callbacks.updateQuantity(it) },
                                    onSave = { callbacks.saveQuantity() },
                                    onDismiss = { callbacks.resetScreenState() }
                                )
                            }

                            else -> {}
                        }
                    }

                    is ScreenState.Idle -> {}
                }
            }
        }
    }
}

@Composable
private fun Buttons(
    saveDish: () -> Unit,
    shareDish: (Context) -> Unit,
    navigate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    ButtonRow(
        modifier = modifier.padding(end = 12.dp),
        primaryButton = {
            FCCPrimaryButton(text = stringResource(R.string.save)) {
                saveDish()
            }
        },
        secondaryButton = {
            FCCOutlinedButton(text = stringResource(R.string.recipe_button_title)) {
                navigate()
            }
        },
        tertiaryButton = {
            FCCTextButton(stringResource(R.string.share)) {
                shareDish(context)
            }
        }
    )
}

@Composable
fun DishDetails(
    dishDomain: DishDomain,
    currency: Currency?,
    modifier: Modifier = Modifier,
    onTaxClick: () -> Unit,
    onMarginClick: () -> Unit,
    onTotalPriceClick: () -> Unit
) {
    Column(modifier) {
        Row {
            DetailItem(
                label = stringResource(R.string.margin),
                value = "${dishDomain.marginPercent}%",
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
                    .clickable {
                        onMarginClick()
                    })
            DetailItem(
                label = stringResource(R.string.tax),
                value = "${dishDomain.taxPercent}%",
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
                    .clickable {
                        onTaxClick()
                    })
        }

        Spacer(modifier = Modifier.size(8.dp))

        Row {
            DetailItem(
                label = stringResource(R.string.food_cost),
                value = Utils.formatPrice(dishDomain.foodCost, currency),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            )
            DetailItem(
                label = stringResource(R.string.final_price),
                value = Utils.formatPrice(dishDomain.totalPrice, currency),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
                    .clickable(enabled = dishDomain.foodCost != 0.0) {
                        onTotalPriceClick()
                    },
                bolder = true
            )
        }
    }
}


@Composable
fun UsedItem(
    usedItem: UsedItem,
    modifier: Modifier = Modifier,
    onRemove: (UsedItem) -> Unit,
    onEdit: (UsedItem) -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        modifier = modifier.animateContentSize(),
        state = swipeState,
        backgroundContent = {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    imageVector = Icons.Sharp.Delete,
                    contentDescription = null
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        content = {
            ListItem(
                colors = (ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)),
                headlineContent = {
                    Text(text = usedItem.item.name)
                },
                supportingContent = {
                    Text(text = usedItem.quantity.toString() + " " + usedItem.quantityUnit)
                },
                trailingContent = {
                    IconButton(onClick = { onEdit(usedItem) }) {
                        Icon(imageVector = Icons.Sharp.Edit, contentDescription = "Edit")
                    }
                })
        })

    when (swipeState.currentValue) {
        SwipeToDismissBoxValue.EndToStart -> {
            LaunchedEffect(swipeState) {
                swipeState.reset()
            }
            onRemove(usedItem)
        }

        SwipeToDismissBoxValue.StartToEnd -> {}

        SwipeToDismissBoxValue.Settled -> {}
    }
}


@Preview
@Composable
private fun UsedItemPreview() {
    FCCTheme {
        UsedItem(
            UsedProductDomain(
                id = 0, ownerId = 0, item = ProductDomain(
                    id = 1,
                    name = "Product",
                    pricePerUnit = 10.0,
                    unit = "kg",
                    tax = 23.0,
                    waste = 20.0
                ), quantity = 1.0, quantityUnit = "kg", weightPiece = 1.0
            ),
            modifier = Modifier,
            onEdit = {},
            onRemove = {},
        )
    }
}

// --- Main Preview Function using the Provider ---

@Preview(name = "Edit Dish Screen States", showBackground = true)
@PreviewLightDark
@Composable
fun EditDishScreenContentPreview(
    @PreviewParameter(EditDishScreenStateProvider::class) state: EditDishScreenState
) {
    // You need a NavController for the preview, even if it doesn't navigate.
    // rememberNavController() is fine for previews.
    val navController = rememberNavController()
    val emptyCallbacks = createEmptyEditDishScreenCallbacks()

    FCCTheme { // Replace FCCTheme with your actual app theme
        EditDishScreenContent(
            state = state,
            navController = navController,
            callbacks = emptyCallbacks,
            modifier = Modifier // Add any desired modifiers for preview sizing if needed
        )
    }
}

private fun createEmptyEditDishScreenCallbacks() = EditDishScreenCallbacks(
    {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
)

package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.content.Context
import android.icu.util.Currency
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.erdees.foodcostcalc.domain.model.dish.DishActionResult
import com.erdees.foodcostcalc.domain.model.dish.DishDetailsActionResultType
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCOutlinedButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCDeleteConfirmationDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCUnsavedChangesDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.navigation.ConfirmPopUp
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.ui.theme.FCCTheme

data class EditDishScreenCallbacks(
    val saveDish: () -> Unit,
    val shareDish: (Context) -> Unit,
    val setInteraction: (InteractionType) -> Unit,
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
    val resetScreenState: () -> Unit,
    val onDeleteDishClick: () -> Unit,
    val onDeleteConfirmed: (Long) -> Unit,
    val discardAndNavigate: (() -> Unit) -> Unit,
    val saveAndNavigate: () -> Unit,
    val onCopyDishClick: () -> Unit,
    val copyDish: () -> Unit,
    val updateCopiedDishName: (String) -> Unit,
    val hideCopyConfirmation: () -> Unit,
    val discardChangesAndProceed: () -> Unit = {},
    val saveChangesAndProceed: () -> Unit = {},
)

data class EditDishScreenState(
    val dishId: Long,
    val usedItems: List<UsedItem>,
    val modifiedDishDomain: DishDomain?,
    val editableQuantity: String,
    val editableTax: String,
    val editableMargin: String,
    val editableName: String,
    val editableCopiedDishName: String,
    val editableTotalPrice: String,
    val currency: Currency?,
    val screenState: ScreenState,
    val showCopyConfirmation: Boolean
)

@Screen
@Composable
fun DishDetailsScreen(
    dishId: Long, navController: NavController, viewModel: DishDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    val screenState by viewModel.screenState.collectAsState()
    val usedItems: List<UsedItem> by viewModel.items.collectAsStateWithLifecycle()
    val modifiedDishDomain by viewModel.dish.collectAsStateWithLifecycle()
    val editableQuantity by viewModel.editableQuantity.collectAsState()
    val editableTax by viewModel.editableTax.collectAsState()
    val editableMargin by viewModel.editableMargin.collectAsState()
    val editableName by viewModel.editableName.collectAsState()
    val editableCopiedDishName by viewModel.editableCopiedDishName.collectAsState()
    val editableTotalPrice by viewModel.editableTotalPrice.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val showCopyConfirmation by viewModel.showCopyConfirmation.collectAsState()

    BackHandler {
        viewModel.handleBackNavigation { navController.popBackStack() }
    }

    LaunchedEffect(screenState) {
        val state = screenState as? ScreenState.Success<*> ?: return@LaunchedEffect
        val data = state.data as? DishActionResult ?: return@LaunchedEffect

        viewModel.resetScreenState()
        when (data.type) {
            DishDetailsActionResultType.COPIED -> {
                navController.navigate(FCCScreen.DishDetails(data.dishId, true)) {
                    popUpTo(navController.currentBackStackEntry?.destination?.route ?: "") {
                        inclusive = true
                    }
                }
            }

            DishDetailsActionResultType.UPDATED_NAVIGATE, DishDetailsActionResultType.DELETED -> {
                navController.popBackStack()
            }

            DishDetailsActionResultType.UPDATED_STAY -> {
                viewModel.handleCopyDish({ getCopyDishPrefilledName(it, context) })
            }
        }
    }

    EditDishScreenContent(
        state = EditDishScreenState(
            dishId = dishId,
            usedItems = usedItems,
            modifiedDishDomain = modifiedDishDomain,
            editableQuantity = editableQuantity,
            editableTax = editableTax,
            editableMargin = editableMargin,
            editableName = editableName,
            editableCopiedDishName = editableCopiedDishName,
            editableTotalPrice = editableTotalPrice,
            currency = currency,
            screenState = screenState,
            showCopyConfirmation = showCopyConfirmation
        ), navController = navController, callbacks = EditDishScreenCallbacks(
            saveDish = viewModel::saveDish,
            shareDish = viewModel::shareDish,
            setInteraction = viewModel::setInteraction,
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
            resetScreenState = viewModel::resetScreenState,
            onDeleteDishClick = viewModel::onDeleteDishClick,
            onDeleteConfirmed = viewModel::confirmDelete,
            copyDish = viewModel::copyDish,
            updateCopiedDishName = viewModel::updateCopiedDishName,
            discardAndNavigate = viewModel::discardChanges,
            saveAndNavigate = viewModel::saveAndNavigate,
            hideCopyConfirmation = viewModel::hideCopyConfirmation,
            saveChangesAndProceed = viewModel::saveChangesAndProceed,
            discardChangesAndProceed = {
                viewModel.discardChangesAndProceed({
                    getCopyDishPrefilledName(
                        it, context
                    )
                })
            },
            onCopyDishClick = {
                viewModel.handleCopyDish({
                    getCopyDishPrefilledName(
                        it, context
                    )
                })
            },
        )
    )
}

private fun getCopyDishPrefilledName(name: String?, context: Context): String {
    return context.getString(R.string.copy_dish_prefilled_name, name)
}

@Composable
private fun EditDishScreenContent(
    state: EditDishScreenState,
    navController: NavController,
    callbacks: EditDishScreenCallbacks,
    modifier: Modifier = Modifier
) {
    with(state) {
        Scaffold(
            modifier = modifier, topBar = {
                EditDishTopBar(
                    dishName = modifiedDishDomain?.name ?: dishId.toString(),
                    onNameClick = { callbacks.setInteraction(InteractionType.EditName) },
                    onDeleteClick = { callbacks.onDeleteDishClick() },
                    onCopyClick = { callbacks.onCopyDishClick() },
                    onBackClick = { navController.popBackStack() })
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
                            })
                    }
                }

                ScreenStateHandler(state, callbacks, navController)

                ConfirmPopUp(visible = showCopyConfirmation) {
                    callbacks.hideCopyConfirmation()
                }
            }
        }
    }
}

@Composable
private fun ScreenStateHandler(
    state: EditDishScreenState,
    callbacks: EditDishScreenCallbacks,
    navController: NavController,
) {
    with(state) {
        when (screenState) {
            is ScreenState.Loading<*> -> ScreenLoadingOverlay()
            is ScreenState.Success<*> -> {} // NOTHING

            is ScreenState.Error -> {
                ErrorDialog {
                    callbacks.resetScreenState()
                }
            }

            is ScreenState.Interaction -> {
                InteractionHandler(
                    interaction = screenState.interaction,
                    state = state,
                    callbacks = callbacks,
                    navController = navController
                )
            }

            is ScreenState.Idle -> {}
        }
    }
}

@Composable
private fun InteractionHandler(
    interaction: InteractionType,
    state: EditDishScreenState,
    callbacks: EditDishScreenCallbacks,
    navController: NavController
) {
    with(state) {
        when (interaction) {
            InteractionType.EditTax -> {
                ValueEditDialog(
                    title = stringResource(R.string.edit_tax),
                    value = editableTax,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    updateValue = { callbacks.updateTax(it) },
                    onSave = { callbacks.saveTax() },
                    onDismiss = { callbacks.resetScreenState() })
            }

            InteractionType.EditMargin -> {
                ValueEditDialog(
                    title = stringResource(R.string.edit_margin),
                    value = editableMargin,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    updateValue = { callbacks.updateMargin(it) },
                    onSave = { callbacks.saveMargin() },
                    onDismiss = { callbacks.resetScreenState() })
            }

            InteractionType.EditTotalPrice -> {
                ValueEditDialog(
                    title = stringResource(R.string.edit_total_price),
                    value = editableTotalPrice,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    updateValue = { callbacks.updateTotalPrice(it) },
                    onSave = { callbacks.saveTotalPrice() },
                    onDismiss = { callbacks.resetScreenState() })
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

            is InteractionType.CopyDish -> {
                ValueEditDialog(
                    title = stringResource(R.string.copy_dish),
                    value = editableCopiedDishName,
                    updateValue = { callbacks.updateCopiedDishName(it) },
                    onSave = { callbacks.copyDish() },
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
                    onDismiss = { callbacks.resetScreenState() })
            }

            is InteractionType.DeleteConfirmation -> {
                val deleteConfirmation = interaction
                FCCDeleteConfirmationDialog(
                    itemName = deleteConfirmation.itemName,
                    onDismiss = { callbacks.resetScreenState() },
                    onConfirmDelete = {
                        callbacks.onDeleteConfirmed(deleteConfirmation.itemId)
                    })
            }

            InteractionType.UnsavedChangesConfirmation -> {
                FCCUnsavedChangesDialog(
                    onDismiss = { callbacks.resetScreenState() },
                    onDiscard = { callbacks.discardAndNavigate { navController.popBackStack() } },
                    onSave = { callbacks.saveAndNavigate() })
            }

            is InteractionType.UnsavedChangesConfirmationBeforeCopy -> {
                FCCUnsavedChangesDialog(
                    onDismiss = callbacks.resetScreenState,
                    onDiscard = callbacks.discardChangesAndProceed,
                    onSave = callbacks.saveChangesAndProceed
                )
            }

            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDishTopBar(
    dishName: String,
    onNameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCopyClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(title = {
        Text(
            text = dishName, modifier = Modifier.clickable { onNameClick() })
    }, actions = {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.more_options)
            )
        }
        DropdownMenu(
            expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.copy_dish)) }, onClick = {
                onCopyClick()
                showMenu = false
            }, leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.content_copy_24dp),
                    contentDescription = stringResource(R.string.copy_dish)
                )
            })
            DropdownMenuItem(text = { Text(stringResource(R.string.remove_dish)) }, onClick = {
                onDeleteClick()
                showMenu = false
            }, leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.delete_24dp),
                    contentDescription = stringResource(R.string.remove_dish)
                )
            })
        }
    }, navigationIcon = {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Sharp.ArrowBack,
                contentDescription = stringResource(R.string.back)
            )
        }
    })
}

@Composable
private fun Buttons(
    saveDish: () -> Unit,
    shareDish: (Context) -> Unit,
    navigate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    ButtonRow(modifier = modifier.padding(end = 12.dp), primaryButton = {
        FCCPrimaryButton(text = stringResource(R.string.save)) {
            saveDish()
        }
    }, secondaryButton = {
        FCCOutlinedButton(text = stringResource(R.string.recipe_button_title)) {
            navigate()
        }
    }, tertiaryButton = {
        FCCTextButton(stringResource(R.string.share)) {
            shareDish(context)
        }
    })
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

@Preview(name = "Edit Dish Screen States", showBackground = true)
@PreviewLightDark
@Composable
private fun EditDishScreenContentPreview(
    @PreviewParameter(EditDishScreenStateProvider::class) state: EditDishScreenState
) {
    val navController = rememberNavController()
    val emptyCallbacks = createEmptyEditDishScreenCallbacks()

    FCCTheme {
        EditDishScreenContent(
            state = state,
            navController = navController,
            callbacks = emptyCallbacks,
            modifier = Modifier
        )
    }
}

private fun createEmptyEditDishScreenCallbacks() = EditDishScreenCallbacks(
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {},
    {})

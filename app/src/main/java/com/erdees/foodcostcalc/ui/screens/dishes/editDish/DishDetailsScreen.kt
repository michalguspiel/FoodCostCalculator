package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.content.Context
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

data class EditDishScreenActions(
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

@Screen
@Composable
fun DishDetailsScreen(
    dishId: Long, navController: NavController, viewModel: DishDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        viewModel.handleBackNavigation { navController.popBackStack() }
    }

    LaunchedEffect(uiState.screenState) {
        val state = uiState.screenState as? ScreenState.Success<*> ?: return@LaunchedEffect
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
        uiState = uiState,
        dishId = dishId,
        navController = navController,
        actions = EditDishScreenActions(
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
    uiState: DishDetailsUiState,
    dishId: Long,
    navController: NavController,
    actions: EditDishScreenActions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            EditDishTopBar(
                dishName = uiState.dish?.name ?: dishId.toString(),
                onNameClick = { actions.setInteraction(InteractionType.EditName) },
                onDeleteClick = { actions.onDeleteDishClick() },
                onCopyClick = { actions.onCopyDishClick() },
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column {
                LazyColumn(Modifier.weight(fill = true, weight = 1f)) {
                    items(uiState.items, key = { item ->
                        item::class.simpleName + item.id.toString()
                    }) { item ->
                        UsedItem(
                            usedItem = item,
                            onRemove = { actions.removeItem(it) },
                            onEdit = {
                                actions.setInteraction(
                                    InteractionType.EditItem(it)
                                )
                            }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )
                    }
                }

                Column(Modifier) {
                    uiState.dish?.let {
                        DishDetails(
                            it,
                            uiState.currency,
                            onTaxClick = {
                                actions.setInteraction(InteractionType.EditTax)
                            },
                            onMarginClick = {
                                actions.setInteraction(InteractionType.EditMargin)
                            },
                            onTotalPriceClick = {
                                actions.setInteraction(InteractionType.EditTotalPrice)
                            }
                        )
                    }

                    Buttons(
                        modifier = Modifier.padding(top = 16.dp),
                        saveDish = { actions.saveDish() },
                        shareDish = { actions.shareDish(it) },
                        navigate = {
                            navController.navigate(FCCScreen.Recipe)
                        }
                    )
                }
            }

            ScreenStateHandler(
                uiState = uiState,
                editableFields = uiState.editableFields,
                callbacks = actions,
                navController = navController
            )

            ConfirmPopUp(visible = uiState.showCopyConfirmation) {
                actions.hideCopyConfirmation()
            }
        }
    }
}

@Composable
private fun ScreenStateHandler(
    uiState: DishDetailsUiState,
    editableFields: EditableFields,
    callbacks: EditDishScreenActions,
    navController: NavController,
) {
    when (uiState.screenState) {
        is ScreenState.Loading<*> -> ScreenLoadingOverlay()
        is ScreenState.Success<*> -> {} // NOTHING

        is ScreenState.Error -> {
            ErrorDialog {
                callbacks.resetScreenState()
            }
        }

        is ScreenState.Interaction -> {
            InteractionHandler(
                interaction = uiState.screenState.interaction,
                editableFields = editableFields,
                callbacks = callbacks,
                navController = navController
            )
        }

        is ScreenState.Idle -> {}
    }
}

@Composable
private fun InteractionHandler(
    interaction: InteractionType,
    editableFields: EditableFields,
    callbacks: EditDishScreenActions,
    navController: NavController
) {
    when (interaction) {
        InteractionType.EditTax -> {
            ValueEditDialog(
                title = stringResource(R.string.edit_tax),
                value = editableFields.tax,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                updateValue = { callbacks.updateTax(it) },
                onSave = { callbacks.saveTax() },
                onDismiss = { callbacks.resetScreenState() })
        }

        InteractionType.EditMargin -> {
            ValueEditDialog(
                title = stringResource(R.string.edit_margin),
                value = editableFields.margin,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                updateValue = { callbacks.updateMargin(it) },
                onSave = { callbacks.saveMargin() },
                onDismiss = { callbacks.resetScreenState() })
        }

        InteractionType.EditTotalPrice -> {
            ValueEditDialog(
                title = stringResource(R.string.edit_total_price),
                value = editableFields.totalPrice,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                updateValue = { callbacks.updateTotalPrice(it) },
                onSave = { callbacks.saveTotalPrice() },
                onDismiss = { callbacks.resetScreenState() })
        }

        InteractionType.EditName -> {
            ValueEditDialog(
                title = stringResource(R.string.edit_name),
                value = editableFields.name,
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
                value = editableFields.copiedDishName,
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
                value = editableFields.quantity,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    capitalization = KeyboardCapitalization.Words
                ),
                updateValue = { callbacks.updateQuantity(it) },
                onSave = { callbacks.saveQuantity() },
                onDismiss = { callbacks.resetScreenState() })
        }

        is InteractionType.DeleteConfirmation -> {
            FCCDeleteConfirmationDialog(
                itemName = interaction.itemName,
                onDismiss = { callbacks.resetScreenState() },
                onConfirmDelete = {
                    callbacks.onDeleteConfirmed(interaction.itemId)
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
    @PreviewParameter(EditDishScreenStateProvider::class) state: DishDetailsUiState
) {
    val navController = rememberNavController()
    val emptyCallbacks = createEmptyEditDishScreenCallbacks()

    FCCTheme {
        EditDishScreenContent(
            uiState = state,
            navController = navController,
            actions = emptyCallbacks,
            modifier = Modifier,
            dishId = 0L
        )
    }
}

private fun createEmptyEditDishScreenCallbacks() = EditDishScreenActions(
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

package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.dish.DishActionResult
import com.erdees.foodcostcalc.domain.model.dish.DishDetailsActionResultType
import com.erdees.foodcostcalc.ext.showUndoDeleteSnackbar
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.UsedItem
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCDeleteConfirmationDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCUnsavedChangesDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog
import com.erdees.foodcostcalc.ui.navigation.ConfirmPopUp
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentLookupForm
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentForm
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormUiState
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormViewModel
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Screen
@Composable
fun DishDetailsScreen(
    dishId: Long, navController:
    NavController,
    viewModel: DishDetailsViewModel = viewModel(),
    existingFormViewModel: ExistingComponentFormViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val existingFormUiState by existingFormViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val addComponentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    BackHandler {
        viewModel.handleBackNavigation { navController.popBackStack() }
    }

    LaunchedEffect(uiState.componentSelection) {
        uiState.componentSelection?.let {
            if (it is ComponentSelection.ExistingComponent) {
                existingFormViewModel.setItemContext(it.item, context.resources)
            }
        }
    }

    LaunchedEffect(uiState.lastRemovedItem) {
        val removedItem = uiState.lastRemovedItem?.item ?: return@LaunchedEffect
        snackbarHostState.showUndoDeleteSnackbar(
            message = context.getString(R.string.removed_item, removedItem.item.name),
            actionLabel = context.getString(R.string.undo),
            actionPerformed = { viewModel.undoRemoveItem() },
            ignored = { viewModel.clearLastRemovedItem() }
        )
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
        existingComponentFormUiState = existingFormUiState,
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
            saveAndNavigate = viewModel::saveAndNavigate,
            hideCopyConfirmation = viewModel::hideCopyConfirmation,
            saveChangesAndProceed = viewModel::saveChangesAndProceed,
            setComponentSelection = viewModel::setComponentSelection,
            onAddExistingComponentClick = viewModel::onAddExistingComponent,
            discardChangesAndProceed = {
                viewModel.discardChangesAndProceed {
                    getCopyDishPrefilledName(
                        it, context
                    )
                }
            },
            onCopyDishClick = {
                viewModel.handleCopyDish {
                    getCopyDishPrefilledName(
                        it, context
                    )
                }
            },
        ),
        existingComponentFormActions = ExistingComponentFormActions(
            onFormDataChange = existingFormViewModel::updateFormData,
            onUnitForDishDropdownExpandedChange = {
                existingFormViewModel.unitForDishDropdownExpanded.value = it
            },
            onAddComponent = { data ->
                scope.launch {
                    addComponentSheetState.hide()
                }.invokeOnCompletion {
                    viewModel.onAddExistingComponent(data)
                    existingFormViewModel.onAddIngredientClick()
                }
            },
            onCancel = {
                viewModel.resetScreenState()
                viewModel.setComponentSelection(null)
            }
        ),
        snackbarHostState = snackbarHostState,
        addComponentSheetState = addComponentSheetState,
    )
}

private fun getCopyDishPrefilledName(name: String?, context: Context): String {
    return context.getString(R.string.copy_dish_prefilled_name, name)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDishScreenContent(
    uiState: DishDetailsUiState,
    existingComponentFormUiState: ExistingComponentFormUiState,
    dishId: Long,
    navController: NavController,
    actions: EditDishScreenActions,
    existingComponentFormActions: ExistingComponentFormActions,
    addComponentSheetState: SheetState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Scaffold(
        modifier = modifier,
        topBar = {
            EditDishTopBar(
                dishName = uiState.dish?.name ?: dishId.toString(),
                onNameClick = { actions.setInteraction(InteractionType.EditName) },
                onDeleteClick = { actions.onDeleteDishClick() },
                onCopyClick = { actions.onCopyDishClick() },
                onShareClick = { actions.shareDish(context) },
                onRecipeClick = { navController.navigate(FCCScreen.Recipe) },
                onBackClick = { navController.popBackStack() }
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column {
                LazyColumn(Modifier.weight(fill = true, weight = 1f)) {
                    items(uiState.items) { item ->
                        UsedItem(
                            modifier = Modifier.animateItem(),
                            usedItem = item,
                            currency = uiState.currency,
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

                    item {
                        FCCTextButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            text = stringResource(R.string.add_component),
                            onClick = {
                                actions.setInteraction(InteractionType.ContextualAddComponent)
                            }
                        )
                    }
                }

                Column(Modifier) {
                    Box(contentAlignment = Alignment.BottomCenter) {
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
                        SnackbarHost(snackbarHostState)
                    }

                    FCCPrimaryButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        text = stringResource(R.string.save),
                        onClick = {
                            actions.saveDish()
                        })
                }
            }

            ScreenStateHandler(
                uiState = uiState,
                existingComponentFormUiState = existingComponentFormUiState,
                actions = actions,
                existingComponentFormActions = existingComponentFormActions,
                addComponentSheetState = addComponentSheetState,
                navController = navController
            )

            ConfirmPopUp(visible = uiState.showCopyConfirmation) {
                actions.hideCopyConfirmation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenStateHandler(
    uiState: DishDetailsUiState,
    existingComponentFormUiState: ExistingComponentFormUiState,
    actions: EditDishScreenActions,
    existingComponentFormActions: ExistingComponentFormActions,
    addComponentSheetState: SheetState,
    navController: NavController,
) {
    when (uiState.screenState) {
        is ScreenState.Loading<*> -> ScreenLoadingOverlay()
        is ScreenState.Success<*> -> {} // NOTHING

        is ScreenState.Error -> {
            ErrorDialog {
                actions.resetScreenState()
            }
        }

        is ScreenState.Interaction -> {
            InteractionHandler(
                uiState = uiState,
                existingComponentFormUiState = existingComponentFormUiState,
                interaction = uiState.screenState.interaction,
                actions = actions,
                existingComponentFormActions = existingComponentFormActions,
                navController = navController,
                addComponentSheetState = addComponentSheetState
            )
        }

        is ScreenState.Idle -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InteractionHandler(
    uiState: DishDetailsUiState,
    existingComponentFormUiState: ExistingComponentFormUiState,
    interaction: InteractionType,
    actions: EditDishScreenActions,
    existingComponentFormActions: ExistingComponentFormActions,
    addComponentSheetState: SheetState,
    navController: NavController,
) {
    Timber.i("InteractionHandler: interaction = $interaction")
    when (interaction) {
        InteractionType.EditTax -> {
            ValueEditDialog(
                title = stringResource(R.string.edit_tax),
                value = uiState.editableFields.tax,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                updateValue = { actions.updateTax(it) },
                onSave = { actions.saveTax() },
                onDismiss = { actions.resetScreenState() })
        }

        InteractionType.EditMargin -> {
            ValueEditDialog(
                title = stringResource(R.string.edit_margin),
                value = uiState.editableFields.margin,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                updateValue = { actions.updateMargin(it) },
                onSave = { actions.saveMargin() },
                onDismiss = { actions.resetScreenState() })
        }

        InteractionType.EditTotalPrice -> {
            ValueEditDialog(
                title = stringResource(R.string.edit_total_price),
                value = uiState.editableFields.totalPrice,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                updateValue = { actions.updateTotalPrice(it) },
                onSave = { actions.saveTotalPrice() },
                onDismiss = { actions.resetScreenState() })
        }

        InteractionType.EditName -> {
            ValueEditDialog(
                title = stringResource(R.string.edit_name),
                value = uiState.editableFields.name,
                updateValue = { actions.updateName(it) },
                onSave = { actions.saveName() },
                onDismiss = { actions.resetScreenState() },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words
                )
            )
        }

        is InteractionType.CopyDish -> {
            ValueEditDialog(
                title = stringResource(R.string.copy_dish),
                value = uiState.editableFields.copiedDishName,
                updateValue = { actions.updateCopiedDishName(it) },
                onSave = { actions.copyDish() },
                onDismiss = { actions.resetScreenState() },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words
                )
            )
        }

        is InteractionType.EditItem -> {
            ValueEditDialog(
                title = stringResource(R.string.edit_quantity),
                value = uiState.editableFields.quantity,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    capitalization = KeyboardCapitalization.Words
                ),
                updateValue = { actions.updateQuantity(it) },
                onSave = { actions.saveQuantity() },
                onDismiss = { actions.resetScreenState() })
        }

        is InteractionType.DeleteConfirmation -> {
            FCCDeleteConfirmationDialog(
                itemName = interaction.itemName,
                onDismiss = { actions.resetScreenState() },
                onConfirmDelete = {
                    actions.onDeleteConfirmed(interaction.itemId)
                })
        }

        InteractionType.UnsavedChangesConfirmation -> {
            FCCUnsavedChangesDialog(
                onDismiss = { actions.resetScreenState() },
                onDiscard = {
                    actions.resetScreenState()
                    navController.popBackStack()
                },
                onSave = { actions.saveAndNavigate() })
        }

        is InteractionType.UnsavedChangesConfirmationBeforeCopy -> {
            FCCUnsavedChangesDialog(
                onDismiss = actions.resetScreenState,
                onDiscard = actions.discardChangesAndProceed,
                onSave = actions.saveChangesAndProceed
            )
        }

        is InteractionType.ContextualAddComponent -> {
            ModalBottomSheet(
                onDismissRequest = {
                    actions.resetScreenState()
                }, sheetState = addComponentSheetState
            ) {
                when (uiState.componentSelection) {
                    is ComponentSelection.ExistingComponent -> {
                        with(existingComponentFormUiState) {
                            ExistingComponentForm(
                                formData = formData,
                                dishName = uiState.dish?.name ?: "",
                                isAddButtonEnabled = isAddButtonEnabled,
                                compatibleUnitsForDish = compatibleUnitsForDish,
                                unitForDishDropdownExpanded = unitForDishDropdownExpanded,
                                selectedComponent = uiState.componentSelection.item,
                                onUnitForDishDropdownExpandedChange = existingComponentFormActions.onUnitForDishDropdownExpandedChange,
                                onCancel = existingComponentFormActions.onCancel,
                                onAddComponent = existingComponentFormActions.onAddComponent,
                                onFormDataChange = existingComponentFormActions.onFormDataChange
                            )
                        }
                    }

                    is ComponentSelection.NewComponent -> {
                        Text("TODO NEW PRODUCT FORM")
                    }

                    null -> ComponentLookupForm {
                        actions.setComponentSelection(
                            it
                        )
                    }
                }
            }
        }

        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Edit Dish Screen States", showBackground = true)
@PreviewLightDark
@Composable
private fun EditDishScreenContentPreview(
    @PreviewParameter(EditDishScreenStateProvider::class) state: DishDetailsUiState
) {
    val navController = rememberNavController()

    FCCTheme {
        EditDishScreenContent(
            uiState = state,
            navController = navController,
            actions = EditDishScreenActions(),
            modifier = Modifier,
            dishId = 0L,
            snackbarHostState = SnackbarHostState(),
            existingComponentFormUiState = ExistingComponentFormUiState(),
            existingComponentFormActions = ExistingComponentFormActions(),
            addComponentSheetState = rememberModalBottomSheetState()
        )
    }
}
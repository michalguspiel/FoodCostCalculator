package com.erdees.foodcostcalc.ui.screens.dishes.editDish

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.erdees.foodcostcalc.domain.model.UsedItem
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
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.DishDetailsUtil.getCopyDishPrefilledName
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormActions
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

    DishDetailsStateManager(
        uiState = uiState,
        context = context,
        navController = navController,
        snackbarHostState = snackbarHostState,
        existingFormViewModel = existingFormViewModel,
        viewModel = viewModel,
    )

    EditDishScreenContent(
        uiState = uiState,
        existingComponentFormUiState = existingFormUiState,
        dishId = dishId,
        navController = navController,
        actions = DishDetailsScreenActions(
            dishActions = DishActions(
                saveDish = viewModel::saveDish,
                shareDish = viewModel::shareDish,
                saveAndNavigate = viewModel::saveAndNavigate,
                resetScreenState = viewModel::resetScreenState,
            ),
            propertyActions = DishPropertyActions(
                updateName = viewModel::updateName,
                saveName = viewModel::saveDishName,
                updateTax = viewModel::updateTax,
                saveTax = viewModel::saveDishTax,
                updateMargin = viewModel::updateMargin,
                saveMargin = viewModel::saveDishMargin,
                updateTotalPrice = viewModel::updateTotalPrice,
                saveTotalPrice = viewModel::saveDishTotalPrice,
            ),
            itemActions = ItemActions(
                removeItem = viewModel::removeItem,
                updateQuantity = viewModel::updateQuantity,
                saveQuantity = viewModel::updateItemQuantity,
                setComponentSelection = viewModel::setComponentSelection,
                onAddExistingComponentClick = viewModel::onAddExistingComponent,
            ),
            deletionActions = DishDeletionActions(
                onDeleteDishClick = viewModel::onDeleteDishClick,
                onDeleteConfirmed = viewModel::confirmDelete,
            ),
            copyActions = DishCopyActions(
                onCopyDishClick = {
                    viewModel.handleCopyDish {
                        getCopyDishPrefilledName(
                            it, context
                        )
                    }
                },
                copyDish = viewModel::copyDish,
                updateCopiedDishName = viewModel::updateCopiedDishName,
                hideCopyConfirmation = viewModel::hideCopyConfirmation,
            ),
            interactionActions = ScreenInteractionActions(
                setInteraction = viewModel::setInteraction,
                saveChangesAndProceed = viewModel::saveChangesAndProceed,
                discardChangesAndProceed = {
                    viewModel.discardChangesAndProceed {
                        getCopyDishPrefilledName(
                            it, context
                        )
                    }
                },
            ),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDishScreenContent(
    uiState: DishDetailsUiState,
    existingComponentFormUiState: ExistingComponentFormUiState,
    dishId: Long,
    navController: NavController,
    actions: DishDetailsScreenActions,
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
                onNameClick = { actions.interactionActions.setInteraction(InteractionType.EditName) },
                onDeleteClick = { actions.deletionActions.onDeleteDishClick() },
                onCopyClick = { actions.copyActions.onCopyDishClick() },
                onShareClick = { actions.dishActions.shareDish(context) },
                onRecipeClick = { navController.navigate(FCCScreen.Recipe) },
                onBackClick = { navController.popBackStack() }
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column {
                LazyColumn(Modifier.weight(fill = true, weight = 1f)) {
                    items(uiState.items, key = {
                        when(it) {
                            is UsedItem -> it.id
                            else -> System.identityHashCode(it)
                        }
                    }) { item ->
                        UsedItem(
                            modifier = Modifier.animateItem(),
                            usedItem = item,
                            currency = uiState.currency,
                            onRemove = { actions.itemActions.removeItem(it) },
                            onEdit = {
                                actions.interactionActions.setInteraction(
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
                                actions.interactionActions.setInteraction(InteractionType.ContextualAddComponent)
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
                                    actions.interactionActions.setInteraction(InteractionType.EditTax)
                                },
                                onMarginClick = {
                                    actions.interactionActions.setInteraction(InteractionType.EditMargin)
                                },
                                onTotalPriceClick = {
                                    actions.interactionActions.setInteraction(InteractionType.EditTotalPrice)
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
                            actions.dishActions.saveDish()
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
                actions.copyActions.hideCopyConfirmation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenStateHandler(
    uiState: DishDetailsUiState,
    existingComponentFormUiState: ExistingComponentFormUiState,
    actions: DishDetailsScreenActions,
    existingComponentFormActions: ExistingComponentFormActions,
    addComponentSheetState: SheetState,
    navController: NavController,
) {
    when (uiState.screenState) {
        is ScreenState.Loading<*> -> ScreenLoadingOverlay()
        is ScreenState.Success<*> -> {} // NOTHING

        is ScreenState.Error -> {
            ErrorDialog {
                actions.dishActions.resetScreenState()
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
    actions: DishDetailsScreenActions,
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
                updateValue = { actions.propertyActions.updateTax(it) },
                onSave = { actions.propertyActions.saveTax() },
                onDismiss = { actions.dishActions.resetScreenState() })
        }

        InteractionType.EditMargin -> {
            ValueEditDialog(
                title = stringResource(R.string.edit_margin),
                value = uiState.editableFields.margin,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                updateValue = { actions.propertyActions.updateMargin(it) },
                onSave = { actions.propertyActions.saveMargin() },
                onDismiss = { actions.dishActions.resetScreenState() })
        }

        InteractionType.EditTotalPrice -> {
            ValueEditDialog(
                title = stringResource(R.string.edit_total_price),
                value = uiState.editableFields.totalPrice,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                updateValue = { actions.propertyActions.updateTotalPrice(it) },
                onSave = { actions.propertyActions.saveTotalPrice() },
                onDismiss = { actions.dishActions.resetScreenState() })
        }

        InteractionType.EditName -> {
            ValueEditDialog(
                title = stringResource(R.string.edit_name),
                value = uiState.editableFields.name,
                updateValue = { actions.propertyActions.updateName(it) },
                onSave = { actions.propertyActions.saveName() },
                onDismiss = { actions.dishActions.resetScreenState() },
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
                updateValue = { actions.copyActions.updateCopiedDishName(it) },
                onSave = { actions.copyActions.copyDish() },
                onDismiss = { actions.dishActions.resetScreenState() },
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
                updateValue = { actions.itemActions.updateQuantity(it) },
                onSave = { actions.itemActions.saveQuantity() },
                onDismiss = { actions.dishActions.resetScreenState() })
        }

        is InteractionType.DeleteConfirmation -> {
            FCCDeleteConfirmationDialog(
                itemName = interaction.itemName,
                onDismiss = { actions.dishActions.resetScreenState() },
                onConfirmDelete = {
                    actions.deletionActions.onDeleteConfirmed(interaction.itemId)
                })
        }

        InteractionType.UnsavedChangesConfirmation -> {
            FCCUnsavedChangesDialog(
                onDismiss = { actions.dishActions.resetScreenState() },
                onDiscard = {
                    actions.dishActions.resetScreenState()
                    navController.popBackStack()
                },
                onSave = { actions.dishActions.saveAndNavigate() })
        }

        is InteractionType.UnsavedChangesConfirmationBeforeCopy -> {
            FCCUnsavedChangesDialog(
                onDismiss = actions.dishActions.resetScreenState,
                onDiscard = actions.interactionActions.discardChangesAndProceed,
                onSave = actions.interactionActions.saveChangesAndProceed
            )
        }

        is InteractionType.ContextualAddComponent -> {
            DishDetailsModalSheet(
                sheetState = addComponentSheetState,
                componentSelection = uiState.componentSelection,
                dishName = uiState.dish?.name ?: "",
                dishDetailsActions = actions,
                existingComponentFormUiState = existingComponentFormUiState,
                existingComponentFormActions = existingComponentFormActions,
            )
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
            actions = DishDetailsScreenActions(),
            modifier = Modifier,
            dishId = 0L,
            snackbarHostState = SnackbarHostState(),
            existingComponentFormUiState = ExistingComponentFormUiState(),
            existingComponentFormActions = ExistingComponentFormActions(),
            addComponentSheetState = rememberModalBottomSheetState()
        )
    }
}

package com.erdees.foodcostcalc.ui.screens.dishes.dishdetails

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
import com.erdees.foodcostcalc.ui.screens.dishes.dishdetails.DishDetailsUtil.getCopyDishPrefilledName
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentLookupFormActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentLookupFormUiState
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentLookupViewModel
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.createActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormUiState
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormViewModel
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormUiState
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormViewModel
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Screen
@Composable
fun DishDetailsScreen(
    dishId: Long,
    navController:
    NavController,
    viewModel: DishDetailsViewModel = viewModel(),
    existingFormViewModel: ExistingComponentFormViewModel = viewModel(),
    newProductFormViewModel: NewProductFormViewModel = viewModel(),
    componentLookupViewModel: ComponentLookupViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val existingFormUiState by existingFormViewModel.uiState.collectAsState()

    val componentLookupFormUiState by componentLookupViewModel.uiState.collectAsState()
    val componentLookupFormActions = componentLookupViewModel.createActions(
        onNext = { viewModel.setComponentSelection(componentLookupViewModel.getComponentSelectionResult()) }
    )

    val newProductFormUiState = NewProductFormUiState(
        productName = (uiState.componentSelection as? ComponentSelection.NewComponent)?.name
            ?: "", // This will be set in the modal sheet based on componentSelection
        dishName = uiState.dish?.name ?: "",
        productCreationUnits = newProductFormViewModel.productCreationUnits.collectAsState().value,
        productAdditionUnits = newProductFormViewModel.productAdditionUnits.collectAsState().value,
        formData = newProductFormViewModel.formData.collectAsState().value,
        isAddButtonEnabled = newProductFormViewModel.isAddButtonEnabled.collectAsState().value,
        productCreationDropdownExpanded = newProductFormViewModel.productCreationUnitDropdownExpanded.collectAsState().value,
        productAdditionDropdownExpanded = newProductFormViewModel.productAdditionUnitDropdownExpanded.collectAsState().value,
    )

    val dishDetailsActions = viewModel.createActions(
        getCopyDishPrefilledName = { getCopyDishPrefilledName(it, context) }
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val addComponentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Initialize product creation units when the component sheet is opened
    LaunchedEffect(uiState.componentSelection) {
        if (uiState.componentSelection is ComponentSelection.NewComponent) {
            newProductFormViewModel.getProductCreationUnits(context.resources)
        }
    }

    BackHandler {
        viewModel.handleBackNavigation { navController.popBackStack() }
    }

    DishDetailsStateManager(
        uiState = uiState,
        navController = navController,
        snackbarHostState = snackbarHostState,
        actions = DishDetailsStateManagerActions(
            undoRemoveItem = viewModel::undoRemoveItem,
            clearLastRemovedItem = viewModel::clearLastRemovedItem,
            resetScreenState = viewModel::resetScreenState,
            handleCopyDish = { name ->
                viewModel.handleCopyDish {
                    getCopyDishPrefilledName(
                        it,
                        context
                    )
                }
            },
            setItemContext = { item ->
                existingFormViewModel.setItemContext(item, context.resources)
            }
        ),
    )

    EditDishScreenContent(
        uiState = uiState,
        existingComponentFormUiState = existingFormUiState,
        dishId = dishId,
        navController = navController,
        actions = dishDetailsActions,
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
        newProductFormUiState = newProductFormUiState,
        newProductFormActions = NewProductFormActions(
            onFormDataUpdate = newProductFormViewModel::updateFormData,
            onProductCreationDropdownExpandedChange = {
                newProductFormViewModel.productCreationUnitDropdownExpanded.value = it
            },
            onProductAdditionDropdownExpandedChange = {
                newProductFormViewModel.productAdditionUnitDropdownExpanded.value = it
            },
            onSaveProduct = { data ->
                scope.launch {
                    addComponentSheetState.hide()
                }.invokeOnCompletion {
                    viewModel.onAddNewProduct(data)
                    newProductFormViewModel.onAddIngredientClick()
                }
            }
        ),
        componentLookupFormUiState = componentLookupFormUiState,
        componentLookupFormActions = componentLookupFormActions,
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
    newProductFormUiState: NewProductFormUiState,
    newProductFormActions: NewProductFormActions,
    componentLookupFormUiState: ComponentLookupFormUiState,
    componentLookupFormActions: ComponentLookupFormActions,
    addComponentSheetState: SheetState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
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
                        when (it) {
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
                newProductFormUiState = newProductFormUiState,
                actions = actions,
                existingComponentFormActions = existingComponentFormActions,
                newProductFormActions = newProductFormActions,
                addComponentSheetState = addComponentSheetState,
                navController = navController,
                componentLookupFormUiState =  componentLookupFormUiState,
                componentLookupFormActions = componentLookupFormActions
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
    newProductFormUiState: NewProductFormUiState,
    actions: DishDetailsScreenActions,
    existingComponentFormActions: ExistingComponentFormActions,
    newProductFormActions: NewProductFormActions,
    addComponentSheetState: SheetState,
    navController: NavController,
    componentLookupFormUiState: ComponentLookupFormUiState,
    componentLookupFormActions: ComponentLookupFormActions
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
                newProductFormUiState = newProductFormUiState,
                interaction = uiState.screenState.interaction,
                actions = actions,
                existingComponentFormActions = existingComponentFormActions,
                newProductFormActions = newProductFormActions,
                navController = navController,
                addComponentSheetState = addComponentSheetState,
                componentLookupFormUiState = componentLookupFormUiState,
                componentLookupFormActions = componentLookupFormActions
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
    newProductFormUiState: NewProductFormUiState,
    interaction: InteractionType,
    actions: DishDetailsScreenActions,
    existingComponentFormActions: ExistingComponentFormActions,
    newProductFormActions: NewProductFormActions,
    addComponentSheetState: SheetState,
    navController: NavController,
    componentLookupFormUiState: ComponentLookupFormUiState,
    componentLookupFormActions: ComponentLookupFormActions
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
                newProductFormUiState = newProductFormUiState,
                newProductFormActions = newProductFormActions,
                componentLookupFormUiState = componentLookupFormUiState,
                componentLookupFormActions = componentLookupFormActions
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
    @PreviewParameter(EditDishScreenStateProvider::class) state: DishDetailsUiState,
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
            newProductFormUiState = NewProductFormUiState(),
            newProductFormActions = NewProductFormActions(),
            componentLookupFormUiState = ComponentLookupFormUiState(),
            componentLookupFormActions = ComponentLookupFormActions(),
            addComponentSheetState = rememberModalBottomSheetState()
        )
    }
}

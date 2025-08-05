package com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart

import android.icu.util.Currency
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.onboarding.OnboardingState
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.product.ProductAddedToDish
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.ui.composables.Ingredients
import com.erdees.foodcostcalc.ui.composables.buttons.FCCOutlinedButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.CreateDishV2ViewModel
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.SingleServing
import com.erdees.foodcostcalc.ui.screens.dishes.dishdetails.DishActions
import com.erdees.foodcostcalc.ui.screens.dishes.dishdetails.DishDetailsModalSheet
import com.erdees.foodcostcalc.ui.screens.dishes.dishdetails.DishDetailsScreenActions
import com.erdees.foodcostcalc.ui.screens.dishes.dishdetails.ItemActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentLookupViewModel
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.createActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormViewModel
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormUiState
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormViewModel
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDishStartScreen(
    navController: NavController,
    viewModel: CreateDishV2ViewModel = viewModel(),
    newProductFormViewModel: NewProductFormViewModel = viewModel(),
    existingProductFormViewModel: ExistingComponentFormViewModel = viewModel(),
    componentLookupViewModel: ComponentLookupViewModel = viewModel(),
) {
    val dishName by viewModel.dishName.collectAsState()
    val addedComponents by viewModel.addedComponents.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isFirstDish by viewModel.isFirstDish.collectAsState()
    val errorRes by viewModel.errorRes.collectAsState()
    val onboardingState by viewModel.onboardingState.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val componentSelection by viewModel.componentSelection.collectAsState()

    val existingFormUiState by existingProductFormViewModel.uiState.collectAsState()

    val componentLookupFormUiState by componentLookupViewModel.uiState.collectAsState()

    val componentLookupFormActions = componentLookupViewModel.createActions(
        onNext = { viewModel.setComponentSelection(componentLookupViewModel.getComponentSelectionResult()) }
    )

    val newProductFormUiState = NewProductFormUiState(
        productName = (componentSelection as? ComponentSelection.NewComponent)?.name ?: "",
        dishName = dishName,
        productCreationUnits = newProductFormViewModel.productCreationUnits.collectAsState().value,
        productAdditionUnits = newProductFormViewModel.productAdditionUnits.collectAsState().value,
        formData = newProductFormViewModel.formData.collectAsState().value,
        isAddButtonEnabled = newProductFormViewModel.isAddButtonEnabled.collectAsState().value,
        productCreationDropdownExpanded = newProductFormViewModel.productCreationUnitDropdownExpanded.collectAsState().value,
        productAdditionDropdownExpanded = newProductFormViewModel.productAdditionUnitDropdownExpanded.collectAsState().value,
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val addComponentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val snackbarMessage = stringResource(id = R.string.onboarding_complete_snackbar_text)

    LaunchedEffect(componentSelection) {
        if (componentSelection is ComponentSelection.NewComponent) {
            newProductFormViewModel.getProductCreationUnits()
        }
    }

    LaunchedEffect(onboardingState) {
        Timber.i("CreateDishStartScreen LaunchedEffect: onboardingState = $onboardingState")
        // If we are in this screen with OnboardingState.STARTED it means user has just completed it.
        if (onboardingState == OnboardingState.STARTED) {
            scope.launch {
                viewModel.onboardingComplete()
                snackbarHostState.showSnackbar(snackbarMessage)
            }
        }
    }

    LaunchedEffect(Unit) {
        newProductFormViewModel.getProductCreationUnits()
    }

    LaunchedEffect(componentSelection) {
        (componentSelection as? ComponentSelection.ExistingComponent)?.let {
            existingProductFormViewModel.setItemContext(it.item)
        }
    }

    CreateDishStartScreenContent(
        navController,
        CreateDishStartScreenState(
            dishName,
            addedComponents,
            currency,
            isFirstDish,
            errorRes
        ),
        snackbarHostState,
        viewModel::updateDishName,
        viewModel::dismissError,
        viewModel::onAddIngredientClick,
        onContinueClick = {
            navController.navigate(FCCScreen.CreateDishSummary)
        }
    )

    when (screenState) {
        is ScreenState.Interaction -> {
            when ((screenState as ScreenState.Interaction).interaction) {
                is InteractionType.ContextualAddComponent -> {
                    DishDetailsModalSheet(
                        sheetState = addComponentSheetState,
                        componentSelection = componentSelection,
                        dishName = dishName,
                        dishDetailsActions = DishDetailsScreenActions(
                            dishActions = DishActions(
                                resetScreenState = viewModel::resetScreenState,
                            ),
                            itemActions = ItemActions(
                                setComponentSelection = viewModel::setComponentSelection,
                                onAddExistingComponentClick = viewModel::onAddExistingComponent
                            ),
                        ),
                        existingComponentFormUiState = existingFormUiState,
                        existingComponentFormActions = ExistingComponentFormActions(
                            onFormDataChange = existingProductFormViewModel::updateFormData,
                            onUnitForDishDropdownExpandedChange = {
                                existingProductFormViewModel.unitForDishDropdownExpanded.value = it
                            },
                            onAddComponent = { data ->
                                scope.launch {
                                    addComponentSheetState.hide()
                                }.invokeOnCompletion {
                                    viewModel.onAddExistingComponent(data)
                                    existingProductFormViewModel.onAddIngredientClick()
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
                                newProductFormViewModel.productCreationUnitDropdownExpanded.value =
                                    it
                            },
                            onProductAdditionDropdownExpandedChange = {
                                newProductFormViewModel.productAdditionUnitDropdownExpanded.value =
                                    it
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
                        componentLookupFormActions = componentLookupFormActions
                    )
                }

                else -> {}
            }
        }

        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateDishStartScreenContent(
    navController: NavController,
    createDishStartScreenState: CreateDishStartScreenState,
    snackbarHostState: SnackbarHostState,
    updateDishName: (String) -> Unit,
    onErrorDismiss: () -> Unit,
    onAddIngredientClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasFocusBeenRequested = rememberSaveable { mutableStateOf(false) }
    val dishNameFocusRequester = remember { FocusRequester() }

    with(createDishStartScreenState) {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(title = {
                    Text(
                        text = stringResource(
                            id = if (isFirstDish) R.string.price_your_first_dish
                            else R.string.create_new_dish
                        )
                    )
                }, navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Sharp.ArrowBack,
                            contentDescription = stringResource(
                                id = R.string.back
                            )
                        )
                    }
                })
            }) { paddingValues ->
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
            ) {
                Column(
                    Modifier.padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.dish_name),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        FCCTextField(
                            modifier = Modifier
                                .focusRequester(dishNameFocusRequester)
                                .onGloballyPositioned {
                                    if (!hasFocusBeenRequested.value) {
                                        dishNameFocusRequester.requestFocus()
                                        hasFocusBeenRequested.value = true
                                    }
                                },
                            title = null,
                            value = dishName,
                            onValueChange = { updateDishName(it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            )
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.added_ingredients),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (addedComponents.isEmpty()) {
                            IngredientsEmptyState(modifier = Modifier.padding(vertical = 24.dp))
                        } else {
                            Ingredients(
                                addedComponents,
                                persistentListOf(),
                                SingleServing,
                                currency,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.size(16.dp))

                        FCCOutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = if (addedComponents.isEmpty()) {
                                stringResource(R.string.add_first_ingredient)
                            } else {
                                stringResource(R.string.add_more_ingredients)
                            },
                            onClick = { onAddIngredientClick() },
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp, bottom = 24.dp),
                ) {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    FCCPrimaryButton(
                        enabled = dishName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.continue_dish_creation),
                        onClick = { onContinueClick() }
                    )
                }
            }
            if (errorRes != null) {
                ErrorDialog(
                    content = stringResource(errorRes),
                    onDismiss = { onErrorDismiss() },
                )
            }
        }
    }
}

@Composable
private fun IngredientsEmptyState(
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(R.drawable.shopping_basket_24px),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = stringResource(R.string.ingredients_empty_state_headline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun CreateDishStartScreenContentPreview() {
    FCCTheme {
        CreateDishStartScreenContent(
            navController = rememberNavController(),
            createDishStartScreenState = CreateDishStartScreenState(
                dishName = "Spaghetti Bolognese",
                addedComponents = persistentListOf(
                    ProductAddedToDish(
                        ProductDomain(
                            id = 0L,
                            name = "Tomato",
                            pricePerUnit = 3.99,
                            tax = 0.0,
                            waste = 10.0,
                            unit = MeasurementUnit.KILOGRAM,
                            inputMethod = InputMethod.UNIT,
                            packagePrice = null,
                            packageQuantity = null,
                            packageUnit = null,
                        ),
                        0.5,
                        MeasurementUnit.KILOGRAM,
                    )
                ),
                currency = Currency.getInstance("PLN"),
                isFirstDish = false,
                errorRes = null
            ),
            snackbarHostState = remember { SnackbarHostState() },
            updateDishName = {},
            onAddIngredientClick = {},
            onContinueClick = {},
            onErrorDismiss = {},
        )
    }
}

@Preview
@Composable
private fun CreateDishStartScreenContentEmptyListPreview() {
    FCCTheme {
        CreateDishStartScreenContent(
            navController = rememberNavController(),
            createDishStartScreenState = CreateDishStartScreenState(
                dishName = "Spaghetti Bolognese",
                addedComponents = persistentListOf(),
                currency = Currency.getInstance("PLN"),
                isFirstDish = false,
                errorRes = null
            ),
            snackbarHostState = remember { SnackbarHostState() },
            updateDishName = {},
            onAddIngredientClick = {},
            onContinueClick = {},
            onErrorDismiss = {},
        )
    }
}

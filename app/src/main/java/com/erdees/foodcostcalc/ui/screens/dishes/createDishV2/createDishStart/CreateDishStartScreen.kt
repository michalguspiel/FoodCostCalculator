package com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart

import android.icu.util.Currency
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.product.ProductAddedToDish
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.composables.Ingredients
import com.erdees.foodcostcalc.ui.composables.Section
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextFieldWithSuggestions
import com.erdees.foodcostcalc.ui.composables.labels.SectionLabel
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.CreateDishV2ViewModel
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.SingleServing
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart.existingProductForm.ExistingProductForm
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart.existingProductForm.ExistingProductFormViewModel
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart.newProductForm.NewProductForm
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart.newProductForm.NewProductFormViewModel
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDishStartScreen(
    navController: NavController,
    viewModel: CreateDishV2ViewModel = viewModel(),
    newProductFormViewModel: NewProductFormViewModel = viewModel(),
    existingProductFormViewModel: ExistingProductFormViewModel = viewModel(),
) {
    val dishName by viewModel.dishName.collectAsState()
    val newProductName by viewModel.newProductName.collectAsState()
    val addedProducts by viewModel.addedProducts.collectAsState()
    val suggestedProducts by viewModel.suggestedProducts.collectAsState()
    val selectedProduct by viewModel.selectedSuggestedProduct.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val shouldShowSuggestedProducts by viewModel.shouldShowSuggestedProducts.collectAsState()
    val userIntent by viewModel.intent.collectAsState()
    val isFirstDish by viewModel.isFirstDish.collectAsState()
    val errorRes by viewModel.errorRes.collectAsState()

    val scope = rememberCoroutineScope()
    val newProductFormSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val existingProductFormSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var allowIngredientListAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(
        newProductFormSheetState.currentValue, existingProductFormSheetState.currentValue
    ) {
        allowIngredientListAnimation =
            newProductFormSheetState.currentValue == SheetValue.Hidden &&
                existingProductFormSheetState.currentValue == SheetValue.Hidden
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        newProductFormViewModel.getProductCreationUnits(context.resources)
    }

    LaunchedEffect(selectedProduct) {
        selectedProduct?.let {
            existingProductFormViewModel.setProductContext(it, context.resources)
        }
    }

    CreateDishStartScreenContent(
        navController,
        CreateDishStartScreenState(
            dishName,
            newProductName,
            shouldShowSuggestedProducts,
            addedProducts,
            suggestedProducts,
            selectedProduct,
            currency,
            isFirstDish,
            errorRes
        ),
        allowIngredientListAnimation,
        viewModel::updateDishName,
        viewModel::updateNewProductName,
        viewModel::onAddIngredientClick,
        viewModel::onSuggestionSelected,
        viewModel::onSuggestionsManuallyDismissed,
        viewModel::dismissError,
        onContinueClick = {
            navController.navigate(FCCScreen.CreateDishSummary)
        })

    AnimatedVisibility(userIntent != null) {
        when (userIntent) {
            is CreateDishIntent.AddNewProduct -> {
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.onModalDismissed()
                    }, sheetState = newProductFormSheetState
                ) {
                    NewProductForm(
                        productName = newProductName,
                        dishName = dishName,
                        productCreationUnits = newProductFormViewModel.productCreationUnits.collectAsState().value,
                        productAdditionUnits = newProductFormViewModel.productAdditionUnits.collectAsState().value,
                        formData = newProductFormViewModel.formData.collectAsState().value,
                        isAddButtonEnabled = newProductFormViewModel.isAddButtonEnabled.collectAsState().value,
                        productCreationDropdownExpanded = newProductFormViewModel.productCreationUnitDropdownExpanded.collectAsState().value,
                        onProductCreationDropdownExpandedChange = {
                            newProductFormViewModel.productCreationUnitDropdownExpanded.value = it
                        },
                        onProductAdditionDropdownExpandedChange = {
                            newProductFormViewModel.productAdditionUnitDropdownExpanded.value = it
                        },
                        productAdditionDropdownExpanded = newProductFormViewModel.productAdditionUnitDropdownExpanded.collectAsState().value,
                        onFormDataUpdate = newProductFormViewModel::updateFormData,
                        onSaveProduct = { data ->
                            scope.launch {
                                newProductFormSheetState.hide()
                            }.invokeOnCompletion {
                                viewModel.onAddNewProductClick(data)
                                newProductFormViewModel.onAddIngredientClick()
                            }
                        },
                    )
                }
            }

            is CreateDishIntent.AddProduct -> {
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.onModalDismissed()
                    }, sheetState = existingProductFormSheetState
                ) {
                    ExistingProductForm(
                        formData = existingProductFormViewModel.formData.collectAsState().value,
                        isAddButtonEnabled = existingProductFormViewModel.isAddButtonEnabled.collectAsState().value,
                        compatibleUnitsForDish = existingProductFormViewModel.compatibleUnitsForDish.collectAsState().value,
                        unitForDishDropdownExpanded = existingProductFormViewModel.unitForDishDropdownExpanded.collectAsState().value,
                        selectedProduct = (userIntent as CreateDishIntent.AddProduct).product,
                        dishName = dishName,
                        onFormDataChange = existingProductFormViewModel::updateFormData,
                        onUnitForDishDropdownExpandedChange = {
                            existingProductFormViewModel.unitForDishDropdownExpanded.value = it
                        },
                        onSaveIngredient = { data ->
                            scope.launch {
                                existingProductFormSheetState.hide()
                            }.invokeOnCompletion {
                                viewModel.onAddExistingProductClick(data)
                                existingProductFormViewModel.onAddIngredientClick()
                            }
                        },
                        onDismiss = {
                            viewModel.onModalDismissed()
                        }

                    )
                }
            }

            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateDishStartScreenContent(
    navController: NavController,
    createDishStartScreenState: CreateDishStartScreenState,
    allowAnimation: Boolean,
    updateDishName: (String) -> Unit,
    updateNewProductName: (String) -> Unit,
    onAddIngredientClick: () -> Unit,
    onSuggestedProductClick: (ProductDomain) -> Unit,
    onDismissSuggestions: () -> Unit,
    onErrorDismiss: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    with(createDishStartScreenState) {
        val dishNameFocusRequester = remember { FocusRequester() }
        val sectionVisible by remember(allowAnimation) {
            mutableStateOf(addedProducts.isNotEmpty())
        }

        Scaffold(
            modifier = modifier, topBar = {
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
                    Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.dish_name),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        FCCTextField(
                            modifier = Modifier.focusRequester(dishNameFocusRequester),
                            title = null,
                            value = dishName,
                            onValueChange = { updateDishName(it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
                        )
                    }

                    Column {
                        Text(
                            if (addedProducts.isEmpty()) stringResource(R.string.add_first_ingredient) else stringResource(
                                R.string.add_next_ingredient
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        FCCTextFieldWithSuggestions(
                            title = null,
                            value = newProductName,
                            placeholder = stringResource(R.string.product_name),
                            onValueChange = { updateNewProductName(it) },
                            suggestions = suggestedProducts?.take(3) ?: emptyList(),
                            shouldShowSuggestions = shouldShowSuggestedProducts,
                            onSuggestionSelected = {
                                onSuggestedProductClick(it)
                            },
                            onDismissSuggestions = {
                                onDismissSuggestions()
                            },
                            suggestionItemContent = { product ->
                                Text(text = product.name, modifier = Modifier.padding(8.dp))
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            )
                        )
                    }

                    FCCPrimaryButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        text = stringResource(R.string.add_product),
                        onClick = { onAddIngredientClick() },
                        enabled = newProductName.isNotEmpty()
                    )
                }

                Section(Modifier.animateContentSize(tween())) {
                    AnimatedVisibility(
                        visible = sectionVisible,
                        enter = fadeIn(animationSpec = tween()) + slideInHorizontally(),
                        exit = ExitTransition.None
                    ) {
                        Column {
                            SectionLabel(stringResource(R.string.added_ingredients))
                            Spacer(Modifier.size(8.dp))
                            Ingredients(
                                addedProducts,
                                persistentListOf(),
                                SingleServing,
                                currency,
                            )
                        }
                    }
                    FCCPrimaryButton(
                        enabled = dishName.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        text = stringResource(R.string.continue_dish_creation),
                        onClick = {
                            onContinueClick()
                        })
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

@Preview
@Composable
private fun CreateDishStartScreenContentPreview() {
    FCCTheme {
        CreateDishStartScreenContent(
            navController = rememberNavController(),
            createDishStartScreenState = CreateDishStartScreenState(
                dishName = "Spaghetti Bolognese",
                newProductName = "",
                shouldShowSuggestedProducts = false,
                addedProducts = persistentListOf(
                    ProductAddedToDish(
                        ProductDomain(0L, "Tomato", 3.99, 0.0, 10.0, "kg"),
                        0.5,
                        "kg",
                    )
                ),
                currency = Currency.getInstance("PLN"),
                isFirstDish = false,
                errorRes = null
            ),
            allowAnimation = true,
            updateDishName = {},
            updateNewProductName = {},
            onAddIngredientClick = {},
            onContinueClick = {},
            onErrorDismiss = {},
            onSuggestedProductClick = {},
            onDismissSuggestions = {})
    }
}

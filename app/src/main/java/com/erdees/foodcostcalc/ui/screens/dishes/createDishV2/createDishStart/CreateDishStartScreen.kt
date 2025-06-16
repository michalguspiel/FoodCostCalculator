package com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart

import android.icu.util.Currency
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextFieldWithSuggestions
import com.erdees.foodcostcalc.ui.composables.labels.SectionLabel
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.CreateDishV2ViewModel
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.SingleServing
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart.existingProductForm.ExistingProductForm
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart.existingProductForm.ExistingProductFormViewModel
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart.newProductForm.NewProductForm
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart.newProductForm.NewProductFormViewModel


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

    val newProductFormSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val existingProductFormSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

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
            currency
        ),
        viewModel::updateDishName,
        viewModel::updateNewProductName,
        viewModel::onAddIngredientClick,
        viewModel::onSuggestionSelected,
        viewModel::onSuggestionsManuallyDismissed,
        onContinueClick = {
            navController.navigate(FCCScreen.CreateDishSummary)
        }
    )

    AnimatedVisibility(userIntent != null) {
        when (userIntent) {
            is CreateDishIntent.AddNewProduct -> {
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.onModalDismissed()
                    },
                    sheetState = newProductFormSheetState
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
                        onSaveProduct = {
                            viewModel.onAddNewProductClick(it)
                            newProductFormViewModel.onAddIngredientClick()
                        },
                    )
                }
            }

            is CreateDishIntent.AddProduct -> {
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.onModalDismissed()
                    },
                    sheetState = existingProductFormSheetState
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
                        onSaveIngredient = {
                            viewModel.onAddExistingProductClick(it)
                            existingProductFormViewModel.onAddIngredientClick()
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
    updateDishName: (String) -> Unit,
    updateNewProductName: (String) -> Unit,
    onAddIngredientClick: () -> Unit,
    onSuggestedProductClick: (ProductDomain) -> Unit,
    onDismissSuggestions: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dishNameFocusRequester = remember { FocusRequester() }
    with(createDishStartScreenState) {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.price_your_first_dish)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Sharp.ArrowBack,
                                contentDescription = stringResource(
                                    id = R.string.back
                                )
                            )
                        }
                    })
            }
        ) { paddingValues ->
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
            ) {

                Column(Modifier.padding(horizontal = 12.dp)) {
                    FCCTextField(
                        modifier = Modifier.focusRequester(dishNameFocusRequester),
                        title = stringResource(R.string.dish_name),
                        value = dishName,
                        onValueChange = { updateDishName(it) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(Modifier.size(24.dp))
                    Text(
                        if (addedProducts.isEmpty()) stringResource(R.string.add_first_ingredient) else stringResource(
                            R.string.add_next_ingredient
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )

                    FCCTextFieldWithSuggestions(
                        title = stringResource(R.string.product_name),
                        value = newProductName,
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
                            imeAction = ImeAction.Next
                        )
                    )

                    ButtonRow(
                        primaryButton = {
                            FCCPrimaryButton(
                                stringResource(R.string.add_product),
                                onClick = { onAddIngredientClick() }
                            )
                        }
                    )
                }

                Section {
                    SectionLabel(stringResource(R.string.added_ingredients))
                    Ingredients(addedProducts, listOf(), SingleServing, currency)
                    ButtonRow(
                        applyDefaultPadding = false,
                        primaryButton = {
                            FCCPrimaryButton(stringResource(R.string.continue_dish_creation), onClick = {
                                onContinueClick()
                            })
                        })
                }
            }
        }
    }
}

@Preview
@Composable
private fun CreateDishStartScreenContentPreview() {
    CreateDishStartScreenContent(
        navController = rememberNavController(),
        createDishStartScreenState = CreateDishStartScreenState(
            dishName = "Spaghetti Bolognese",
            newProductName = "",
            shouldShowSuggestedProducts = false,
            addedProducts = listOf(
                ProductAddedToDish(
                    ProductDomain(0L, "Tomato", 3.99, 0.0, 10.0, "kg"),
                    0.5,
                    "kg",
                )
            ),
            currency = Currency.getInstance("PLN")
        ),
        updateDishName = {},
        updateNewProductName = {},
        onAddIngredientClick = {},
        onContinueClick = {},
        onSuggestedProductClick = {},
        onDismissSuggestions = {})
}

package com.erdees.foodcostcalc.ui.screens.dishes.createDish

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.utils.Utils // Import Utils for formatting
import com.erdees.foodcostcalc.utils.onNumericValueChange
import java.util.Currency // Import Currency
import java.util.Locale // Import Locale


// Comment out or remove mock data class if no longer needed for previews after this step
// data class IngredientItem(val id: Int, val name: String, val quantity: Double, val unit: String)

@OptIn(ExperimentalMaterial3Api::class)
@Screen
@Composable
fun CreateDishScreen(
    navController: NavController,
    viewModel: CreateDishScreenViewModel = viewModel()
) {

    val addedDish by viewModel.addedDish.collectAsState()
    val dishName by viewModel.dishName.collectAsState()
    val margin by viewModel.margin.collectAsState()
    val tax by viewModel.tax.collectAsState()
    val addButtonEnabled by viewModel.addButtonEnabled.collectAsState()
    val newIngredientName by viewModel.newIngredientName.collectAsState()
    val suggestedIngredients by viewModel.suggestedIngredients.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()

    val foodCost by viewModel.foodCost.collectAsState()
    val finalPrice by viewModel.finalPrice.collectAsState()
    // TODO: Replace with currency from ViewModel once available.
    // For now, using a placeholder. Proper integration is noted as a follow-up.
    val placeholderCurrency: Currency = Currency.getInstance(Locale.getDefault())


    val showNewIngredientDialog by viewModel.showNewIngredientDialog.collectAsState()
    val newIngredientPurchasePrice by viewModel.newIngredientPurchasePrice.collectAsState()
    val newIngredientPurchaseUnit by viewModel.newIngredientPurchaseUnit.collectAsState()
    val newIngredientWastePercentage by viewModel.newIngredientWastePercentage.collectAsState()


    val snackbarHostState = remember { SnackbarHostState() }

    val focusRequester = remember { FocusRequester() }
    val itemAddedText = stringResource(id = R.string.item_added)

    var textFieldLoaded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(addedDish) {
        addedDish?.let {
            snackbarHostState.showSnackbar(itemAddedText)
            viewModel.resetAddedDish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.create_dish)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = stringResource(
                                id = R.string.back
                            )
                        )
                    }
                })
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Column {
                    FieldLabel(
                        text = stringResource(id = R.string.name),
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onGloballyPositioned {
                                if (!textFieldLoaded) {
                                    focusRequester.requestFocus()
                                    // Prevent the focusRequester from being called again
                                    textFieldLoaded = true
                                }
                            },
                        value = dishName,
                        singleLine = true,
                        maxLines = 1,
                        onValueChange = { viewModel.dishName.value = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                }


                Column {
                    FieldLabel(
                        text = stringResource(id = R.string.margin),
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = margin,
                        singleLine = true,
                        maxLines = 1,
                        onValueChange = {
                            onNumericValueChange(it, viewModel.margin)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                Column {
                    FieldLabel(
                        text = stringResource(id = R.string.tax),
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = tax,
                        singleLine = true,
                        maxLines = 1,
                        onValueChange = {
                            onNumericValueChange(it, viewModel.tax)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                }

                // Ingredients Section
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = stringResource(id = R.string.ingredients_subheader),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box { // Box to allow suggestions to overlay if needed, though current setup might not require overlay
                    Column {
                        OutlinedTextField(
                            value = newIngredientName,
                            onValueChange = { viewModel.newIngredientName.value = it },
                            label = { Text(stringResource(id = R.string.add_ingredient)) },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.onAddIngredientClicked() }) { // Updated onClick
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = stringResource(id = R.string.add_ingredient_icon_desc)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done // Changed from Search to Done as primary action is adding
                            )
                        )

                        if (suggestedIngredients.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp) // Space between TextField and Card
                                    .heightIn(max = 200.dp), // Limit height of suggestions
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(suggestedIngredients) { product ->
                                        Text(
                                            text = product.name,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.onSuggestionSelected(product) }
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                        )
                                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                    }
                                }
                            }
                        }
                    }
                }

                if (ingredients.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.no_ingredients_added),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    // This LazyColumn is for ALREADY ADDED ingredients
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp) // Constrain height if it's inside a vertically scrolling parent
                            .padding(vertical = 8.dp)
                    ) {
                        items(ingredients, key = { it.tempClientId }) { ingredient ->
                            val dishQuantity by ingredient.dishQuantity.collectAsState()
                            val dishUnit by ingredient.dishUnit.collectAsState()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = ingredient.productName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(0.4f) // Give more weight to name
                                )
                                OutlinedTextField(
                                    value = dishQuantity,
                                    onValueChange = { newQty -> viewModel.updateDishIngredientQuantity(ingredient, newQty) },
                                    label = { Text(stringResource(id = R.string.quantity_short)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                    modifier = Modifier.weight(0.25f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = dishUnit,
                                    onValueChange = { newUnit -> viewModel.updateDishIngredientUnit(ingredient, newUnit) },
                                    label = { Text(stringResource(id = R.string.unit_short)) },
                                    modifier = Modifier.weight(0.2f),
                                    singleLine = true
                                )
                                IconButton(
                                    onClick = { viewModel.onRemoveIngredient(ingredient) },
                                    modifier = Modifier.weight(0.15f)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = stringResource(id = R.string.remove_ingredient_desc)
                                    )
                                }
                            }
                            Divider()
                        }
                    }
                }


                // Summary Section
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = stringResource(id = R.string.summary_subheader),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(text = stringResource(id = R.string.food_cost_label, Utils.formatPrice(foodCost, placeholderCurrency)))
                Text(text = stringResource(id = R.string.final_price_label, Utils.formatPrice(finalPrice, placeholderCurrency)))

            }
            ButtonRow(primaryButton = {
                FCCPrimaryButton(
                    enabled = addButtonEnabled,
                    onClick = { viewModel.addDish() },
                    text = stringResource(id = R.string.add)
                )
            })
        }

        if (showNewIngredientDialog) {
            NewIngredientDialog(
                ingredientName = newIngredientName,
                purchasePrice = newIngredientPurchasePrice,
                onPurchasePriceChange = { viewModel.newIngredientPurchasePrice.value = it },
                purchaseUnit = newIngredientPurchaseUnit,
                onPurchaseUnitChange = { viewModel.newIngredientPurchaseUnit.value = it },
                wastePercentage = newIngredientWastePercentage,
                onWastePercentageChange = { viewModel.newIngredientWastePercentage.value = it },
                onDismiss = { viewModel.onDismissNewIngredientDialog() },
                onSave = { viewModel.onSaveNewIngredient() }
            )
        }
    }
}

@Composable
fun NewIngredientDialog(
    ingredientName: String,
    purchasePrice: String,
    onPurchasePriceChange: (String) -> Unit,
    purchaseUnit: String,
    onPurchaseUnitChange: (String) -> Unit,
    wastePercentage: String,
    onWastePercentageChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.dialog_title_add_new_ingredient),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${stringResource(id = R.string.ingredient_name_label)}: $ingredientName",
                    style = MaterialTheme.typography.bodyMedium
                ) // Displaying name, non-editable in this dialog context

                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = onPurchasePriceChange,
                    label = { Text(stringResource(id = R.string.purchase_price_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = purchaseUnit,
                    onValueChange = onPurchaseUnitChange,
                    label = { Text(stringResource(id = R.string.purchase_unit_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = wastePercentage,
                    onValueChange = onWastePercentageChange,
                    label = { Text(stringResource(id = R.string.waste_percentage_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onSave) {
                        Text(stringResource(id = R.string.save))
                    }
                }
            }
        }
    }
}

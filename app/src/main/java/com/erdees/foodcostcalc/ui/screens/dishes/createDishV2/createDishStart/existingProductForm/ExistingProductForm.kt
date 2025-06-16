package com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart.existingProductForm // Or a more general location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.fields.UnitField
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.onNumericValueChange


@Composable
fun ExistingProductForm(
    formData: ExistingProductFormData,
    isAddButtonEnabled: Boolean,
    compatibleUnitsForDish: Set<String>,
    unitForDishDropdownExpanded: Boolean,
    selectedProduct: ProductDomain,
    dishName: String,
    onFormDataChange: (ExistingProductFormData) -> Unit, // More generic callback for form data changes
    onUnitForDishDropdownExpandedChange: (Boolean) -> Unit,
    onSaveIngredient: (ExistingProductFormData) -> Unit,
    onDismiss: () -> Unit,
) {
    val quantityFocusRequester = remember { FocusRequester() }
    val unitForDishFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    val localFocusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add Ingredient: ${selectedProduct.name}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        FCCTextField(
            modifier = Modifier.focusRequester(quantityFocusRequester),
            title = "Quantity for '$dishName'",
            value = formData.quantityForDish,
            onValueChange = { newValue ->
                onFormDataChange(
                    formData.copy(
                        quantityForDish = onNumericValueChange(
                            formData.quantityForDish,
                            newValue
                        )
                    )
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    unitForDishFocusRequester.requestFocus()
                    onUnitForDishDropdownExpandedChange(true)
                }
            )
        )

        UnitField(
            modifier = Modifier.focusRequester(unitForDishFocusRequester),
            label = "Unit for dish",
            units = compatibleUnitsForDish,
            expanded = unitForDishDropdownExpanded,
            onExpandChange = { isExpanded -> onUnitForDishDropdownExpandedChange(isExpanded) },
            selectedUnit = formData.unitForDish,
            selectUnit = { selectedUnit ->
                onFormDataChange(formData.copy(unitForDish = selectedUnit))
                // Optionally hide keyboard or move focus
                onUnitForDishDropdownExpandedChange(false) // Close dropdown after selection
                localFocusManager.clearFocus() // Example: Clear focus after selection
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ButtonRow(
            primaryButton = {
                FCCPrimaryButton(
                    text = "Add Ingredient",
                    enabled = isAddButtonEnabled,
                    onClick = { onSaveIngredient(formData) }
                )
            },
            secondaryButton = {
                FCCTextButton(
                    text = "Cancel",
                    onClick = onDismiss
                )
            }
        )
    }
}


@Preview(showBackground = true, name = "Existing Product Form - Light")
@PreviewLightDark
@Composable
private fun ExistingProductIngredientFormPreview() {
    val previewProduct = ProductDomain(
        id = 1L, name = "Flour", pricePerUnit = 2.5, tax = 0.0, unit = "kg", waste = 10.0
    )
    FCCTheme {
        ExistingProductForm(
            formData = ExistingProductFormData(quantityForDish = "100", unitForDish = "g"),
            isAddButtonEnabled = true,
            compatibleUnitsForDish = setOf("g", "kg", "oz", "lb"),
            unitForDishDropdownExpanded = false,
            selectedProduct = previewProduct,
            dishName = "Bread",
            onFormDataChange = {},
            onDismiss = {},
            onSaveIngredient = {},
            onUnitForDishDropdownExpandedChange = {})
    }
}
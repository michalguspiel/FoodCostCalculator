package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.fields.UnitField
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.onNumericValueChange

@Composable
fun NewProductForm(
    state: NewProductFormUiState,
    modifier: Modifier = Modifier,
    onProductCreationDropdownExpandedChange: (Boolean) -> Unit = {},
    onProductAdditionDropdownExpandedChange: (Boolean) -> Unit = {},
    onFormDataUpdate: (NewProductFormData) -> Unit = {},
    onSaveProduct: (NewProductFormData) -> Unit = {},
) {
    val purchaseUnitFocusRequester = remember { FocusRequester() }
    val productAdditionUnitFocusRequester = remember { FocusRequester() }
    val wastePercentFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    with(state) {
        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("New Ingredient: $productName", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            FCCTextField(
                title = "Purchase Price",
                value = formData.purchasePrice,
                onValueChange = {
                    val newValue = onNumericValueChange(formData.purchasePrice, it)
                    onFormDataUpdate(formData.copy(purchasePrice = newValue))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    purchaseUnitFocusRequester.requestFocus()
                    onProductCreationDropdownExpandedChange(true)
                })
            )

            UnitField(
                modifier = Modifier.focusRequester(purchaseUnitFocusRequester),
                label = "Purchase unit",
                expanded = productCreationDropdownExpanded,
                onExpandChange = { onProductCreationDropdownExpandedChange(it) },
                units = productCreationUnits,
                selectedUnit = formData.purchaseUnit,
                selectUnit = {
                    onFormDataUpdate(formData.copy(purchaseUnit = it))
                    wastePercentFocusRequester.requestFocus()
                }
            )

            // Waste %
            FCCTextField(
                modifier = Modifier.focusRequester(wastePercentFocusRequester),
                value = formData.wastePercent,
                title = "Waste % (Optional)",
                onValueChange = {
                    val newValue = onNumericValueChange(formData.wastePercent, it)
                    onFormDataUpdate(formData.copy(wastePercent = newValue))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            FCCTextField(
                value = formData.quantityAddedToDish,
                title = "Quantity for '$dishName'",
                onValueChange = {
                    val newValue = onNumericValueChange(formData.quantityAddedToDish, it)
                    onFormDataUpdate(formData.copy(quantityAddedToDish = newValue))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    productAdditionUnitFocusRequester.requestFocus()
                    onProductAdditionDropdownExpandedChange(true)
                })
            )

            UnitField(
                modifier = Modifier.focusRequester(productAdditionUnitFocusRequester),
                label = "Unit for dish",
                units = productAdditionUnits,
                expanded = productAdditionDropdownExpanded,
                onExpandChange = { onProductAdditionDropdownExpandedChange(it) },
                selectedUnit = formData.unitForDish,
                selectUnit = {
                    onFormDataUpdate(formData.copy(unitForDish = it))
                })

            ButtonRow(
                modifier = Modifier.padding(vertical = 12.dp),
                applyDefaultPadding = false,
                primaryButton = {
                    FCCPrimaryButton("Add Ingredient", enabled = isAddButtonEnabled) {
                        onSaveProduct(formData)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "New Product Form")
@PreviewLightDark
@Composable
private fun NewProductIngredientModalContentDarkPreview() {
    FCCTheme {
        NewProductForm(
            NewProductFormUiState(
                productName = "Sugar",
                dishName = "Cake",
                productAdditionUnits = setOf("kg", "g", "l", "ml"),
                productCreationUnits = setOf("per kilogram", "per liter"),
                productAdditionDropdownExpanded = false,
                productCreationDropdownExpanded = false,
                formData = NewProductFormData(
                    purchasePrice = "12.99",
                    purchaseUnit = "per kilogram",
                    "10",
                    "200",
                    "gram"
                ),
                isAddButtonEnabled = true,
            ),
            onSaveProduct = {
                println("Preview (Dark): Add Ingredient Clicked with data:")
            },
            onProductAdditionDropdownExpandedChange = {},
            onProductCreationDropdownExpandedChange = {},
            onFormDataUpdate = {},
        )
    }
}
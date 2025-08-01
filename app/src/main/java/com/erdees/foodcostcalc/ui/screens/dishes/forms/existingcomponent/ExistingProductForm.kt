package com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent // Or a more general location

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.fields.UnitField
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.onNumericValueChange

@Composable
fun ExistingComponentForm(
    formData: ExistingItemFormData,
    isAddButtonEnabled: Boolean,
    compatibleUnitsForDish: Set<String>,
    unitForDishDropdownExpanded: Boolean,
    selectedComponent: Item,
    dishName: String,
    modifier: Modifier = Modifier,
    onFormDataChange: (ExistingItemFormData) -> Unit = {},
    onUnitForDishDropdownExpandedChange: (Boolean) -> Unit = {},
    onAddComponent: (ExistingItemFormData) -> Unit = {},
    onCancel: () -> Unit,
) {
    val quantityFocusRequester = remember { FocusRequester() }
    val unitForDishFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    val localFocusManager = LocalFocusManager.current

    val title = when (selectedComponent) {
        is ProductDomain -> stringResource(R.string.add_product_with_name, selectedComponent.name)
        is HalfProductDomain -> stringResource(
            R.string.add_half_product_with_name,
            selectedComponent.name
        )

        else -> stringResource(R.string.add_product)
    }
    val primaryButtonText = when (selectedComponent) {
        is ProductDomain -> stringResource(R.string.add_product)
        is HalfProductDomain -> stringResource(R.string.add_half_product)
        else -> stringResource(R.string.add_product)
    }
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
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
            modifier = Modifier.padding(vertical = 12.dp),
            applyDefaultPadding = false,
            primaryButton = {
                FCCPrimaryButton(
                    text = primaryButtonText,
                    enabled = isAddButtonEnabled,
                    onClick = { onAddComponent(formData) }
                )
            },
            secondaryButton = {
                FCCTextButton(
                    text = stringResource(R.string.cancel),
                    onClick = onCancel
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
        ExistingComponentForm(
            formData = ExistingItemFormData(quantityForDish = "100", unitForDish = "g"),
            isAddButtonEnabled = true,
            compatibleUnitsForDish = setOf("g", "kg", "oz", "lb"),
            unitForDishDropdownExpanded = false,
            selectedComponent = previewProduct,
            dishName = "Bread",
            onFormDataChange = {},
            onCancel = {},
            onAddComponent = {},
            onUnitForDishDropdownExpandedChange = {})
    }
}
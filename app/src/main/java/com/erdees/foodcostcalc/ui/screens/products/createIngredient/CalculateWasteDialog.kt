package com.erdees.foodcostcalc.ui.screens.products.createIngredient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCDialog
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.utils.onNumericValueChange

@Composable
fun CalculateWasteDialog(
    onDismiss: () -> Unit,
    onSave: (totalQuantity: Double?, wasteQuantity: Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    var totalQuantity by remember {
        mutableStateOf("")
    }

    var wasteQuantity by remember {
        mutableStateOf("")
    }

    FCCDialog(
        modifier = modifier,
        title = stringResource(id = R.string.count_waste),
        onDismiss = { onDismiss() },
        onPrimaryButtonClick = {
            onSave(
                totalQuantity.toDoubleOrNull(),
                wasteQuantity.toDoubleOrNull()
            )
        }) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            FCCTextField(
                title = stringResource(id = R.string.quantity_before_processing),
                value = totalQuantity,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            ) {
                totalQuantity = onNumericValueChange(oldValue = totalQuantity, newValue = it)
            }

            FCCTextField(
                title = stringResource(id = R.string.waste_quantity),
                value = wasteQuantity,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            ) {
                wasteQuantity = onNumericValueChange(oldValue = wasteQuantity, newValue = it)
            }

        }
    }
}
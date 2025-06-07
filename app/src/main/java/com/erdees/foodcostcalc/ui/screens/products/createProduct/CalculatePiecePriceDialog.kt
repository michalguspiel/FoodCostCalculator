package com.erdees.foodcostcalc.ui.screens.products.createProduct

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
import com.erdees.foodcostcalc.utils.onIntegerValueChange
import com.erdees.foodcostcalc.utils.onNumericValueChange

@Composable
fun CalculatePiecePriceDialog(
    onDismiss: () -> Unit,
    onSave: (boxPrice: Double?, quantityInBox: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var boxPrice by remember {
        mutableStateOf("")
    }

    var quantityInBox by remember {
        mutableStateOf("")
    }

    FCCDialog(
        modifier = modifier,
        title = stringResource(id = R.string.calculate_price_per_piece),
        onDismiss = { onDismiss() },
        onPrimaryButtonClick = {
            onSave(
                boxPrice.toDoubleOrNull(),
                quantityInBox.toIntOrNull()
            )
        }) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            FCCTextField(
                title = stringResource(id = R.string.box_price), value = boxPrice,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            ) {
                boxPrice = onNumericValueChange(oldValue = boxPrice, newValue = it)
            }

            FCCTextField(
                title = stringResource(id = R.string.box_quantity), value = quantityInBox,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            ) {
                quantityInBox = onIntegerValueChange(oldValue = quantityInBox, newValue = it)
            }

        }
    }
}
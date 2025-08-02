package com.erdees.foodcostcalc.ui.composables.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel
import com.erdees.foodcostcalc.ui.screens.dishes.addItemToDish.SelectedTab
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun AddItemFields(
    items: List<Item>,
    units: Set<MeasurementUnit>,
    quantity: String,
    selectedTab: SelectedTab,
    selectedItem: Item?,
    selectedUnit: MeasurementUnit?,
    modifier: Modifier = Modifier,
    selectItem: (Item) -> Unit,
    selectUnit: (MeasurementUnit) -> Unit,
    setQuantity: (String) -> Unit,
    extraField: @Composable () -> Unit = {}
) {

    val itemFieldLabel =
        if (selectedTab == SelectedTab.ADD_PRODUCT) stringResource(id = R.string.product)
        else stringResource(id = R.string.half_product)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column {
            FieldLabel(
                text = itemFieldLabel,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
            ItemField(
                items = items,
                selectedItem = selectedItem,
                selectItem = selectItem,
                selectedTab = selectedTab
            )
        }

        Column {
            FieldLabel(
                text = stringResource(id = R.string.quantity),
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
            QuantityField(
                quantity = quantity,
                modifier = Modifier.fillMaxWidth(),
                setQuantity = setQuantity
            )
        }
        UnitField(units = units, selectedUnit = selectedUnit, selectUnit = selectUnit)


        extraField()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemField(
    items: List<Item>,
    selectedItem: Item?,
    selectedTab: SelectedTab,
    modifier: Modifier = Modifier,
    selectItem: (Item) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val optionalHint =
        if (selectedTab == SelectedTab.ADD_PRODUCT) stringResource(id = R.string.select_product)
        else stringResource(id = R.string.select_half_product)

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            modifier = Modifier,
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {

            TextField(
                // The `menuAnchor` modifier must be passed to the text field to handle
                // expanding/collapsing the menu on click. A read-only text field has
                // the anchor type `PrimaryNotEditable`.
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = selectedItem?.name ?: optionalHint,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )

            DropdownMenu(
                modifier = Modifier
                    .exposedDropdownSize(true),
                expanded = expanded,
                onDismissRequest = { expanded = false }) {
                items.forEach { product ->
                    DropdownMenuItem(
                        onClick = {
                            selectItem(product)
                            expanded = false
                        },
                        text = { Text(text = product.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuantityField(
    quantity: String,
    modifier: Modifier = Modifier,
    setQuantity: (String) -> Unit
) {
    OutlinedTextField(
        value = quantity,
        singleLine = true,
        onValueChange = { newValue ->
            setQuantity(newValue)
        },
        modifier = modifier,
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.End
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}


@Preview
@Composable
private fun PreviewFields() {
    FCCTheme {
        Box(Modifier.background(Color.Gray)) {
            AddItemFields(
                items = listOf(),
                units = setOf(MeasurementUnit.KILOGRAM, MeasurementUnit.GRAM),
                quantity = "1",
                selectedTab = SelectedTab.ADD_PRODUCT,
                selectedItem = null,
                selectedUnit = MeasurementUnit.GRAM,
                selectItem = {},
                selectUnit = {},
                setQuantity = {}
            )
        }
    }
}


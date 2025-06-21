package com.erdees.foodcostcalc.ui.composables.fields

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel


@Composable
fun UnitField(
    units: Set<String>,
    selectedUnit: String,
    modifier: Modifier = Modifier,
    label: String = stringResource(id = R.string.unit),
    selectUnit: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    UnitField(
        expanded = expanded,
        units = units,
        label = label,
        selectedUnit = selectedUnit,
        modifier = modifier,
        selectUnit = selectUnit,
        onExpandChange = { expanded = it },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitField(
    expanded: Boolean,
    units: Set<String>,
    selectedUnit: String,
    modifier: Modifier = Modifier,
    label: String = stringResource(id = R.string.unit),
    onExpandChange: (Boolean) -> Unit = {},
    selectUnit: (String) -> Unit = {},
){
    Column(modifier = modifier) {
        FieldLabel(
            text = label,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Box {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { onExpandChange(it) },
            ) {

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    value = selectedUnit,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(focusedPlaceholderColor = Color.Transparent),
                )

                DropdownMenu(
                    modifier = Modifier.exposedDropdownSize(true),
                    expanded = expanded,
                    onDismissRequest = { onExpandChange(false)}) {
                    units.forEach { unit ->
                        DropdownMenuItem(onClick = {
                            selectUnit(unit)
                            onExpandChange(false)
                        }, text = {
                            Text(unit)
                        })
                    }
                }
            }
        }
    }
}
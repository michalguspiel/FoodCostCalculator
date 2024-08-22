package com.erdees.foodcostcalc.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitField(
  units: Set<String>,
  selectedUnit: String,
  modifier: Modifier = Modifier,
  selectUnit: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = it },
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
        onDismissRequest = { expanded = false }) {
        units.forEach { unit ->
          DropdownMenuItem(onClick = {
            selectUnit(unit)
            expanded = false
          }, text = {
            Text(unit)
          })
        }
      }
    }
  }
}

package com.erdees.foodcostcalc.ui.screens.dishes.addItemToDish

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel
import com.erdees.foodcostcalc.ui.theme.FCCTheme

enum class SelectedTab {
  ADD_PRODUCT,
  ADD_HALF_PRODUCT
}

//TODO: String resources.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemToDishScreen(dishId: Long, dishName: String) {

  val viewModel: AddItemToDishViewModel = viewModel()

  val selectedTab by viewModel.selectedTab.collectAsState()
  val selectedItem by viewModel.selectedItem.collectAsState()
  val products by viewModel.products.collectAsState()
  val halfProducts by viewModel.halfProducts.collectAsState()
  val units by viewModel.units.collectAsState()
  val selectedUnit by viewModel.selectedUnit.collectAsState()
  val quantity by viewModel.quantity.collectAsState()
  val screenState by viewModel.screenState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(screenState) {
    when (screenState) {
      is ScreenState.Success -> {
        snackbarHostState.showSnackbar("Item added.", duration = SnackbarDuration.Short)
        viewModel.resetScreenState()
      }

      else -> {}
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      TopAppBar(title = {
        Text(text = dishName)
      })
    }) { paddingValues ->
    Box(Modifier.padding(paddingValues), contentAlignment = Alignment.Center) {
      Column(Modifier) {

        // Tabs
        Row {
          Tab(
            modifier = Modifier.weight(1f),
            selected = selectedTab == SelectedTab.ADD_PRODUCT,
            onClick = { viewModel.selectTab(SelectedTab.ADD_PRODUCT) },
            text = { Text(text = "Add Product") },
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurface
          )

          Tab(
            modifier = Modifier.weight(1f),
            selected = selectedTab == SelectedTab.ADD_HALF_PRODUCT,
            onClick = { viewModel.selectTab(SelectedTab.ADD_HALF_PRODUCT) }, text = {
              Text(text = "Add Half Product")
            },
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurface
          )
        }
        HorizontalDivider()

        // Fields
        val items = if (selectedTab == SelectedTab.ADD_PRODUCT) products else halfProducts

        Column(
          verticalArrangement = Arrangement.SpaceBetween,
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp)
            .padding(horizontal = 12.dp)
        ) {
          Fields(
            modifier = Modifier
              .fillMaxWidth(),
            items = items,
            units = units,
            selectedItem = selectedItem,
            selectedUnit = selectedUnit,
            selectedTab = selectedTab,
            quantity = quantity,
            selectItem = { viewModel.selectItem(it) },
            selectUnit = { viewModel.selectUnit(it) },
            setQuantity = { viewModel.setQuantity(it) }
          )

          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = { viewModel.addItem(dishId) }) {
              Text(text = "Add")
            }
          }
        }
      } // Column

      when (screenState) {
        is ScreenState.Loading -> ScreenLoadingOverlay()
        is ScreenState.Error -> {
          AlertDialog(
            onDismissRequest = { viewModel.resetScreenState() },
            title = { Text("Error") },
            text = { Text("Something went wrong") },
            confirmButton = {
              Button(onClick = { viewModel.resetScreenState() }) {
                Text("OK")
              }
            }
          )
        }

        else -> {}
      }
    }
  }
}

@Composable
fun Fields(
  items: List<Item>,
  units: Set<String>,
  quantity: String,
  selectedTab: SelectedTab,
  selectedItem: Item?,
  selectedUnit: String,
  modifier: Modifier = Modifier,
  selectItem: (Item) -> Unit,
  selectUnit: (String) -> Unit,
  setQuantity: (String) -> Unit
) {

  val itemFieldLabel = if (selectedTab == SelectedTab.ADD_PRODUCT) "Product" else "Half Product"
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Column {
      FieldLabel(text = itemFieldLabel, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
      ItemField(
        items = items,
        selectedItem = selectedItem,
        selectItem = selectItem,
        selectedTab = selectedTab
      )
    }

    Column {
      FieldLabel(text = "Quantity", modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
      QuantityField(
        quantity = quantity,
        modifier = Modifier.fillMaxWidth(),
        setQuantity = setQuantity
      )
    }
    Column {
      FieldLabel(text = "Unit", modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
      UnitField(units = units, selectedUnit = selectedUnit, selectUnit = selectUnit)
    }
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
    if (selectedTab == SelectedTab.ADD_PRODUCT) "Select a product" else "Select half product"

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

@Preview
@Composable
private fun PreviewFields() {
  FCCTheme {
    Box(Modifier.background(Color.Gray)) {
      Fields(
        items = listOf(),
        units = setOf("kg", "g"),
        quantity = "1",
        selectedTab = SelectedTab.ADD_PRODUCT,
        selectedItem = null,
        selectedUnit = "kg",
        selectItem = {},
        selectUnit = {},
        setQuantity = {}
      )
    }
  }
}

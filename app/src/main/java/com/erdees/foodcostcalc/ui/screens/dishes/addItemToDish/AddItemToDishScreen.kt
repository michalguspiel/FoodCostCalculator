package com.erdees.foodcostcalc.ui.screens.dishes.addItemToDish

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.ui.composables.AddItemFields
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton

enum class SelectedTab {
    ADD_PRODUCT,
    ADD_HALF_PRODUCT
}

//TODO: String resources.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemToDishScreen(navController: NavController, dishId: Long, dishName: String) {

    val viewModel: AddItemToDishViewModel = viewModel()

    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()
    val products by viewModel.products.collectAsState()
    val halfProducts by viewModel.halfProducts.collectAsState()
    val units by viewModel.units.collectAsState()
    val selectedUnit by viewModel.selectedUnit.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val addButtonEnabled by viewModel.addButtonEnabled.collectAsState()
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
            }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = "Back")
                }
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
                    AddItemFields(
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
                        FCCPrimaryButton(
                            onClick = { viewModel.addItem(dishId) },
                            text = "Add",
                            enabled = addButtonEnabled
                        )
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

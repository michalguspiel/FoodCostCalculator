package com.erdees.foodcostcalc.ui.screens.dishes.addItemToDish

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.fields.AddItemFields
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.navigation.Screen

enum class SelectedTab {
    ADD_PRODUCT,
    ADD_HALF_PRODUCT
}

@OptIn(ExperimentalMaterial3Api::class)
@Screen
@Composable
fun AddItemToDishScreen(
    navController: NavController,
    dishId: Long,
    dishName: String,
    viewModel: AddItemToDishViewModel = viewModel()
) {

    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()
    val products by viewModel.products.collectAsState()
    val halfProducts by viewModel.halfProducts.collectAsState()
    val units by viewModel.units.collectAsState()
    val selectedUnit by viewModel.selectedUnit.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val addButtonEnabled by viewModel.addButtonEnabled.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val showHalfProducts by viewModel.showHalfProducts.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(screenState) {
        when (screenState) {
            is ScreenState.Success<*> -> {
                val addedItemName = (screenState as ScreenState.Success<*>).data as? String
                val message = context.getString(R.string.item_added, addedItemName)
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
                viewModel.resetScreenState()
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = dishName)
            }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Sharp.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            })
        }) { paddingValues ->
        Box(Modifier.padding(paddingValues), contentAlignment = Alignment.Center) {
            Column(Modifier) {
                if (showHalfProducts == true) {
                    // Tabs
                    Row {
                        Tab(
                            modifier = Modifier.weight(1f),
                            selected = selectedTab == SelectedTab.ADD_PRODUCT,
                            onClick = { viewModel.selectTab(SelectedTab.ADD_PRODUCT) },
                            text = { Text(text = stringResource(id = R.string.add_product)) },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                        Tab(
                            modifier = Modifier.weight(1f),
                            selected = selectedTab == SelectedTab.ADD_HALF_PRODUCT,
                            onClick = { viewModel.selectTab(SelectedTab.ADD_HALF_PRODUCT) },
                            text = {
                                Text(text = stringResource(id = R.string.add_half_product))
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HorizontalDivider(Modifier.padding(bottom = 12.dp))
                }

                // Fields
                val items = if (selectedTab == SelectedTab.ADD_PRODUCT) products else halfProducts

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {

                    if (showHalfProducts == false) {
                        Text(
                            text = stringResource(id = R.string.add_product),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            textAlign = TextAlign.Start
                        )
                    }

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

                    Spacer(Modifier.weight(1f))

                    Column {
                        SnackbarHost(hostState = snackbarHostState)
                        ButtonRow(
                            modifier = Modifier.padding(bottom = 24.dp, top = 12.dp),
                            applyDefaultPadding = false,
                            primaryButton = {
                            FCCPrimaryButton(
                                onClick = { viewModel.addItem(dishId) },
                                text = stringResource(id = R.string.add),
                                enabled = addButtonEnabled
                            )
                        })
                    }

                }
            } // Column

            when (screenState) {
                is ScreenState.Loading<*> -> ScreenLoadingOverlay()
                is ScreenState.Error -> {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetScreenState() },
                        title = { Text(stringResource(id = R.string.error)) },
                        text = { Text(stringResource(R.string.something_went_wrong)) },
                        confirmButton = {
                            Button(onClick = { viewModel.resetScreenState() }) {
                                Text(stringResource(id = R.string.okay))
                            }
                        }
                    )
                }

                else -> {}
            }
        }
    }
}

package com.erdees.foodcostcalc.ui.screens.halfProducts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.databinding.CompHalfProductsBinding
import com.erdees.foodcostcalc.ui.composables.UnitField
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.screens.halfProducts.halfProductsFragment.HalfProductsFragment
import com.erdees.foodcostcalc.utils.Utils

@Composable
fun HalfProductsScreen(navController: NavController) {

    val viewModel: HalfProductsScreenViewModel = viewModel()

    var showDialog by remember {
        mutableStateOf(false)
    }
    Scaffold(
        modifier = Modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showDialog = true
                },
                shape = CircleShape,
            ) {
                Icon(Icons.Filled.Add, "Large floating action button")
            }
        }) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues), contentAlignment = Alignment.Center
        ) {
            AndroidViewBinding(
                factory = CompHalfProductsBinding::inflate,
                modifier = Modifier
            ) {
                this.halfProductsScreenFragmentContainerView.getFragment<HalfProductsFragment?>()
                    ?.let { fragment ->
                        fragment.navigateToAddItemsToHalfProductScreen = { halfProduct ->
                            navController.navigate(FCCScreen.AddItemToHalfProduct(halfProduct))
                        }
                        fragment.navigateToEditHalfProductScreen = { halfProduct ->
                            navController.navigate(FCCScreen.EditHalfProduct(halfProduct))
                        }
                    }
            }

            if (showDialog) {
                CreateHalfProductDialog(
                    units = Utils.getUnitsSet(
                        LocalContext.current.resources,
                        viewModel.preferences
                    ),
                    onSave = { name, unit ->
                        viewModel.addHalfProduct(name = name, unit = unit)
                        showDialog = false
                    },
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHalfProductDialog(
    units: Set<String>,
    modifier: Modifier = Modifier,
    onSave: (name: String, unit: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }

    var selectedUnit by remember {
        mutableStateOf(units.firstOrNull() ?: "")
    }

    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = { onDismiss() },
    ) {
        Column(
            Modifier
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp)
        ) {
            Text(text = "Create half product", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.size(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                Column {
                    FieldLabel(
                        text = "Name",
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { value ->
                            name = value
                        },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words,
                        )
                    )
                }

                Column {
                    FieldLabel(
                        text = "Unit",
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    UnitField(
                        units = units,
                        selectedUnit = selectedUnit,
                        selectUnit = { selectedUnit = it }
                    )
                }
            }

            Spacer(modifier = Modifier.size(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FCCTextButton(text = "Save") {
                    onSave(name, selectedUnit)
                }
            }
        }
    }
}

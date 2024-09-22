package com.erdees.foodcostcalc.ui.screens.products.editProduct

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    providedProduct: ProductDomain,
    navController: NavController
) {
    val viewModel: EditProductViewModel = viewModel()
    val screenState by viewModel.screenState.collectAsState()
    val product by viewModel.product.collectAsState()
    val editableName by viewModel.editableName.collectAsState()
    val saveButtonEnabled by viewModel.saveButtonEnabled.collectAsState()
    val saveNameButtonEnabled by viewModel.saveNameButtonEnabled.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeWith(providedProduct)
    }

    LaunchedEffect(screenState) {
        when (screenState) {
            is ScreenState.Success -> {
                Log.i(
                    "EditDishScreen",
                    "Success, popping backstack \n" +
                            "Previous backstack entry: ${navController.previousBackStackEntry?.destination?.route} \n"
                )
                viewModel.resetScreenState()
                navController.popBackStack()
            }

            else -> {}
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = product?.name ?: providedProduct.name,
                        modifier = Modifier.clickable {
                            viewModel.setInteractionEditName()
                        })
                },
                actions = {
                    IconButton(onClick = { viewModel.deleteProduct(providedProduct) }) {
                        Icon(imageVector = Icons.Sharp.Delete, contentDescription = "Remove dish")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp)
                    .padding(horizontal = 12.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "Values ${providedProduct.unit}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FCCTextField(
                        title = "Price",
                        value = product?.pricePerUnit.toString(),
                        onValueChange = viewModel::updatePrice,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )

                    FCCTextField(
                        title = "Tax %",
                        value = product?.tax.toString(),
                        onValueChange = viewModel::updateTax,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )

                    FCCTextField(
                        title = "% of waste",
                        value = product?.waste.toString(),
                        onValueChange = viewModel::updateWaste,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    FCCPrimaryButton(
                        enabled = saveButtonEnabled,
                        onClick = {
                            viewModel.save()
                        },
                        text = "Save"
                    )
                }
            }


            when (screenState) {
                is ScreenState.Interaction -> {
                    when ((screenState as ScreenState.Interaction).interaction) {
                        InteractionType.EditName -> {
                            ValueEditDialog(
                                title = "Edit name",
                                value = editableName,
                                saveButtonEnabled = saveNameButtonEnabled,
                                updateValue = viewModel::updateName,
                                onSave = viewModel::saveName,
                                onDismiss = viewModel::resetScreenState,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    capitalization = KeyboardCapitalization.Words
                                )
                            )
                        }

                        else -> {}
                    }
                }

                is ScreenState.Loading -> {
                    ScreenLoadingOverlay()
                }

                is ScreenState.Error -> {
                    ErrorDialog {
                        viewModel.resetScreenState()
                    }
                }

                else -> {}
            }
        }
    }
}
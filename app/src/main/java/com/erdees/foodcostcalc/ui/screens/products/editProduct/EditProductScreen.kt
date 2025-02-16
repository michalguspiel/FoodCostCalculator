package com.erdees.foodcostcalc.ui.screens.products.editProduct

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.navigation.Screen
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Screen
@Composable
fun EditProductScreen(
    productId: Long,
    navController: NavController,
    viewModel: EditProductViewModel = viewModel()
) {
    val screenState by viewModel.screenState.collectAsState()
    val product by viewModel.product.collectAsState()
    val editableName by viewModel.editableName.collectAsState()
    val saveButtonEnabled by viewModel.saveButtonEnabled.collectAsState()
    val saveNameButtonEnabled by viewModel.saveNameButtonEnabled.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeWith(productId)
    }

    LaunchedEffect(screenState) {
        when (screenState) {
            is ScreenState.Success -> {
                Timber.i(
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
                        text = product?.name ?: "",
                        modifier = Modifier.clickable {
                            viewModel.setInteractionEditName()
                        })
                },
                actions = {
                    IconButton(onClick = { viewModel.deleteProduct(productId) }) {
                        Icon(
                            imageVector = Icons.Sharp.Delete, contentDescription = stringResource(
                                id = R.string.content_description_remove_product
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = stringResource(
                                id = R.string.back
                            )
                        )
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                    ,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = stringResource(id = R.string.values_per_unit, product?.unit ?: ""),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FCCTextField(
                        title = stringResource(id = R.string.price),
                        value = product?.pricePerUnit.toString(),
                        onValueChange = viewModel::updatePrice,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )

                    FCCTextField(
                        title = stringResource(id = R.string.tax_percent),
                        value = product?.tax.toString(),
                        onValueChange = viewModel::updateTax,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )

                    FCCTextField(
                        title = stringResource(id = R.string.percent_of_waste),
                        value = product?.waste.toString(),
                        onValueChange = viewModel::updateWaste,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                }
                ButtonRow(
                    primaryButton = {
                    FCCPrimaryButton(
                        enabled = saveButtonEnabled,
                        onClick = {
                            viewModel.save()
                        },
                        text = stringResource(id = R.string.save)
                    )
                })
            }

            when (screenState) {
                is ScreenState.Interaction -> {
                    when ((screenState as ScreenState.Interaction).interaction) {
                        InteractionType.EditName -> {
                            ValueEditDialog(
                                title = stringResource(id = R.string.edit_name),
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
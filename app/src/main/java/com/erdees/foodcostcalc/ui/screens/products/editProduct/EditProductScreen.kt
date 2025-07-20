package com.erdees.foodcostcalc.ui.screens.products.editProduct

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.res.painterResource
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
import com.erdees.foodcostcalc.domain.model.product.EditableProductDomain
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCDeleteConfirmationDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCUnsavedChangesDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.navigation.Screen
import timber.log.Timber

@Screen
@Composable
fun EditProductScreen(
    navController: NavController,
    viewModel: EditProductViewModel = viewModel()
) {
    val screenState by viewModel.screenState.collectAsState()
    val product by viewModel.product.collectAsState()
    val editableName by viewModel.editableName.collectAsState()
    val saveButtonEnabled by viewModel.saveButtonEnabled.collectAsState()
    val saveNameButtonEnabled by viewModel.saveNameButtonEnabled.collectAsState()
    val showTaxPercent by viewModel.showTaxPercent.collectAsState()

    BackHandler {
        viewModel.handleBackNavigation { navController.popBackStack() }
    }

    LaunchedEffect(screenState) {
        when (screenState) {
            is ScreenState.Success<*> -> {
                Timber.i("Success, popping backstack")
                viewModel.resetScreenState()
                navController.popBackStack()
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            EditProductTopBar(
                productName = product?.name ?: "",
                onNameClick = { viewModel.setInteractionEditName() },
                onDeleteClick = { viewModel.onDeleteProductClick() },
                onBackClick = { viewModel.handleBackNavigation { navController.popBackStack() } }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            EditProductContent(
                product = product,
                showTaxPercent = showTaxPercent,
                saveButtonEnabled = saveButtonEnabled,
                onPriceChange = viewModel::updatePrice,
                onTaxChange = viewModel::updateTax,
                onWasteChange = viewModel::updateWaste,
                onSaveClick = viewModel::save
            )

            HandleScreenState(
                screenState = screenState,
                editableName = editableName,
                saveNameButtonEnabled = saveNameButtonEnabled,
                onNameChange = viewModel::updateName,
                onSaveName = viewModel::saveName,
                onDismissDialog = viewModel::resetScreenState,
                onDeleteConfirm = viewModel::deleteProduct,
                onDiscardChanges = {
                    viewModel.discardChanges { navController.popBackStack() }
                },
                onSaveChanges = viewModel::saveAndNavigate
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProductTopBar(
    productName: String,
    onNameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = productName,
                modifier = Modifier.clickable { onNameClick() }
            )
        },
        actions = {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(R.drawable.delete_24dp),
                    contentDescription = stringResource(id = R.string.content_description_remove_product)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Sharp.ArrowBack,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        }
    )
}

@Composable
private fun EditProductContent(
    product: EditableProductDomain?,
    showTaxPercent: Boolean,
    saveButtonEnabled: Boolean,
    onPriceChange: (String) -> Unit,
    onTaxChange: (String) -> Unit,
    onWasteChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
    ) {
        ProductFormFields(
            product = product,
            showTaxPercent = showTaxPercent,
            onPriceChange = onPriceChange,
            onTaxChange = onTaxChange,
            onWasteChange = onWasteChange
        )

        FCCPrimaryButton(
            enabled = saveButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 24.dp),
            text = stringResource(R.string.save),
            onClick = {
                onSaveClick()
            })
    }
}

@Composable
private fun ProductFormFields(
    product: EditableProductDomain?,
    showTaxPercent: Boolean,
    onPriceChange: (String) -> Unit,
    onTaxChange: (String) -> Unit,
    onWasteChange: (String) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .animateContentSize(),
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
            onValueChange = onPriceChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )

        if (showTaxPercent || product?.tax?.toDoubleOrNull() != 0.0) {
            FCCTextField(
                title = stringResource(id = R.string.tax_percent),
                value = product?.tax.toString(),
                onValueChange = onTaxChange,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
        }

        FCCTextField(
            title = stringResource(id = R.string.percent_of_waste),
            value = product?.waste.toString(),
            onValueChange = onWasteChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun HandleScreenState(
    screenState: ScreenState,
    editableName: String,
    saveNameButtonEnabled: Boolean,
    onNameChange: (String) -> Unit,
    onSaveName: () -> Unit,
    onDismissDialog: () -> Unit,
    onDeleteConfirm: (Long) -> Unit,
    onDiscardChanges: () -> Unit,
    onSaveChanges: () -> Unit
) {
    when (screenState) {
        is ScreenState.Interaction -> {
            when (screenState.interaction) {
                InteractionType.EditName -> {
                    ValueEditDialog(
                        title = stringResource(id = R.string.edit_name),
                        value = editableName,
                        saveButtonEnabled = saveNameButtonEnabled,
                        updateValue = onNameChange,
                        onSave = onSaveName,
                        onDismiss = onDismissDialog,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words
                        )
                    )
                }

                is InteractionType.DeleteConfirmation -> {
                    val deleteConfirmation = screenState.interaction
                    FCCDeleteConfirmationDialog(
                        itemName = deleteConfirmation.itemName,
                        onDismiss = onDismissDialog,
                        onConfirmDelete = {
                            onDeleteConfirm(deleteConfirmation.itemId)
                        }
                    )
                }

                InteractionType.UnsavedChangesConfirmation -> {
                    FCCUnsavedChangesDialog(
                        onDismiss = onDismissDialog,
                        onDiscard = onDiscardChanges,
                        onSave = onSaveChanges
                    )
                }

                else -> {}
            }
        }

        is ScreenState.Loading<*> -> {
            ScreenLoadingOverlay()
        }

        is ScreenState.Error -> {
            ErrorDialog {
                onDismissDialog()
            }
        }

        else -> {}
    }
}
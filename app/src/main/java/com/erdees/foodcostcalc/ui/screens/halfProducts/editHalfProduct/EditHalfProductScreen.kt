package com.erdees.foodcostcalc.ui.screens.halfProducts.editHalfProduct

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.ui.composables.DetailItem
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.UsedItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHalfProductScreen(navController: NavController, providedHalfProduct: HalfProductDomain) {

    val viewModel: EditHalfProductViewModel = viewModel()
    val screenState by viewModel.screenState.collectAsState()
    val usedItems by viewModel.usedItems.collectAsState()
    val halfProduct by viewModel.halfProduct.collectAsState()
    val editableQuantity by viewModel.editableQuantity.collectAsState()
    val editableName by viewModel.editableName.collectAsState()

    LaunchedEffect(providedHalfProduct) {
        viewModel.initializeWith(providedHalfProduct)
    }

    LaunchedEffect(screenState) {
        when (screenState) {
            is ScreenState.Success -> {
                Log.i(
                    "EditHalfProductScreen",
                    "Success, popping backstack \n" +
                            "Previous backstack entry: ${navController.previousBackStackEntry?.destination?.route} \n"
                )
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
                        text = halfProduct?.name ?: providedHalfProduct.name,
                        modifier = Modifier.clickable {
                            viewModel.setInteraction(InteractionType.EditName)
                        })
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteHalfProduct(providedHalfProduct.id)
                    }) {
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
            modifier = Modifier.padding(paddingValues),
            contentAlignment = Center
        ) {
            Column {
                LazyColumn(Modifier.weight(fill = true, weight = 1f)) {
                    items(usedItems, key = { item -> item.id }) { item ->
                        UsedItem(
                            usedItem = item,
                            onRemove = viewModel::removeItem,
                            onEdit = {
                                viewModel.setInteraction(InteractionType.EditItem(it))
                            }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )
                    }
                }

                HalfProductDetails(
                    halfProductDomain = halfProduct ?: providedHalfProduct,
                    modifier = Modifier
                )

                Spacer(Modifier.size(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, end = 16.dp)
                ) {
                    FCCPrimaryButton(text = "Save") {
                        viewModel.saveHalfProduct()
                    }
                }
            }

            when (screenState) {
                ScreenState.Loading -> {
                    ScreenLoadingOverlay()
                }

                is ScreenState.Error -> {
                    ErrorDialog {
                        viewModel.resetScreenState()
                    }
                }

                ScreenState.Success -> {
                    // TODO()
                }

                is ScreenState.Interaction -> {
                    when ((screenState as ScreenState.Interaction).interaction) {
                        is InteractionType.EditItem -> {
                            ValueEditDialog(
                                title = "Edit quantity",
                                value = editableQuantity,
                                modifier = Modifier,
                                updateValue = viewModel::setEditableQuantity,
                                onSave = viewModel::updateProductQuantity,
                                onDismiss = viewModel::resetScreenState,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        }

                        InteractionType.EditName -> {
                            ValueEditDialog(
                                title = "Edit name",
                                value = editableName,
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

                else -> {}
            }

        }
    }
}

@Composable
fun HalfProductDetails(
    halfProductDomain: HalfProductDomain,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(modifier) {
        DetailItem(
            label = "Price per recipe",
            value = halfProductDomain.formattedPricePerRecipe(context),
            modifier = Modifier.weight(1f)
        )
        DetailItem(
            label = "Price ${halfProductDomain.halfProductUnit}",
            value = halfProductDomain.formattedPricePerUnit(context),
            modifier = Modifier.weight(1f)
        )
    }
}
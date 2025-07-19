package com.erdees.foodcostcalc.ui.screens.halfProducts.editHalfProduct

import android.icu.util.Currency
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.ext.showUndoDeleteSnackbar
import com.erdees.foodcostcalc.ui.composables.DetailItem
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCDeleteConfirmationDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCUnsavedChangesDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.UsedItem
import timber.log.Timber

data class EditHalfProductScreenState(
    val screenState: ScreenState,
    val usedItems: List<UsedItem>,
    val halfProduct: HalfProductDomain?,
    val editableQuantity: String,
    val editableName: String,
    val currency: Currency?
)

data class EditHalfProductScreenCallbacks(
    val onBack: () -> Unit,
    val onDeleteHalfProductClick: () -> Unit,
    val setInteraction: (InteractionType) -> Unit,
    val removeItem: (UsedItem) -> Unit,
    val saveHalfProduct: () -> Unit,
    val resetScreenState: () -> Unit,
    val setEditableQuantity: (String) -> Unit,
    val updateProductQuantity: () -> Unit,
    val updateName: (String) -> Unit,
    val saveName: () -> Unit,
    val confirmDelete: (Long) -> Unit,
    val discardChanges: (() -> Unit) -> Unit,
    val saveAndNavigate: () -> Unit
)

@Screen
@Composable
fun EditHalfProductScreen(
    navController: NavController,
    viewModel: EditHalfProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val screenState by viewModel.screenState.collectAsState()
    val usedItems by viewModel.usedItems.collectAsState()
    val halfProduct by viewModel.halfProduct.collectAsState()
    val editableQuantity by viewModel.editableQuantity.collectAsState()
    val editableName by viewModel.editableName.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val lastRemovedItem by viewModel.lastRemovedItem.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        viewModel.handleBackNavigation { navController.popBackStack() }
    }

    LaunchedEffect(lastRemovedItem) {
        val removedItem = lastRemovedItem ?: return@LaunchedEffect
        snackbarHostState.showUndoDeleteSnackbar(
            message = context.getString(R.string.removed_item, removedItem.item.name),
            actionLabel = context.getString(R.string.undo),
            actionPerformed = { viewModel.undoRemoveItem() },
            ignored = { viewModel.clearLastRemovedItem() }
        )
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

    val state = EditHalfProductScreenState(
        screenState = screenState,
        usedItems = usedItems,
        halfProduct = halfProduct,
        editableQuantity = editableQuantity,
        editableName = editableName,
        currency = currency
    )

    val callbacks = EditHalfProductScreenCallbacks(
        onBack = { viewModel.handleBackNavigation { navController.popBackStack() } },
        onDeleteHalfProductClick = viewModel::onDeleteHalfProductClick,
        setInteraction = viewModel::setInteraction,
        removeItem = viewModel::removeItem,
        saveHalfProduct = viewModel::saveHalfProduct,
        resetScreenState = viewModel::resetScreenState,
        setEditableQuantity = viewModel::setEditableQuantity,
        updateProductQuantity = viewModel::updateProductQuantity,
        updateName = viewModel::updateName,
        saveName = viewModel::saveName,
        confirmDelete = viewModel::confirmDelete,
        discardChanges = viewModel::discardChanges,
        saveAndNavigate = viewModel::saveAndNavigate
    )

    EditHalfProductScreenContent(
        state = state,
        callbacks = callbacks,
        navController,
        snackbarHostState
    )
}

@Composable
private fun EditHalfProductScreenContent(
    state: EditHalfProductScreenState,
    callbacks: EditHalfProductScreenCallbacks,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            EditHalfProductTopBar(
                halfProduct = state.halfProduct,
                onBackClick = callbacks.onBack,
                onDeleteClick = callbacks.onDeleteHalfProductClick,
                onNameClick = { callbacks.setInteraction(InteractionType.EditName) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues),
            contentAlignment = Center
        ) {
            Column {
                LazyColumn(Modifier.weight(fill = true, weight = 1f)) {
                    items(state.usedItems, key = { item -> item.id }) { item ->
                        UsedItem(
                            usedItem = item,
                            onRemove = callbacks.removeItem,
                            onEdit = {
                                callbacks.setInteraction(InteractionType.EditItem(it))
                            }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )
                    }
                }

                state.halfProduct?.let {
                    HalfProductDetails(
                        halfProductDomain = it,
                        currency = state.currency,
                        modifier = Modifier
                    )
                }

                ButtonRow(
                    modifier = Modifier.padding(end = 12.dp),
                    primaryButton = {
                        FCCPrimaryButton(text = stringResource(id = R.string.save)) {
                            callbacks.saveHalfProduct()
                        }
                    })
            }

            HandleScreenState(state, callbacks, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditHalfProductTopBar(
    halfProduct: HalfProductDomain?,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onNameClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = halfProduct?.name ?: "",
                modifier = Modifier.clickable { onNameClick() })
        },
        actions = {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(R.drawable.delete_24dp),
                    contentDescription = stringResource(id = R.string.content_description_remove_half_product)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = stringResource(
                        id = R.string.back
                    )
                )
            }
        }
    )
}

@Composable
private fun HandleScreenState(
    state: EditHalfProductScreenState,
    callbacks: EditHalfProductScreenCallbacks,
    navController: NavController
) {
    when (state.screenState) {
        is ScreenState.Loading<*> -> {
            ScreenLoadingOverlay()
        }

        is ScreenState.Error -> {
            ErrorDialog {
                callbacks.resetScreenState()
            }
        }

        is ScreenState.Interaction -> {
            when (val interaction = state.screenState.interaction) {
                is InteractionType.EditItem -> {
                    ValueEditDialog(
                        title = stringResource(id = R.string.edit_quantity),
                        value = state.editableQuantity,
                        modifier = Modifier,
                        updateValue = callbacks.setEditableQuantity,
                        onSave = callbacks.updateProductQuantity,
                        onDismiss = callbacks.resetScreenState,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }

                InteractionType.EditName -> {
                    ValueEditDialog(
                        title = stringResource(id = R.string.edit_name),
                        value = state.editableName,
                        updateValue = callbacks.updateName,
                        onSave = callbacks.saveName,
                        onDismiss = callbacks.resetScreenState,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words
                        )
                    )
                }

                is InteractionType.DeleteConfirmation -> {
                    FCCDeleteConfirmationDialog(
                        itemName = interaction.itemName,
                        onDismiss = callbacks.resetScreenState,
                        onConfirmDelete = {
                            callbacks.confirmDelete(interaction.itemId)
                        }
                    )
                }

                InteractionType.UnsavedChangesConfirmation -> {
                    FCCUnsavedChangesDialog(
                        onDismiss = callbacks.resetScreenState,
                        onDiscard = { callbacks.discardChanges { navController.popBackStack() } },
                        onSave = callbacks.saveAndNavigate
                    )
                }

                else -> {}
            }
        }

        else -> {}
    }
}

@Composable
fun HalfProductDetails(
    halfProductDomain: HalfProductDomain,
    currency: Currency?,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        DetailItem(
            label = stringResource(id = R.string.price_per_recipe),
            value = halfProductDomain.formattedSingleRecipePrice(currency),
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f)
        )
        DetailItem(
            label = stringResource(id = R.string.price_per_unit, halfProductDomain.halfProductUnit),
            value = halfProductDomain.formattedPricePresentedRecipe(
                halfProductDomain.totalQuantity,
                1.0,
                currency,
            ),
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f)
        )
    }
}

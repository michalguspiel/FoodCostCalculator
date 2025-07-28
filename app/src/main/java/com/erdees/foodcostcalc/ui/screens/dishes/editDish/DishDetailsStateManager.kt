package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.JustRemovedItem
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.dish.DishActionResult
import com.erdees.foodcostcalc.domain.model.dish.DishDetailsActionResultType
import com.erdees.foodcostcalc.ext.showUndoDeleteSnackbar
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.DishDetailsUtil.getCopyDishPrefilledName
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormViewModel

@Composable
fun DishDetailsStateManager(
    uiState: DishDetailsUiState,
    context: Context,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    existingFormViewModel: ExistingComponentFormViewModel,
    viewModel: DishDetailsViewModel,
) {
    HandleComponentSelectionChange(
        componentSelection = uiState.componentSelection,
        existingFormViewModel = existingFormViewModel,
        context = context
    )

    HandleItemRemovalWithUndo(
        lastRemovedItem = uiState.lastRemovedItem,
        snackbarHostState = snackbarHostState,
        context = context,
        viewModel = viewModel
    )

    HandleScreenStateNavigation(
        screenState = uiState.screenState,
        navController = navController,
        viewModel = viewModel,
        context = context
    )
}

@Composable
private fun HandleComponentSelectionChange(
    componentSelection: ComponentSelection?,
    existingFormViewModel: ExistingComponentFormViewModel,
    context: Context
) {
    LaunchedEffect(componentSelection) {
        componentSelection?.let {
            if (it is ComponentSelection.ExistingComponent) {
                existingFormViewModel.setItemContext(it.item, context.resources)
            }
        }
    }
}

@Composable
private fun HandleItemRemovalWithUndo(
    lastRemovedItem: JustRemovedItem?,
    snackbarHostState: SnackbarHostState,
    context: Context,
    viewModel: DishDetailsViewModel
) {
    LaunchedEffect(lastRemovedItem) {
        val removedItem = lastRemovedItem?.item ?: return@LaunchedEffect
        snackbarHostState.showUndoDeleteSnackbar(
            message = context.getString(R.string.removed_item, removedItem.item.name),
            actionLabel = context.getString(R.string.undo),
            actionPerformed = { viewModel.undoRemoveItem() },
            ignored = { viewModel.clearLastRemovedItem() }
        )
    }
}

@Composable
private fun HandleScreenStateNavigation(
    screenState: ScreenState,
    navController: NavController,
    viewModel: DishDetailsViewModel,
    context: Context
) {
    LaunchedEffect(screenState) {
        val state = screenState as? ScreenState.Success<*> ?: return@LaunchedEffect
        val data = state.data as? DishActionResult ?: return@LaunchedEffect

        viewModel.resetScreenState()
        when (data.type) {
            DishDetailsActionResultType.COPIED -> {
                navController.navigate(FCCScreen.DishDetails(data.dishId, true)) {
                    popUpTo(navController.currentBackStackEntry?.destination?.route ?: "") {
                        inclusive = true
                    }
                }
            }

            DishDetailsActionResultType.UPDATED_NAVIGATE, DishDetailsActionResultType.DELETED -> {
                navController.popBackStack()
            }

            DishDetailsActionResultType.UPDATED_STAY -> {
                viewModel.handleCopyDish { getCopyDishPrefilledName(it, context) }
            }
        }
    }
}

package com.erdees.foodcostcalc.ui.screens.dishes.dishdetails

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.JustRemovedItem
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.dish.DishActionResult
import com.erdees.foodcostcalc.domain.model.dish.DishDetailsActionResultType
import com.erdees.foodcostcalc.ext.showUndoDeleteSnackbar
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.screens.dishes.dishdetails.DishDetailsUtil.getCopyDishPrefilledName
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection

data class DishDetailsStateManagerActions(
    val undoRemoveItem: () -> Unit,
    val clearLastRemovedItem: () -> Unit,
    val resetScreenState: () -> Unit,
    val handleCopyDish: ((String) -> String) -> Unit,
    val setItemContext: (Item) -> Unit
)

@Composable
fun DishDetailsStateManager(
    uiState: DishDetailsUiState,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    actions: DishDetailsStateManagerActions,
) {
    HandleComponentSelectionChange(
        componentSelection = uiState.componentSelection,
        setItemContext = actions.setItemContext,
    )

    HandleItemRemovalWithUndo(
        lastRemovedItem = uiState.lastRemovedItem,
        snackbarHostState = snackbarHostState,
        undoRemoveItem = actions.undoRemoveItem,
        clearLastRemovedItem = actions.clearLastRemovedItem
    )

    HandleScreenStateNavigation(
        screenState = uiState.screenState,
        navController = navController,
        resetScreenState = actions.resetScreenState,
        handleCopyDish = actions.handleCopyDish,
    )
}

@Composable
private fun HandleComponentSelectionChange(
    componentSelection: ComponentSelection?,
    setItemContext: (Item) -> Unit,
) {
    val currentSetItemContext = rememberUpdatedState(setItemContext)

    LaunchedEffect(componentSelection) {
        componentSelection?.let {
            if (it is ComponentSelection.ExistingComponent) {
                currentSetItemContext.value(it.item)
            }
        }
    }
}

@Composable
private fun HandleItemRemovalWithUndo(
    lastRemovedItem: JustRemovedItem?,
    snackbarHostState: SnackbarHostState,
    undoRemoveItem: () -> Unit,
    clearLastRemovedItem: () -> Unit
) {
    val context = LocalContext.current
    val currentUndoRemoveItem = rememberUpdatedState(undoRemoveItem)
    val currentClearLastRemovedItem = rememberUpdatedState(clearLastRemovedItem)

    LaunchedEffect(lastRemovedItem) {
        val removedItem = lastRemovedItem?.item ?: return@LaunchedEffect
        snackbarHostState.showUndoDeleteSnackbar(
            message = context.getString(R.string.removed_item, removedItem.item.name),
            actionLabel = context.getString(R.string.undo),
            actionPerformed = { currentUndoRemoveItem.value() },
            ignored = { currentClearLastRemovedItem.value() }
        )
    }
}

@Composable
private fun HandleScreenStateNavigation(
    screenState: ScreenState,
    navController: NavController,
    resetScreenState: () -> Unit,
    handleCopyDish: ((String) -> String) -> Unit,
) {
    val context = LocalContext.current
    val currentResetScreenState = rememberUpdatedState(resetScreenState)
    val currentHandleCopyDish = rememberUpdatedState(handleCopyDish)

    LaunchedEffect(screenState) {
        val state = screenState as? ScreenState.Success<*> ?: return@LaunchedEffect
        val data = state.data as? DishActionResult ?: return@LaunchedEffect

        currentResetScreenState.value()
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
                currentHandleCopyDish.value { getCopyDishPrefilledName(it, context) }
            }
        }
    }
}

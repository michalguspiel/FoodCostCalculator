package com.erdees.foodcostcalc.ui.screens.dishes.dishdetails

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentLookupForm
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentLookupFormActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentLookupFormUiState
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentForm
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormUiState
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormUiState
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailsModalSheet(
    sheetState: SheetState,
    componentSelection: ComponentSelection?,
    dishName: String,
    dishDetailsActions: DishDetailsScreenActions,
    existingComponentFormUiState: ExistingComponentFormUiState,
    existingComponentFormActions: ExistingComponentFormActions,
    newProductFormUiState: NewProductFormUiState,
    newProductFormActions: NewProductFormActions,
    componentLookupFormUiState: ComponentLookupFormUiState,
    componentLookupFormActions: ComponentLookupFormActions
) {
    ModalBottomSheet(
        onDismissRequest = { dishDetailsActions.dishActions.resetScreenState() },
        sheetState = sheetState
    ) {
        AnimatedContent(
            targetState = componentSelection,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
            },
            label = "modal_content_transition"
        ) { selection ->
            when (val safeSelection = selection) {
                is ComponentSelection.ExistingComponent -> {
                    with(existingComponentFormUiState) {
                        ExistingComponentForm(
                            formData = formData,
                            dishName = dishName,
                            isAddButtonEnabled = isAddButtonEnabled,
                            compatibleUnitsForDish = compatibleUnitsForDish,
                            unitForDishDropdownExpanded = unitForDishDropdownExpanded,
                            selectedComponent = safeSelection.item,
                            onUnitForDishDropdownExpandedChange = existingComponentFormActions.onUnitForDishDropdownExpandedChange,
                            onCancel = existingComponentFormActions.onCancel,
                            onAddComponent = { data ->
                                existingComponentFormActions.onAddComponent(data)
                                componentLookupFormActions.onReset()
                            },
                            onFormDataChange = existingComponentFormActions.onFormDataChange
                        )
                    }
                }

                is ComponentSelection.NewComponent -> {
                    NewProductForm(
                        state = newProductFormUiState.copy(
                            productName = safeSelection.name,
                            dishName = dishName
                        ),
                        actions = NewProductFormActions(
                            onFormDataUpdate = newProductFormActions.onFormDataUpdate,
                            onProductCreationDropdownExpandedChange = newProductFormActions.onProductCreationDropdownExpandedChange,
                            onProductAdditionDropdownExpandedChange = newProductFormActions.onProductAdditionDropdownExpandedChange,
                            onSaveProduct = { data ->
                                newProductFormActions.onSaveProduct(data)
                                componentLookupFormActions.onReset()
                            },
                            onNextStep = newProductFormActions.onNextStep,
                            onPreviousStep = newProductFormActions.onPreviousStep,
                            onCancel = newProductFormActions.onCancel
                        )
                    )
                }

                null -> {
                    ComponentLookupForm(
                        uiState = componentLookupFormUiState,
                        actions = componentLookupFormActions
                    )
                }
            }
        }
    }
}

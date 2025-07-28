package com.erdees.foodcostcalc.ui.screens.dishes.dishdetails

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentLookupForm
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentForm
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailsModalSheet(
    sheetState: SheetState,
    componentSelection: ComponentSelection?,
    dishName: String,
    dishDetailsActions: DishDetailsScreenActions,
    existingComponentFormUiState: ExistingComponentFormUiState,
    existingComponentFormActions: ExistingComponentFormActions,
) {
    ModalBottomSheet(
        onDismissRequest = { dishDetailsActions.dishActions.resetScreenState() },
        sheetState = sheetState
    ) {
        when (componentSelection) {
            is ComponentSelection.ExistingComponent -> {
                with(existingComponentFormUiState) {
                    ExistingComponentForm(
                        formData = formData,
                        dishName = dishName,
                        isAddButtonEnabled = isAddButtonEnabled,
                        compatibleUnitsForDish = compatibleUnitsForDish,
                        unitForDishDropdownExpanded = unitForDishDropdownExpanded,
                        selectedComponent = componentSelection.item,
                        onUnitForDishDropdownExpandedChange = existingComponentFormActions.onUnitForDishDropdownExpandedChange,
                        onCancel = existingComponentFormActions.onCancel,
                        onAddComponent = existingComponentFormActions.onAddComponent,
                        onFormDataChange = existingComponentFormActions.onFormDataChange
                    )
                }
            }

            is ComponentSelection.NewComponent -> {
                Text("TODO NEW PRODUCT FORM")
            }

            null -> {
                ComponentLookupForm {
                    dishDetailsActions.itemActions.setComponentSelection(it)
                }
            }
        }
    }
}

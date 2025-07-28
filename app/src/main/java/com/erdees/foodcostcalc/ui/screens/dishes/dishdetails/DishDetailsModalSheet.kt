package com.erdees.foodcostcalc.ui.screens.dishes.dishdetails

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentLookupForm
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentForm
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingComponentFormUiState
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductForm
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormActions
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormUiState

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
                NewProductForm(
                    state = newProductFormUiState.copy(
                        productName = componentSelection.name,
                        dishName = dishName
                    ),
                    onProductCreationDropdownExpandedChange = newProductFormActions.onProductCreationDropdownExpandedChange,
                    onProductAdditionDropdownExpandedChange = newProductFormActions.onProductAdditionDropdownExpandedChange,
                    onFormDataUpdate = newProductFormActions.onFormDataUpdate,
                    onSaveProduct = newProductFormActions.onSaveProduct
                )
            }

            null -> {
                ComponentLookupForm {
                    dishDetailsActions.itemActions.setComponentSelection(it)
                }
            }
        }
    }
}

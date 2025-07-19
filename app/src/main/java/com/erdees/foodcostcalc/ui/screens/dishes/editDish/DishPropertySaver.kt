package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import com.erdees.foodcostcalc.domain.model.ScreenState
import timber.log.Timber

class DishPropertySaver {

    enum class PropertyType {
        TAX, MARGIN, NAME, TOTAL_PRICE
    }

    /**
     * Processes a save property request and returns the appropriate updates through callbacks.
     *
     * @param propertyType The type of property being saved
     * @param uiState The current UI state of the dish details screen
     * @param updateUiState A callback to update the UI state with new values
     */
    fun saveProperty(
        propertyType: PropertyType,
        uiState: DishDetailsUiState,
        updateUiState: (DishDetailsUiState) -> Unit
    ) {
        val currentDish = uiState.dish

        if (currentDish == null) {
            Timber.e("Cannot save property - current dish is null")
            updateUiState(uiState.copy(screenState = ScreenState.Idle))
            return
        }

        when (propertyType) {
            PropertyType.TAX -> {
                val value = uiState.editableFields.tax.toDoubleOrNull()
                if (value == null) {
                    updateUiState(uiState.copy(screenState = ScreenState.Idle))
                    return
                }
                updateUiState(
                    uiState.copy(
                        dish = currentDish.copy(taxPercent = value),
                        screenState = ScreenState.Idle
                    )
                )
            }

            PropertyType.MARGIN -> {
                val value = uiState.editableFields.margin.toDoubleOrNull()
                if (value == null) {
                    updateUiState(uiState.copy(screenState = ScreenState.Idle))
                    return
                }
                updateUiState(
                    uiState.copy(
                        dish = currentDish.copy(marginPercent = value),
                        screenState = ScreenState.Idle
                    )
                )
            }

            PropertyType.NAME -> {
                val value = uiState.editableFields.name
                updateUiState(
                    uiState.copy(
                        dish = currentDish.copy(name = value),
                        screenState = ScreenState.Idle
                    )
                )
            }

            PropertyType.TOTAL_PRICE -> {
                val value = uiState.editableFields.totalPrice.toDoubleOrNull()
                if (value == null) {
                    Timber.e("Invalid total price format: ${uiState.editableFields.totalPrice}")
                    updateUiState(
                        uiState.copy(
                            screenState = ScreenState.Error(Error("Invalid total price format."))
                        )
                    )
                    return
                }

                updateUiState(
                    uiState.copy(
                        dish = currentDish.withUpdatedTotalPrice(value),
                        screenState = ScreenState.Idle
                    )
                )
            }
        }
    }
}

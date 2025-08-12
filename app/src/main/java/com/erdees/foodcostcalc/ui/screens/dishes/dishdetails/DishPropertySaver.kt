package com.erdees.foodcostcalc.ui.screens.dishes.dishdetails

import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.domain.model.ScreenState
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class DishPropertySaver {

    private val analyticsRepository: AnalyticsRepository by inject(AnalyticsRepository::class.java)

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
        updateUiState: (DishDetailsUiState) -> Unit,
    ) {
        if (uiState.dish == null) {
            Timber.e("Cannot save property - current dish is null")
            updateUiState(uiState.copy(screenState = ScreenState.Idle))
            return
        }

        val updatedState = when (propertyType) {
            PropertyType.TAX -> {
                val value = uiState.editableFields.tax.toDoubleOrNull()
                if (value == null) {
                    uiState.copy(screenState = ScreenState.Idle)
                } else {
                    uiState.copy(
                        dish = uiState.dish.copy(taxPercent = value),
                        screenState = ScreenState.Idle
                    )
                }
            }

            PropertyType.MARGIN -> {
                val value = uiState.editableFields.margin.toDoubleOrNull()
                if (value == null) {
                    uiState.copy(screenState = ScreenState.Idle)
                } else {
                    uiState.copy(
                        dish = uiState.dish.copy(marginPercent = value),
                        screenState = ScreenState.Idle
                    )
                }
            }

            PropertyType.NAME -> {
                val value = uiState.editableFields.name
                uiState.copy(
                    dish = uiState.dish.copy(name = value),
                    screenState = ScreenState.Idle
                )
            }

            PropertyType.TOTAL_PRICE -> {
                val value = uiState.editableFields.totalPrice.toDoubleOrNull()
                if (value == null) {
                    Timber.e("Invalid total price format: ${uiState.editableFields.totalPrice}")
                    uiState.copy(
                        screenState = ScreenState.Error(Error("Invalid total price format."))
                    )
                } else {
                    uiState.copy(
                        dish = uiState.dish.withUpdatedTotalPrice(value, analyticsRepository),
                        screenState = ScreenState.Idle
                    )
                }
            }
        }
        updateUiState(updatedState)
    }
}

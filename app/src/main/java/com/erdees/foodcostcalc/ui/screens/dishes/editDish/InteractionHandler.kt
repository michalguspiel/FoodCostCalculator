package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.utils.Utils

/**
 * Handles setting up interactions in the dish details screen.
 * This class is designed to be stateless - it receives all necessary data as parameters
 * and returns updated values through callbacks.
 */
class InteractionHandler {

    /**
     * Processes an interaction and returns the appropriate updates through callbacks.
     *
     * @param interaction The interaction type to process
     * @param uiState The current UI state of the dish details screen
     * @param updateUiState A callback to update the UI state with new values
     */
    fun handleInteraction(
        interaction: InteractionType,
        uiState: DishDetailsUiState,
        updateUiState: (DishDetailsUiState) -> Unit,
    ) {
        when (interaction) {
            is InteractionType.EditItem -> {
                updateUiState(
                    uiState.copy(
                        currentlyEditedItem = interaction.usedItem,
                        editableFields = uiState.editableFields.copy(
                            quantity = interaction.usedItem.quantity.toString()
                        ),
                        screenState = ScreenState.Interaction(interaction)
                    )
                )
            }

            is InteractionType.EditTax -> {
                updateUiState(
                    uiState.copy(
                        screenState = ScreenState.Interaction(interaction),
                        editableFields = uiState.editableFields.copy(
                            tax = uiState.dish?.taxPercent?.toString() ?: "",
                        )
                    )
                )
            }

            is InteractionType.EditMargin -> {
                updateUiState(
                    uiState.copy(
                        screenState = ScreenState.Interaction(interaction),
                        editableFields = uiState.editableFields.copy(
                            margin = uiState.dish?.marginPercent?.toString() ?: ""
                        )
                    )
                )
            }

            is InteractionType.EditName -> {
                updateUiState(
                    uiState.copy(
                        screenState = ScreenState.Interaction(interaction),
                        editableFields = uiState.editableFields.copy(
                            name = uiState.dish?.name ?: ""
                        )
                    )
                )
            }

            is InteractionType.EditTotalPrice -> {
                val currentDish = uiState.dish
                if (currentDish?.foodCost == 0.00) {
                    return
                }
                val price = Utils.formatPriceWithoutSymbol(
                    currentDish?.totalPrice, uiState.currency?.currencyCode
                )
                updateUiState(
                    uiState.copy(
                        screenState = ScreenState.Interaction(interaction),
                        editableFields = uiState.editableFields.copy(
                            totalPrice = price
                        )
                    )
                )
            }

            is InteractionType.CopyDish ->
                updateUiState(
                    uiState.copy(
                        screenState = ScreenState.Interaction(interaction),
                        editableFields = uiState.editableFields.copy(
                            copiedDishName = interaction.prefilledName
                        )
                    )
                )

            else -> {}
        }
    }
}

package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import com.erdees.foodcostcalc.domain.model.JustRemovedItem
import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import timber.log.Timber

class DishItemOperationHandler(
    private val updateUiState: (DishDetailsUiState) -> Unit
) {

    /**
     * Updates the quantity of an item in a dish.
     *
     * @param uiState The current UI state of the dish details screen
     * @return Boolean indicating if the operation was successful
     */
    fun updateItemQuantity(uiState: DishDetailsUiState) {
        val quantity = uiState.editableFields.quantity.toDoubleOrNull()
        val currentlyEditedItem = uiState.currentlyEditedItem
        val currentDish = uiState.dish

        if (quantity == null || currentlyEditedItem == null || currentDish == null) {
            return
        }

        when (currentlyEditedItem) {
            is UsedProductDomain -> {
                val index = currentDish.products.indexOf(currentlyEditedItem)
                if (index != -1) {
                    val updatedItem = currentlyEditedItem.copy(quantity = quantity)
                    val updatedProducts = currentDish.products.toMutableList()
                        .apply { set(index, updatedItem) }
                    updateUiState(
                        uiState.copy(
                            dish = currentDish.copy(products = updatedProducts)
                        )
                    )
                }
            }

            is UsedHalfProductDomain -> {
                val index = currentDish.halfProducts.indexOf(currentlyEditedItem)
                if (index != -1) {
                    val updatedItem = currentlyEditedItem.copy(quantity = quantity)
                    val updatedHalfProducts = currentDish.halfProducts.toMutableList()
                        .apply { set(index, updatedItem) }
                    updateUiState(
                        uiState.copy(
                            dish = currentDish.copy(halfProducts = updatedHalfProducts)
                        )
                    )
                }
            }
        }
    }

    /**
     * Removes an item from a dish.
     *
     * @param item The item to remove
     * @param uiState The current UI state of the dish details screen
     */
    fun removeItem(
        item: UsedItem,
        uiState: DishDetailsUiState
    ) {
        val currentDish = uiState.dish
        if (currentDish == null) {
            Timber.e("Cannot remove item - current dish is null")
            return
        }

        when (item) {
            is UsedProductDomain -> {
                val index = currentDish.products.indexOf(item)
                val updatedProducts = currentDish.products.filter { it != item }
                updateUiState(
                    uiState.copy(
                        dish = currentDish.copy(products = updatedProducts),
                        lastRemovedItem = JustRemovedItem(item, index)
                    )
                )
            }

            is UsedHalfProductDomain -> {
                val index = currentDish.halfProducts.indexOf(item)
                val updatedHalfProducts = currentDish.halfProducts.filter { it != item }
                updateUiState(
                    uiState.copy(
                        dish = currentDish.copy(halfProducts = updatedHalfProducts),
                        lastRemovedItem = JustRemovedItem(item, index)
                    )
                )
            }
        }
    }

    /**
     * Restores a previously removed item to the dish.
     *
     * @param item The item to restore
     * @param uiState The current UI state of the dish details screen
     */
    fun restoreItem(
        item: JustRemovedItem,
        uiState: DishDetailsUiState
    ) {
        val currentDish = uiState.dish ?: return
        when (item.item) {
            is UsedProductDomain -> {
                val updatedProducts =
                    currentDish.products.toMutableList().apply { add(item.index, item.item) }
                updateUiState(
                    uiState.copy(
                        dish = currentDish.copy(products = updatedProducts),
                        lastRemovedItem = null
                    )
                )
            }

            is UsedHalfProductDomain -> {
                val updatedHalfProducts =
                    currentDish.halfProducts.toMutableList().apply { add(item.index, item.item) }
                updateUiState(
                    uiState.copy(
                        dish = currentDish.copy(halfProducts = updatedHalfProducts),
                        lastRemovedItem = null
                    )
                )
            }
        }
    }
}

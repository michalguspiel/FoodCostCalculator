package com.erdees.foodcostcalc.ui.screens.dishes.dishdetails

import com.erdees.foodcostcalc.domain.model.ItemUsageEntry
import com.erdees.foodcostcalc.domain.model.JustRemovedItem
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductAddedToDish
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductAddedToDish
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingItemFormData
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormData
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

            is ProductAddedToDish -> {
                val index = currentDish.productsNotSaved.indexOf(currentlyEditedItem)
                if (index != -1) {
                    val updatedItem = currentlyEditedItem.copy(quantity = quantity)
                    val updatedProductsNotSaved = currentDish.productsNotSaved.toMutableList()
                        .apply { set(index, updatedItem) }
                    updateUiState(
                        uiState.copy(
                            dish = currentDish.copy(productsNotSaved = updatedProductsNotSaved)
                        )
                    )
                }
            }

            is HalfProductAddedToDish -> {
                val index = currentDish.halfProductsNotSaved.indexOf(currentlyEditedItem)
                if (index != -1) {
                    val updatedItem = HalfProductAddedToDish(
                        item = currentlyEditedItem.item,
                        quantity = quantity,
                        quantityUnit = currentlyEditedItem.quantityUnit
                    )
                    val updatedHalfProductsNotSaved = currentDish.halfProductsNotSaved.toMutableList()
                        .apply { set(index, updatedItem) }
                    updateUiState(
                        uiState.copy(
                            dish = currentDish.copy(halfProductsNotSaved = updatedHalfProductsNotSaved)
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
        item: ItemUsageEntry,
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

            is ProductAddedToDish -> {
                val index = currentDish.productsNotSaved.indexOf(item)
                val updatedProductsNotSaved =
                    currentDish.productsNotSaved.filter { it != item }
                updateUiState(
                    uiState.copy(
                        dish = currentDish.copy(productsNotSaved = updatedProductsNotSaved),
                        lastRemovedItem = JustRemovedItem(item, index)
                    )
                )
            }

            is HalfProductAddedToDish -> {
                val index = currentDish.halfProductsNotSaved.indexOf(item)
                val updatedHalfProductsNotSaved =
                    currentDish.halfProductsNotSaved.filter { it != item }
                updateUiState(
                    uiState.copy(
                        dish = currentDish.copy(halfProductsNotSaved = updatedHalfProductsNotSaved),
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

            is ProductAddedToDish -> {
                val updatedProductsNotSaved =
                    currentDish.productsNotSaved.toMutableList()
                        .apply { add(item.index, item.item) }
                updateUiState(
                    uiState.copy(
                        dish = currentDish.copy(productsNotSaved = updatedProductsNotSaved),
                        lastRemovedItem = null
                    )
                )
            }

            is HalfProductAddedToDish -> {
                val updatedHalfProductsNotSaved =
                    currentDish.halfProductsNotSaved.toMutableList()
                        .apply { add(item.index, item.item) }
                updateUiState(
                    uiState.copy(
                        dish = currentDish.copy(halfProductsNotSaved = updatedHalfProductsNotSaved),
                        lastRemovedItem = null
                    )
                )
            }
        }
    }

    fun addNewProductToDish(
        uiState: DishDetailsUiState,
        newProduct: ProductDomain,
        newProductFormData: NewProductFormData
    ) {
        val dish = uiState.dish ?: error("Cannot add product - current dish is null")
        val quantityAddedToDish = newProductFormData.quantityAddedToDish.toDoubleOrNull()
            ?: error("Quantity for the product in the dish cannot be empty or invalid.")
        val unitForDish = newProductFormData.quantityAddedToDishUnit
            ?: error("Unit for the product in the dish cannot be empty.")
        val productAddedToDish = ProductAddedToDish(
            item = newProduct,
            quantity = quantityAddedToDish,
            quantityUnit = unitForDish
        )
        val updatedProductsNotSaved =
            dish.productsNotSaved.toMutableList().apply { add(productAddedToDish) }
        updateUiState(
            uiState.copy(dish = dish.copy(productsNotSaved = updatedProductsNotSaved))
        )
    }

    fun onAddExistingComponent(
        uiState: DishDetailsUiState,
        existingComponentFormData: ExistingItemFormData,
        selectedComponent: ComponentSelection
    ) {
        val existingComponent = selectedComponent as? ComponentSelection.ExistingComponent ?: return
        when (existingComponent.item) {
            is ProductDomain -> {
                addProduct(uiState, existingComponentFormData, existingComponent.item)
            }

            is HalfProductDomain -> {
                addHalfProduct(uiState, existingComponentFormData, existingComponent.item)
            }
        }
    }

    private fun addProduct(
        uiState: DishDetailsUiState,
        existingComponentFormData: ExistingItemFormData,
        product: ProductDomain,
    ) {
        val dish = uiState.dish ?: return
        val quantityAddedToDish = existingComponentFormData.quantityForDish.toDoubleOrNull()
            ?: error("Quantity for the product in the dish cannot be empty or invalid.")
        val unitForDish = existingComponentFormData.unitForDish
            ?: error("Unit for the product in the dish cannot be empty.")
        val item = ProductAddedToDish(
            item = product,
            quantity = quantityAddedToDish,
            quantityUnit = unitForDish
        )
        val updatedProductsNotSaved =
            dish.productsNotSaved.toMutableList().apply { add(item) }
        updateUiState(
            uiState.copy(dish = dish.copy(productsNotSaved = updatedProductsNotSaved))
        )
    }

    private fun addHalfProduct(
        uiState: DishDetailsUiState,
        existingComponentFormData: ExistingItemFormData,
        halfProduct: HalfProductDomain
    ) {
        val dish = uiState.dish ?: return
        val quantityAddedToDish = existingComponentFormData.quantityForDish.toDoubleOrNull()
            ?: error("Quantity for the product in the dish cannot be empty or invalid.")
        val unitForDish = existingComponentFormData.unitForDish
            ?: error("Unit for the product in the dish cannot be empty.")
        val item = HalfProductAddedToDish(
            item = halfProduct,
            quantity = quantityAddedToDish,
            quantityUnit = unitForDish
        )
        val updatedHalfProductsNotSaved =
            dish.halfProductsNotSaved.toMutableList().apply { add(item) }
        updateUiState(
            uiState.copy(dish = dish.copy(halfProductsNotSaved = updatedHalfProductsNotSaved))
        )
    }
}
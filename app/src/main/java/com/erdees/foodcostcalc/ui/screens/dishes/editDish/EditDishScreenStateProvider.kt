package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish
import com.erdees.foodcostcalc.data.model.local.joined.CompleteDish
import com.erdees.foodcostcalc.data.model.local.joined.ProductAndProductDish
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishDomain
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState

@Suppress("MagicNumber")
private fun createDishModel(): CompleteDish {
    return CompleteDish(
        dish = DishBase(0L, "Broccoli Chicken", 200.0, 10.0, null),
        recipe = null,
        products = listOf(
            ProductAndProductDish(
                productDish = ProductDish(0L, 0L, 0L, 1.0, "kilogram"),
                product = ProductBase(0L, "Broccoli", 10.0, 0.0, 50.0, "per kilogram")
            )
        ),
        halfProducts = emptyList(),
    )
}

class EditDishScreenStateProvider : PreviewParameterProvider<EditDishScreenState> {
    private val sampleDish = createDishModel().toDishDomain()
    private val sampleCurrency = android.icu.util.Currency.getInstance("USD")

    override val values: Sequence<EditDishScreenState> = sequenceOf(
        EditDishScreenState(
            dishId = 1L,
            usedItems = sampleDish.products,
            modifiedDishDomain = sampleDish,
            editableQuantity = "1.0",
            editableTax = "10",
            editableMargin = "150",
            editableName = sampleDish.name,
            editableCopiedDishName = "",
            editableTotalPrice = "120",
            currency = sampleCurrency,
            screenState = ScreenState.Idle // or ScreenState.Idle if you have one
        ),
        // Loading State
        EditDishScreenState(
            dishId = 1L,
            usedItems = emptyList(),
            modifiedDishDomain = sampleDish.copy(products = listOf()),
            editableQuantity = "",
            editableTax = "",
            editableMargin = "",
            editableName = "",
            editableCopiedDishName = "",
            editableTotalPrice = "",
            currency = sampleCurrency,
            screenState = ScreenState.Loading<Nothing>()
        ),
        // Error State
        EditDishScreenState(
            dishId = 1L,
            usedItems = emptyList(),
            modifiedDishDomain = null,
            editableQuantity = "",
            editableTax = "",
            editableMargin = "",
            editableName = "",
            editableCopiedDishName = "",
            editableTotalPrice = "",
            currency = sampleCurrency,
            screenState = ScreenState.Error(Error("Something went wrong!"))
        ),
        // Interaction: Edit Name
        EditDishScreenState(
            dishId = 1L,
            usedItems = sampleDish.products,
            modifiedDishDomain = sampleDish,
            editableQuantity = "1.0",
            editableTax = "10",
            editableMargin = "150",
            editableName = sampleDish.name,
            editableCopiedDishName = "",
            editableTotalPrice = sampleDish.totalPrice.toString(),
            currency = sampleCurrency,
            screenState = ScreenState.Interaction(InteractionType.EditName)
        ),
        // Interaction: Edit Tax
        EditDishScreenState(
            dishId = 1L,
            usedItems = sampleDish.products,
            modifiedDishDomain = sampleDish,
            editableQuantity = "1.0",
            editableTax = "10", // Current tax being edited
            editableMargin = "150",
            editableName = sampleDish.name,
            editableCopiedDishName = "",
            editableTotalPrice = sampleDish.totalPrice.toString(),
            currency = sampleCurrency,
            screenState = ScreenState.Interaction(InteractionType.EditTax)
        ),
    )
}
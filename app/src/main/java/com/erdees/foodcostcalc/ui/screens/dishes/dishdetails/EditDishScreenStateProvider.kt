package com.erdees.foodcostcalc.ui.screens.dishes.dishdetails

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish
import com.erdees.foodcostcalc.data.model.local.joined.CompleteDish
import com.erdees.foodcostcalc.data.model.local.joined.ProductAndProductDish
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishDomain
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

@Suppress("MagicNumber")
private fun createDishModel(): CompleteDish {
    return CompleteDish(
        dish = DishBase(0L, "Broccoli Chicken", 200.0, 10.0, null),
        recipe = null,
        products = listOf(
            ProductAndProductDish(
                productDish = ProductDish(0L, 0L, 0L, 1.0, MeasurementUnit.KILOGRAM),
                product = ProductBase(0L, "Broccoli", 10.0, 0.0, 50.0, MeasurementUnit.KILOGRAM)
            )
        ),
        halfProducts = emptyList(),
    )
}

class EditDishScreenStateProvider : PreviewParameterProvider<DishDetailsUiState> {
    private val sampleDish = createDishModel().toDishDomain()

    override val values = sequenceOf(
        // Idle state with dish
        DishDetailsUiState(
            dish = sampleDish,
            editableFields = EditableFields(
                name = sampleDish.name,
                totalPrice = sampleDish.totalPrice.toString(),
                tax = sampleDish.taxPercent.toString(),
                margin = sampleDish.marginPercent.toString()
            ),
            screenState = ScreenState.Idle
        ),

        // Loading state
        DishDetailsUiState(
            dish = sampleDish,
            screenState = ScreenState.Loading<Nothing>()
        ),

        // Error state
        DishDetailsUiState(
            dish = sampleDish,
            screenState = ScreenState.Error(Error("Sample error message"))
        ),

        // Interaction: Edit tax
        DishDetailsUiState(
            dish = sampleDish,
            editableFields = EditableFields(
                tax = sampleDish.taxPercent.toString()
            ),
            screenState = ScreenState.Interaction(InteractionType.EditTax)
        ),

        // Interaction: Edit margin
        DishDetailsUiState(
            dish = sampleDish,
            editableFields = EditableFields(
                margin = sampleDish.marginPercent.toString()
            ),
            screenState = ScreenState.Interaction(InteractionType.EditMargin)
        ),

        // Interaction: Edit name
        DishDetailsUiState(
            dish = sampleDish,
            editableFields = EditableFields(
                name = sampleDish.name
            ),
            screenState = ScreenState.Interaction(InteractionType.EditName)
        ),

        // Interaction: Copy dish
        DishDetailsUiState(
            dish = sampleDish,
            editableFields = EditableFields(
                copiedDishName = "Copy of ${sampleDish.name}"
            ),
            screenState = ScreenState.Interaction(InteractionType.CopyDish("Copy of ${sampleDish.name}"))
        ),

        // Show copy confirmation
        DishDetailsUiState(
            dish = sampleDish,
            showCopyConfirmation = true,
            screenState = ScreenState.Idle
        )
    )
}
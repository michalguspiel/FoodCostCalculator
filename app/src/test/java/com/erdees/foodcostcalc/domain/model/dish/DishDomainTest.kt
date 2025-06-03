package com.erdees.foodcostcalc.domain.model.dish

import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.domain.model.recipe.RecipeDomain
import org.junit.Assert.assertEquals
import org.junit.Test

class DishDomainTest {

    private fun createDishDomain(
        id: Long = 1L,
        name: String = "Test Dish",
        marginPercent: Double = 20.0,
        taxPercent: Double = 10.0,
        products: List<UsedProductDomain> = emptyList(),
        recipe: RecipeDomain? = null
    ): DishDomain {
        return DishDomain(
            id = id,
            name = name,
            marginPercent = marginPercent,
            taxPercent = taxPercent,
            products = products,
            halfProducts = emptyList(), // Assuming empty for these tests
            recipe = recipe
        )
    }

    @Test
    fun `test_withUpdatedTotalPrice_positiveMargin`() {
        // Arrange
        val initialProduct = ProductDomain(id = 1L, name = "test product", pricePerUnit = 100.0, unit = "kg", tax = 0.0, waste = 0.0)
        val usedProduct = UsedProductDomain(id = 1L, ownerId = 1L, item = initialProduct, quantity = 1.0, quantityUnit = "kg", weightPiece = 1.0)
        val dish = createDishDomain(
            products = listOf(usedProduct), // foodCost is 100.0
            taxPercent = 10.0,
            marginPercent = 20.0 // Initial total price: (100.0 + 20% margin) * (1 + 10% tax) = 120.0 * 1.1 = 132.0
        )
        assertEquals(132.0, dish.totalPrice, 0.01) // Verify initial calculation

        // Act
        val newTotalPrice = 165.0
        // Expected:
        // priceBeforeTax = 165.0 / (1 + 10.0 / 100) = 165.0 / 1.1 = 150.0
        // marginAmount = 150.0 - 100.0 (foodCost) = 50.0
        // newMarginPercent = (50.0 / 100.0) * 100 = 50.0
        val newDish = dish.withUpdatedTotalPrice(newTotalPrice)

        // Assert
        assertEquals(50.0, newDish.marginPercent, 0.01)
        assertEquals(newTotalPrice, newDish.totalPrice, 0.01)
    }

    @Test
    fun `test_withUpdatedTotalPrice_zeroFoodCost`() {
        // Arrange
        val dish = createDishDomain(
            products = emptyList(), // foodCost is 0.0
            taxPercent = 10.0,
            marginPercent = 0.0
        )
        assertEquals(0.0, dish.foodCost, 0.01)
        assertEquals(0.0, dish.totalPrice, 0.01) // Initial total price should be 0

        // Act
        // newTotalPrice = 100.0
        // priceBeforeTax = 100.0 / 1.1 = 90.909...
        // marginAmount = 90.909... - 0.0 = 90.909...
        // newMarginPercent = (90.909... / 0.0) * 100 -> results in 0.0 due to foodCost == 0.0 check
        val newDish = dish.withUpdatedTotalPrice(100.0)

        // Assert
        assertEquals(0.0, newDish.marginPercent, 0.01)
        // Total price should remain 0 because foodCost is 0, and margin is applied to foodcost.
        // The current implementation of totalPrice: foodCost * marginPercent / 100 then add tax.
        // If foodCost is 0, totalPrice will be 0.
        assertEquals(0.0, newDish.totalPrice, 0.01)
    }

    @Test
    fun `test_withUpdatedTotalPrice_zeroMargin`() {
        // Arrange
        val initialProduct = ProductDomain(id = 1L, name = "test product", pricePerUnit = 100.0, unit = "kg", tax = 0.0, waste = 0.0)
        val usedProduct = UsedProductDomain(id = 1L, ownerId = 1L, item = initialProduct, quantity = 1.0, quantityUnit = "kg", weightPiece = 1.0)
        val dish = createDishDomain(
            products = listOf(usedProduct), // foodCost is 100.0
            taxPercent = 0.0, // No tax
            marginPercent = 10.0 // Initial margin 10%, so totalPrice = 100.0 * 1.10 = 110.0
        )
        assertEquals(110.0, dish.totalPrice, 0.01)


        // Act
        // newTotalPrice = 100.0
        // priceBeforeTax = 100.0 / (1 + 0.0/100) = 100.0
        // marginAmount = 100.0 - 100.0 (foodCost) = 0.0
        // newMarginPercent = (0.0 / 100.0) * 100 = 0.0
        val newDish = dish.withUpdatedTotalPrice(100.0)

        // Assert
        assertEquals(0.0, newDish.marginPercent, 0.01)
        // totalPrice = (foodCost * (1 + newMarginPercent/100)) * (1 + taxPercent/100)
        // totalPrice = (100.0 * (1 + 0/100)) * (1 + 0/100) = 100.0
        assertEquals(100.0, newDish.totalPrice, 0.01)
    }

     @Test
    fun `totalPrice calculation should be correct`() {
        val initialProduct = ProductDomain(id = 1L, name = "test product", pricePerUnit = 125.0, unit = "kg", tax = 0.0, waste = 0.0)
        val usedProduct = UsedProductDomain(id = 1L, ownerId = 1L, item = initialProduct, quantity = 1.0, quantityUnit = "kg", weightPiece = 1.0)
        val dish = createDishDomain(
            products = listOf(usedProduct), // foodCost = 125.0
            marginPercent = 50.0,           // Price with margin: 125 * 1.50 = 187.5
            taxPercent = 20.0              // Tax amount: 187.5 * 0.20 = 37.5. Total: 187.5 + 37.5 = 225.0
        )
        assertEquals(225.0, dish.totalPrice, 0.01)
    }
}

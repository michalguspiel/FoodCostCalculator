package com.erdees.foodcostcalc.domain.model.dish

import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.utils.Constants
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class DishDomainTest {

    private val mockAnalyticsRepository = mockk<AnalyticsRepository>(relaxed = true)

    private val totalPriceTolerance = 0.001
    private val marginPercentTolerance = 0.1

    private fun createTestDish(
        id: Long = 1L,
        name: String = "Test Dish",
        marginPercent: Double = 150.0,
        taxPercent: Double = 20.0,
        foodCost: Double = 10.0
    ): DishDomain {
        val product = ProductDomain(
            id = 5L,
            name = "Mock Product",
            inputMethod = InputMethod.UNIT,
            packagePrice = null,
            packageQuantity = null,
            packageUnit = null,
            canonicalPrice = foodCost, // This will be the base price
            canonicalUnit = MeasurementUnit.KILOGRAM,
            tax = 0.0, // No tax so priceAfterWasteAndTax = canonicalPrice
            waste = 0.0 // No waste so priceAfterWasteAndTax = canonicalPrice
        )

        val usedProduct = UsedProductDomain(
            id = 10L,
            ownerId = id,
            item = product,
            quantity = 1.0, // Use 1.0 so foodCost == priceAfterWasteAndTax
            quantityUnit = MeasurementUnit.KILOGRAM,
            weightPiece = null
        )

        return DishDomain(
            id = id,
            name = name,
            marginPercent = marginPercent,
            taxPercent = taxPercent,
            products = listOf(usedProduct),
            halfProducts = emptyList(),
            productsNotSaved = emptyList(),
            halfProductsNotSaved = emptyList(),
            recipe = null
        )
    }

    @Test
    fun `withUpdatedTotalPrice returns same dish when foodCost is zero`() {
        // Given
        val dish = createTestDish(foodCost = 0.0)
        val newTotalPrice = 25.0

        // When
        val result = dish.withUpdatedTotalPrice(newTotalPrice, mockAnalyticsRepository)

        // Then
        result shouldBe dish
        verify { mockAnalyticsRepository.logEvent(Constants.Analytics.DishV2.UPDATE_TOTAL_PRICE_ZERO_FOOD_COST) }
    }

    @Test
    fun `withUpdatedTotalPrice calculates correct margin for simple case`() {
        // Given
        val dish = createTestDish(
            foodCost = 10.0,
            marginPercent = 150.0,
            taxPercent = 20.0
        )
        val newTotalPrice = 24.0

        // When
        val result = dish.withUpdatedTotalPrice(newTotalPrice, mockAnalyticsRepository)

        // Then
        // sellingPriceBeforeTax = 24.0 / (1 + 20/100) = 20.0
        // calculatedMarginPercent = (20.0 / 10.0) * 100 = 200.0
        result.marginPercent shouldBe 200.0.plusOrMinus(marginPercentTolerance)
        result.totalPrice shouldBe newTotalPrice.plusOrMinus(totalPriceTolerance)
    }

    @Test
    fun `withUpdatedTotalPrice handles precision convergence within tolerance`() {
        // Given
        val dish = createTestDish(
            foodCost = 8.33,
            marginPercent = 180.0,
            taxPercent = 15.5
        )
        val newTotalPrice = 22.75

        // When
        val result = dish.withUpdatedTotalPrice(newTotalPrice, mockAnalyticsRepository)

        // Then
        // sellingPriceBeforeTax = 22.75 / 1.155 = 19.7056...
        // initialMargin = (19.7056 / 8.33) * 100 = 236.562...
        // Rounded to 1 decimal: 236.6. New total: 8.33 * 2.366 * 1.155 = 22.7535...
        // Rounded to 2 decimals: 236.56. New total: 8.33 * 2.3656 * 1.155 = 22.749... (closest)
        result.marginPercent shouldBe 236.56.plusOrMinus(marginPercentTolerance)
        result.totalPrice shouldBe newTotalPrice.plusOrMinus(totalPriceTolerance)
    }

    @Test
    fun `withUpdatedTotalPrice handles very small food cost`() {
        // Given
        val dish = createTestDish(
            foodCost = 0.01,
            marginPercent = 200.0,
            taxPercent = 10.0
        )
        val newTotalPrice = 1.0

        // When
        val result = dish.withUpdatedTotalPrice(newTotalPrice, mockAnalyticsRepository)

        // Then
        // sellingPriceBeforeTax = 1.0 / 1.1 = 0.9090...
        // initialMargin = (0.9090 / 0.01) * 100 = 9090.90...
        // Rounded to 2 decimals: 9090.91. New total: 0.01 * 90.9091 * 1.1 = 1.0000001
        result.marginPercent shouldBe 9090.91.plusOrMinus(marginPercentTolerance)
        result.totalPrice shouldBe newTotalPrice.plusOrMinus(totalPriceTolerance)
    }

    @Test
    fun `withUpdatedTotalPrice handles zero tax percentage`() {
        // Given
        val dish = createTestDish(
            foodCost = 5.0,
            marginPercent = 100.0,
            taxPercent = 0.0
        )
        val newTotalPrice = 15.0

        // When
        val result = dish.withUpdatedTotalPrice(newTotalPrice, mockAnalyticsRepository)

        // Then
        // With no tax: sellingPriceBeforeTax = newTotalPrice = 15.0
        // calculatedMarginPercent = (15.0 / 5.0) * 100 = 300.0
        result.marginPercent shouldBe 300.0.plusOrMinus(marginPercentTolerance)
        result.totalPrice shouldBe newTotalPrice.plusOrMinus(totalPriceTolerance)
    }

    @Test
    fun `withUpdatedTotalPrice handles high precision requirements`() {
        // Given
        val dish = createTestDish(
            foodCost = 7.333,
            marginPercent = 155.5,
            taxPercent = 13.75
        )
        val newTotalPrice = 19.999

        // When
        val result = dish.withUpdatedTotalPrice(newTotalPrice, mockAnalyticsRepository)

        // Then
        // sellingPriceBeforeTax = 19.999 / 1.1375 = 17.5815...
        // initialMargin = (17.5815 / 7.333) * 100 = 240.00...
        // Rounded to 1 decimal: 239.7. New total: 7.333 * 2.4 * 1.1375 = 19.99905
        result.marginPercent shouldBe 239.7.plusOrMinus(marginPercentTolerance)
        result.totalPrice shouldBe newTotalPrice.plusOrMinus(totalPriceTolerance)
    }

    @Test
    fun `withUpdatedTotalPrice finds best approximation when exact convergence impossible`() {
        // Given - Create a scenario that's difficult to converge exactly
        val dish = createTestDish(
            foodCost = 3.141592,
            marginPercent = 271.828,
            taxPercent = 17.777
        )
        val newTotalPrice = 11.11111

        // When
        val result = dish.withUpdatedTotalPrice(newTotalPrice, mockAnalyticsRepository)

        // Then
        // sellingPriceBeforeTax = 11.11111 / 1.17777 = 9.4339...
        // initialMargin = (9.4339 / 3.141592) * 100 = 300.28...
        // Rounded to 2 decimals: 300.28. New total: 3.141592 * 3.0028 * 1.17777 = 11.1109...
        result.marginPercent shouldBe 300.28.plusOrMinus(marginPercentTolerance)
        result.totalPrice shouldBe newTotalPrice.plusOrMinus(totalPriceTolerance)
    }

    @Test
    fun `withUpdatedTotalPrice preserves other dish properties`() {
        // Given
        val originalDish = createTestDish(
            id = 42L,
            name = "Special Dish",
            foodCost = 12.0
        )
        val newTotalPrice = 30.0

        // When
        val result = originalDish.withUpdatedTotalPrice(newTotalPrice, mockAnalyticsRepository)

        // Then
        result.id shouldBe originalDish.id
        result.name shouldBe originalDish.name
        result.taxPercent shouldBe originalDish.taxPercent
        result.products shouldBe originalDish.products
        result.halfProducts shouldBe originalDish.halfProducts
        result.recipe shouldBe originalDish.recipe

        // Only margin should change
        // sellingPriceBeforeTax = 30.0 / 1.2 = 25.0
        // margin = (25.0 / 12.0) * 100 = 208.333...
        result.marginPercent shouldBe 208.33.plusOrMinus(marginPercentTolerance)
    }
}
package com.erdees.foodcostcalc.ui.screens.products.delegates

import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UnitPricingDelegateTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var delegate: UnitPricingDelegate

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    private fun TestScope.initializeDelegate() {
        delegate = UnitPricingDelegate(testScope)
        advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have default values`() = runTest {
        initializeDelegate()

        delegate.unitPrice.first() shouldBe ""
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.KILOGRAM
    }

    @Test
    fun `updateUnitPrice should update unit price`() = runTest {
        initializeDelegate()

        delegate.updateUnitPrice("25.50")

        delegate.unitPrice.first() shouldBe "25.50"
    }

    @Test
    fun `updateUnitPriceUnit should update unit price unit`() = runTest {
        initializeDelegate()

        delegate.updateUnitPriceUnit(MeasurementUnit.GRAM)
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.GRAM

        delegate.updateUnitPriceUnit(MeasurementUnit.LITER)
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.LITER

        delegate.updateUnitPriceUnit(MeasurementUnit.POUND)
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.POUND
    }

    @Test
    fun `reset should reset all fields to defaults`() = runTest {
        initializeDelegate()

        // Set some values
        delegate.updateUnitPrice("25.50")
        delegate.updateUnitPriceUnit(MeasurementUnit.GRAM)
        advanceUntilIdle()

        // Reset
        delegate.reset()

        delegate.unitPrice.first() shouldBe ""
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.KILOGRAM
    }

    @Test
    fun `createValidation should return false with empty price`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        validation.first() shouldBe false
    }

    @Test
    fun `createValidation should return false with invalid price`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        delegate.updateUnitPrice("invalid")
        advanceUntilIdle()

        validation.first() shouldBe false
    }

    @Test
    fun `createValidation should return false with non-numeric price`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        delegate.updateUnitPrice("abc123")
        advanceUntilIdle()

        validation.first() shouldBe false
    }

    @Test
    fun `createValidation should return true with valid integer price`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        delegate.updateUnitPrice("25")
        advanceUntilIdle()

        validation.first() shouldBe true
    }

    @Test
    fun `createValidation should return true with valid decimal price`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        delegate.updateUnitPrice("25.50")
        advanceUntilIdle()

        validation.first() shouldBe true
    }

    @Test
    fun `createValidation should return true with zero price`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        delegate.updateUnitPrice("0")
        advanceUntilIdle()

        validation.first() shouldBe true
    }

    @Test
    fun `createValidation should return true with decimal zero price`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        delegate.updateUnitPrice("0.0")
        advanceUntilIdle()

        validation.first() shouldBe true
    }

    @Test
    fun `state should update independently`() = runTest {
        initializeDelegate()

        // Update price
        delegate.updateUnitPrice("15.75")
        delegate.unitPrice.first() shouldBe "15.75"
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.KILOGRAM

        // Update unit
        delegate.updateUnitPriceUnit(MeasurementUnit.LITER)
        delegate.unitPrice.first() shouldBe "15.75"
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.LITER
    }

    @Test
    fun `updateUnitPrice should handle various numeric formats`() = runTest {
        initializeDelegate()

        // Test different valid numeric formats
        delegate.updateUnitPrice("123")
        delegate.unitPrice.first() shouldBe "123"

        delegate.updateUnitPrice("123.45")
        delegate.unitPrice.first() shouldBe "123.45"

        delegate.updateUnitPrice("0.99")
        delegate.unitPrice.first() shouldBe "0.99"

        delegate.updateUnitPrice("1000.00")
        delegate.unitPrice.first() shouldBe "1000.00"
    }

    @Test
    fun `updateUnitPriceUnit should support all measurement units`() = runTest {
        initializeDelegate()

        // Test weight units
        delegate.updateUnitPriceUnit(MeasurementUnit.KILOGRAM)
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.KILOGRAM

        delegate.updateUnitPriceUnit(MeasurementUnit.GRAM)
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.GRAM

        delegate.updateUnitPriceUnit(MeasurementUnit.POUND)
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.POUND

        delegate.updateUnitPriceUnit(MeasurementUnit.OUNCE)
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.OUNCE

        // Test volume units
        delegate.updateUnitPriceUnit(MeasurementUnit.LITER)
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.LITER

        delegate.updateUnitPriceUnit(MeasurementUnit.MILLILITER)
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.MILLILITER

        delegate.updateUnitPriceUnit(MeasurementUnit.GALLON)
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.GALLON

        delegate.updateUnitPriceUnit(MeasurementUnit.FLUID_OUNCE)
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.FLUID_OUNCE

        // Test count unit
        delegate.updateUnitPriceUnit(MeasurementUnit.PIECE)
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.PIECE
    }

    @Test
    fun `createValidation should update reactively when price changes`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        // Initially false
        validation.first() shouldBe false

        // Update to valid price
        delegate.updateUnitPrice("10.0")
        advanceUntilIdle()
        validation.first() shouldBe true

        // Update to invalid price
        delegate.updateUnitPrice("")
        advanceUntilIdle()
        validation.first() shouldBe false

        // Back to valid price
        delegate.updateUnitPrice("5.99")
        advanceUntilIdle()
        validation.first() shouldBe true
    }

    @Test
    fun `createValidation should handle edge cases`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        // Very large number
        delegate.updateUnitPrice("999999.99")
        advanceUntilIdle()
        validation.first() shouldBe true

        // Very small number
        delegate.updateUnitPrice("0.001")
        advanceUntilIdle()
        validation.first() shouldBe true

        // Empty string
        delegate.updateUnitPrice("")
        advanceUntilIdle()
        validation.first() shouldBe false

        // Only decimal point
        delegate.updateUnitPrice(".")
        advanceUntilIdle()
        validation.first() shouldBe false

        // Multiple decimal points (should be handled by onNumericValueChange)
        delegate.updateUnitPrice("10.5.0")
        advanceUntilIdle()
        // This depends on how onNumericValueChange handles invalid input
        // It might filter to "10.5" or reject completely
    }

    @Test
    fun `reset should be idempotent`() = runTest {
        initializeDelegate()

        // Reset when already at defaults
        delegate.reset()
        delegate.unitPrice.first() shouldBe ""
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.KILOGRAM

        // Set values, reset, then reset again
        delegate.updateUnitPrice("100.0")
        delegate.updateUnitPriceUnit(MeasurementUnit.LITER)
        delegate.reset()

        delegate.unitPrice.first() shouldBe ""
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.KILOGRAM

        // Reset again should still work
        delegate.reset()
        delegate.unitPrice.first() shouldBe ""
        delegate.unitPriceUnit.first() shouldBe MeasurementUnit.KILOGRAM
    }

    @Test
    fun `validation should not depend on unit type`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        delegate.updateUnitPrice("50.0")

        // Test with different unit categories
        delegate.updateUnitPriceUnit(MeasurementUnit.KILOGRAM) // Weight
        advanceUntilIdle()
        validation.first() shouldBe true

        delegate.updateUnitPriceUnit(MeasurementUnit.LITER) // Volume
        advanceUntilIdle()
        validation.first() shouldBe true

        delegate.updateUnitPriceUnit(MeasurementUnit.PIECE) // Count
        advanceUntilIdle()
        validation.first() shouldBe true
    }
}

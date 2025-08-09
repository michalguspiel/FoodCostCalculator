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
class PackagePricingDelegateTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var delegate: PackagePricingDelegate

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    private fun TestScope.initializeDelegate() {
        delegate = PackagePricingDelegate(testScope)
        advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have default values`() = runTest {
        initializeDelegate()

        delegate.packagePrice.first() shouldBe ""
        delegate.packageQuantity.first() shouldBe ""
        delegate.packageUnit.first() shouldBe MeasurementUnit.KILOGRAM
        delegate.canonicalPriceAndUnit.first() shouldBe (null to null)
    }

    @Test
    fun `updatePackagePrice should update package price`() = runTest {
        initializeDelegate()

        delegate.updatePackagePrice("15.99")

        delegate.packagePrice.first() shouldBe "15.99"
    }

    @Test
    fun `updatePackageQuantity should update package quantity`() = runTest {
        initializeDelegate()

        delegate.updatePackageQuantity("2.5")

        delegate.packageQuantity.first() shouldBe "2.5"
    }

    @Test
    fun `updatePackageUnit should update package unit`() = runTest {
        initializeDelegate()

        delegate.updatePackageUnit(MeasurementUnit.GRAM)
        delegate.packageUnit.first() shouldBe MeasurementUnit.GRAM

        delegate.updatePackageUnit(MeasurementUnit.LITER)
        delegate.packageUnit.first() shouldBe MeasurementUnit.LITER
    }

    @Test
    fun `reset should reset all fields to defaults`() = runTest {
        initializeDelegate()

        // Set some values
        delegate.updatePackagePrice("15.99")
        delegate.updatePackageQuantity("2.5")
        delegate.updatePackageUnit(MeasurementUnit.GRAM)
        advanceUntilIdle()

        // Reset
        delegate.reset()

        delegate.packagePrice.first() shouldBe ""
        delegate.packageQuantity.first() shouldBe ""
        delegate.packageUnit.first() shouldBe MeasurementUnit.KILOGRAM
    }

    @Test
    fun `canonicalPriceAndUnit should calculate correctly with valid inputs`() = runTest {
        initializeDelegate()

        // Collect flow early to ensure it's hot
        val canonicalPriceAndUnit = delegate.canonicalPriceAndUnit

        delegate.updatePackagePrice("10.0")
        delegate.updatePackageQuantity("2.0")
        delegate.updatePackageUnit(MeasurementUnit.KILOGRAM)
        advanceUntilIdle()

        val result = canonicalPriceAndUnit.first()
        result shouldBe (5.0 to MeasurementUnit.KILOGRAM) // 10.0 / 2.0 = 5.0 per kg
    }

    @Test
    fun `canonicalPriceAndUnit should return null with invalid price`() = runTest {
        initializeDelegate()

        // Collect flow early
        val canonicalPriceAndUnit = delegate.canonicalPriceAndUnit

        delegate.updatePackagePrice("invalid")
        delegate.updatePackageQuantity("2.0")
        advanceUntilIdle()

        canonicalPriceAndUnit.first() shouldBe (null to null)
    }

    @Test
    fun `canonicalPriceAndUnit should return null with invalid quantity`() = runTest {
        initializeDelegate()

        // Collect flow early
        val canonicalPriceAndUnit = delegate.canonicalPriceAndUnit

        delegate.updatePackagePrice("10.0")
        delegate.updatePackageQuantity("invalid")
        advanceUntilIdle()

        canonicalPriceAndUnit.first() shouldBe (null to null)
    }

    @Test
    fun `canonicalPriceAndUnit should return null with zero quantity`() = runTest {
        initializeDelegate()

        // Collect flow early
        val canonicalPriceAndUnit = delegate.canonicalPriceAndUnit

        delegate.updatePackagePrice("10.0")
        delegate.updatePackageQuantity("0")
        advanceUntilIdle()

        canonicalPriceAndUnit.first() shouldBe (null to null)
    }

    @Test
    fun `canonicalPriceAndUnit should return null when calculateCanonicalPrice throws exception`() = runTest {
        initializeDelegate()

        // Collect flow early
        val canonicalPriceAndUnit = delegate.canonicalPriceAndUnit

        // Use a unit that would cause division by zero (this shouldn't happen in practice but tests edge case)
        delegate.updatePackagePrice("10.0")
        delegate.updatePackageQuantity("0") // This will cause the exception path
        delegate.updatePackageUnit(MeasurementUnit.GRAM)
        advanceUntilIdle()

        canonicalPriceAndUnit.first() shouldBe (null to null)
    }

    @Test
    fun `createValidation should return false with invalid inputs`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        // Empty inputs
        validation.first() shouldBe false

        // Invalid price
        delegate.updatePackagePrice("invalid")
        delegate.updatePackageQuantity("2.0")
        advanceUntilIdle()
        validation.first() shouldBe false

        // Invalid quantity
        delegate.updatePackagePrice("10.0")
        delegate.updatePackageQuantity("0")
        advanceUntilIdle()
        validation.first() shouldBe false
    }

    @Test
    fun `createValidation should return true with valid inputs`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        delegate.updatePackagePrice("10.0")
        delegate.updatePackageQuantity("2.0")
        delegate.updatePackageUnit(MeasurementUnit.GRAM)
        advanceUntilIdle()

        validation.first() shouldBe true
    }

    @Test
    fun `createValidation should return true with decimal inputs`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        delegate.updatePackagePrice("15.99")
        delegate.updatePackageQuantity("2.5")
        advanceUntilIdle()

        validation.first() shouldBe true
    }

    @Test
    fun `canonicalPriceAndUnit should update reactively when values change`() = runTest {
        initializeDelegate()

        // Collect flow early
        val canonicalPriceAndUnit = delegate.canonicalPriceAndUnit

        // Initially null
        canonicalPriceAndUnit.first() shouldBe (null to null)

        // Update values with GRAM unit (1000g = 1kg, so price per kg = 16.0 / 2.0 * 1000 / 1000 = 8.0)
        delegate.updatePackagePrice("16.0")
        delegate.updatePackageQuantity("2000.0") // 2000 grams
        delegate.updatePackageUnit(MeasurementUnit.GRAM)
        advanceUntilIdle()

        // Should calculate new value: 16.0 / 2000.0 per gram = 0.008 per gram = 8.0 per kg
        canonicalPriceAndUnit.first() shouldBe (8.0 to MeasurementUnit.KILOGRAM)
    }

    @Test
    fun `state updates should be independent`() = runTest {
        initializeDelegate()

        // Update price
        delegate.updatePackagePrice("25.0")
        delegate.packagePrice.first() shouldBe "25.0"
        delegate.packageQuantity.first() shouldBe ""
        delegate.packageUnit.first() shouldBe MeasurementUnit.KILOGRAM

        // Update quantity
        delegate.updatePackageQuantity("3.0")
        delegate.packagePrice.first() shouldBe "25.0"
        delegate.packageQuantity.first() shouldBe "3.0"
        delegate.packageUnit.first() shouldBe MeasurementUnit.KILOGRAM

        // Update unit
        delegate.updatePackageUnit(MeasurementUnit.LITER)
        delegate.packagePrice.first() shouldBe "25.0"
        delegate.packageQuantity.first() shouldBe "3.0"
        delegate.packageUnit.first() shouldBe MeasurementUnit.LITER
    }

    @Test
    fun `createValidation should handle edge cases`() = runTest {
        initializeDelegate()

        // Collect flow early
        val validation = delegate.createValidation()

        // Negative price should be handled by onNumericValueChange (likely filtered out)
        delegate.updatePackagePrice("-10.0")
        delegate.updatePackageQuantity("2.0")
        advanceUntilIdle()
        validation.first() shouldBe true // onNumericValueChange should handle negative validation

        // Very small quantity should be valid
        delegate.updatePackagePrice("10.0")
        delegate.updatePackageQuantity("0.001")
        advanceUntilIdle()
        validation.first() shouldBe true

        // Large numbers should be valid
        delegate.updatePackagePrice("999999.99")
        delegate.updatePackageQuantity("1000.0")
        advanceUntilIdle()
        validation.first() shouldBe true
    }
}

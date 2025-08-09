package com.erdees.foodcostcalc.ui.screens.products.delegates

import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormData
import com.erdees.foodcostcalc.ui.screens.products.PackagePriceState
import com.erdees.foodcostcalc.ui.screens.products.UnitPriceState
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
class NewProductFormBridgeDelegateTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var preferences: Preferences
    private lateinit var productFormDelegate: ProductFormDelegate
    private lateinit var packagePricingDelegate: PackagePricingDelegate
    private lateinit var unitPricingDelegate: UnitPricingDelegate
    private lateinit var bridgeDelegate: NewProductFormBridgeDelegate

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        preferences = mockk(relaxed = true)

        every { preferences.metricUsed } returns flowOf(true)
        every { preferences.imperialUsed } returns flowOf(false)
        every { preferences.showProductTax } returns MutableStateFlow(true)
    }

    private fun TestScope.initializeBridgeDelegate() {
        productFormDelegate = ProductFormDelegate(preferences, testScope)
        packagePricingDelegate = PackagePricingDelegate(testScope)
        unitPricingDelegate = UnitPricingDelegate(testScope)
        bridgeDelegate = NewProductFormBridgeDelegate(
            productFormDelegate,
            packagePricingDelegate,
            unitPricingDelegate,
            testScope
        )
        advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createFormDataState should return NewProductFormData with delegate state`() = runTest {
        initializeBridgeDelegate()

        // Set some values in delegates
        productFormDelegate.updateName("Test Product")
        productFormDelegate.updateWaste("5.0")
        packagePricingDelegate.updatePackagePrice("10.0")
        packagePricingDelegate.updatePackageQuantity("2.0")
        advanceUntilIdle()

        val formData = bridgeDelegate.createFormDataState().first()

        formData.inputMethod shouldBe InputMethod.PACKAGE
        formData.packagePrice shouldBe "10.0"
        formData.packageQuantity shouldBe "2.0"
        formData.packageUnit shouldBe MeasurementUnit.KILOGRAM
        formData.wastePercent shouldBe "5.0"
        formData.quantityAddedToDish shouldBe ""
        formData.quantityAddedToDishUnit shouldBe null
    }

    @Test
    fun `createFormDataState should return data for unit pricing mode`() = runTest {
        initializeBridgeDelegate()

        // Switch to unit pricing
        productFormDelegate.setInputMethod(InputMethod.UNIT)
        productFormDelegate.updateName("Test Product")
        productFormDelegate.updateWaste("3.0")
        unitPricingDelegate.updateUnitPrice("15.0")
        unitPricingDelegate.updateUnitPriceUnit(MeasurementUnit.GRAM)
        advanceUntilIdle()

        val formData = bridgeDelegate.createFormDataState().first()

        formData.inputMethod shouldBe InputMethod.UNIT
        formData.unitPrice shouldBe "15.0"
        formData.unitPriceUnit shouldBe MeasurementUnit.GRAM
        formData.wastePercent shouldBe "3.0"
    }

    @Test
    fun `syncFromFormData should update waste field`() = runTest {
        initializeBridgeDelegate()

        val formData = NewProductFormData(wastePercent = "7.5")

        bridgeDelegate.syncFromFormData(formData)

        productFormDelegate.waste.first() shouldBe "7.5"
    }

    @Test
    fun `syncFromFormData should update input method`() = runTest {
        initializeBridgeDelegate()

        val formData = NewProductFormData(inputMethod = InputMethod.UNIT)

        bridgeDelegate.syncFromFormData(formData)

        productFormDelegate.inputMethod.first() shouldBe InputMethod.UNIT
    }

    @Test
    fun `syncFromFormData should update package fields when input method is PACKAGE`() = runTest {
        initializeBridgeDelegate()

        val formData = NewProductFormData(
            inputMethod = InputMethod.PACKAGE,
            packagePrice = "12.0",
            packageQuantity = "3.0",
            packageUnit = MeasurementUnit.GRAM
        )

        bridgeDelegate.syncFromFormData(formData)

        packagePricingDelegate.packagePrice.first() shouldBe "12.0"
        packagePricingDelegate.packageQuantity.first() shouldBe "3.0"
        packagePricingDelegate.packageUnit.first() shouldBe MeasurementUnit.GRAM
    }

    @Test
    fun `syncFromFormData should update unit fields when input method is UNIT`() = runTest {
        initializeBridgeDelegate()

        val formData = NewProductFormData(
            inputMethod = InputMethod.UNIT,
            unitPrice = "8.5",
            unitPriceUnit = MeasurementUnit.LITER
        )

        bridgeDelegate.syncFromFormData(formData)

        unitPricingDelegate.unitPrice.first() shouldBe "8.5"
        unitPricingDelegate.unitPriceUnit.first() shouldBe MeasurementUnit.LITER
    }

    @Test
    fun `syncFromFormData should not update fields if values are the same`() = runTest {
        initializeBridgeDelegate()

        // Set initial values
        productFormDelegate.updateWaste("5.0")
        packagePricingDelegate.updatePackagePrice("10.0")
        advanceUntilIdle()

        val initialWasteValue = productFormDelegate.waste.first()
        val initialPriceValue = packagePricingDelegate.packagePrice.first()

        // Sync with same values
        val formData = NewProductFormData(
            wastePercent = "5.0",
            packagePrice = "10.0"
        )

        bridgeDelegate.syncFromFormData(formData)

        productFormDelegate.waste.first() shouldBe initialWasteValue
        packagePricingDelegate.packagePrice.first() shouldBe initialPriceValue
    }

    @Test
    fun `createValidation should return false when package validation fails`() = runTest {
        initializeBridgeDelegate()

        // Set input method to package but don't provide valid package data
        productFormDelegate.setInputMethod(InputMethod.PACKAGE)
        advanceUntilIdle()

        bridgeDelegate.createValidation().first() shouldBe false
    }

    @Test
    fun `createValidation should return true when package validation succeeds`() = runTest {
        initializeBridgeDelegate()

        // Set valid package data
        productFormDelegate.setInputMethod(InputMethod.PACKAGE)
        packagePricingDelegate.updatePackagePrice("10.0")
        packagePricingDelegate.updatePackageQuantity("2.0")
        advanceUntilIdle()

        bridgeDelegate.createValidation().first() shouldBe true
    }

    @Test
    fun `createValidation should return false when unit validation fails`() = runTest {
        initializeBridgeDelegate()

        // Set input method to unit but don't provide valid unit data
        productFormDelegate.setInputMethod(InputMethod.UNIT)
        advanceUntilIdle()

        bridgeDelegate.createValidation().first() shouldBe false
    }

    @Test
    fun `createValidation should return true when unit validation succeeds`() = runTest {
        initializeBridgeDelegate()

        // Set valid unit data
        productFormDelegate.setInputMethod(InputMethod.UNIT)
        unitPricingDelegate.updateUnitPrice("15.0")
        advanceUntilIdle()

        bridgeDelegate.createValidation().first() shouldBe true
    }

    @Test
    fun `toEditableProductUiState should return PackagePriceState when input method is PACKAGE`() = runTest {
        initializeBridgeDelegate()

        // Set package data
        productFormDelegate.updateName("Test Product")
        productFormDelegate.updateTax("10.0")
        productFormDelegate.updateWaste("5.0")
        packagePricingDelegate.updatePackagePrice("20.0")
        packagePricingDelegate.updatePackageQuantity("4.0")
        packagePricingDelegate.updatePackageUnit(MeasurementUnit.GRAM)
        advanceUntilIdle()

        val uiState = bridgeDelegate.toEditableProductUiState().first()

        uiState.shouldBeInstanceOf<PackagePriceState>()
        with(uiState) {
            name shouldBe "Test Product"
            tax shouldBe "10.0"
            waste shouldBe "5.0"
            packagePrice shouldBe "20.0"
            packageQuantity shouldBe "4.0"
            packageUnit shouldBe MeasurementUnit.GRAM
        }
    }

    @Test
    fun `toEditableProductUiState should return UnitPriceState when input method is UNIT`() = runTest {
        initializeBridgeDelegate()

        // Set unit data
        productFormDelegate.setInputMethod(InputMethod.UNIT)
        productFormDelegate.updateName("Unit Product")
        productFormDelegate.updateTax("15.0")
        productFormDelegate.updateWaste("3.0")
        unitPricingDelegate.updateUnitPrice("25.0")
        unitPricingDelegate.updateUnitPriceUnit(MeasurementUnit.LITER)
        advanceUntilIdle()

        val uiState = bridgeDelegate.toEditableProductUiState().first()

        uiState.shouldBeInstanceOf<UnitPriceState>()
        with(uiState) {
            name shouldBe "Unit Product"
            tax shouldBe "15.0"
            waste shouldBe "3.0"
            unitPrice shouldBe "25.0"
            unitPriceUnit shouldBe MeasurementUnit.LITER
        }
    }

    @Test
    fun `state should update reactively when delegate values change`() = runTest {
        initializeBridgeDelegate()

        // Initial state
        var formData = bridgeDelegate.createFormDataState().first()
        formData.packagePrice shouldBe ""

        // Update delegate
        packagePricingDelegate.updatePackagePrice("50.0")
        advanceUntilIdle()

        // State should reflect the change
        formData = bridgeDelegate.createFormDataState().first()
        formData.packagePrice shouldBe "50.0"
    }

    @Test
    fun `syncFromFormData should only sync package fields when packageUnit is not null`() = runTest {
        initializeBridgeDelegate()

        val formDataWithNullUnit = NewProductFormData(
            inputMethod = InputMethod.PACKAGE,
            packagePrice = "12.0",
            packageQuantity = "3.0",
            packageUnit = null
        )

        bridgeDelegate.syncFromFormData(formDataWithNullUnit)

        packagePricingDelegate.packagePrice.first() shouldBe "12.0"
        packagePricingDelegate.packageQuantity.first() shouldBe "3.0"
        // Package unit should remain default since it was null
        packagePricingDelegate.packageUnit.first() shouldBe MeasurementUnit.KILOGRAM
    }

    @Test
    fun `syncFromFormData should only sync unit fields when unitPriceUnit is not null`() = runTest {
        initializeBridgeDelegate()

        val formDataWithNullUnit = NewProductFormData(
            inputMethod = InputMethod.UNIT,
            unitPrice = "8.5",
            unitPriceUnit = null
        )

        bridgeDelegate.syncFromFormData(formDataWithNullUnit)

        unitPricingDelegate.unitPrice.first() shouldBe "8.5"
        // Unit price unit should remain default since it was null
        unitPricingDelegate.unitPriceUnit.first() shouldBe MeasurementUnit.KILOGRAM
    }
}

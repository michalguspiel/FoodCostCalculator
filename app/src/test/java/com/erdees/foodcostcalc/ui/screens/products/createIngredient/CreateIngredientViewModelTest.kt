package com.erdees.foodcostcalc.ui.screens.products.createIngredient

import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.domain.usecase.CreateProductUseCase
import com.erdees.foodcostcalc.ui.screens.products.PackagePriceState
import com.erdees.foodcostcalc.ui.screens.products.UnitPriceState
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@ExperimentalCoroutinesApi
class CreateIngredientViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var preferences: Preferences
    private lateinit var createProductUseCase: CreateProductUseCase
    private lateinit var viewModel: CreateIngredientViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        preferences = mockk(relaxed = true)
        createProductUseCase = mockk(relaxed = true)

        // Setup default preference values
        every { preferences.metricUsed } returns flowOf(true)
        every { preferences.imperialUsed } returns flowOf(false)
        every { preferences.showProductTax } returns MutableStateFlow(true)

        startKoin {
            modules(module {
                single { preferences }
                single { createProductUseCase }
            })
        }
    }

    private fun initializeViewModel() {
        viewModel = CreateIngredientViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
        clearAllMocks()
    }

    @Test
    fun `initial state should be PackagePriceState with default values`() = runTest(testDispatcher) {
        initializeViewModel()
        advanceUntilIdle()

        val initialState = viewModel.uiState.value
        initialState.shouldBeInstanceOf<PackagePriceState>()

        with(initialState) {
            id shouldBe 0L
            name shouldBe ""
            tax shouldBe ""
            waste shouldBe ""
            packagePrice shouldBe ""
            packageQuantity shouldBe ""
            packageUnit shouldBe MeasurementUnit.KILOGRAM
        }
    }

    @Test
    fun `onNameChanged should correctly update name property in state`() = runTest(testDispatcher) {
        initializeViewModel()
        advanceUntilIdle()

        val testName = "Test Ingredient"
        viewModel.onNameChanged(testName)
        advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        updatedState.name shouldBe testName
    }

    @Test
    fun `togglePriceMode should transition from PackagePriceState to UnitPriceState preserving common fields`() = runTest(testDispatcher) {
        initializeViewModel()
        advanceUntilIdle()

        // Set up initial state with some values
        viewModel.onNameChanged("Test Product")
        viewModel.onTaxChanged("10.5")
        viewModel.onWasteChanged("5.0")
        advanceUntilIdle()

        val initialState = viewModel.uiState.value as PackagePriceState
        initialState.name shouldBe "Test Product"
        initialState.tax shouldBe "10.5"
        initialState.waste shouldBe "5.0"

        // Toggle to unit price mode
        viewModel.togglePriceMode()
        advanceUntilIdle()

        val newState = viewModel.uiState.value
        newState.shouldBeInstanceOf<UnitPriceState>()

        with(newState) {
            name shouldBe "Test Product"
            tax shouldBe "10.5"
            waste shouldBe "5.0"
            unitPrice shouldBe ""
            unitPriceUnit shouldBe MeasurementUnit.KILOGRAM
        }
    }

    @Test
    fun `togglePriceMode should transition from UnitPriceState to PackagePriceState preserving common fields`() = runTest(testDispatcher) {
        initializeViewModel()
        advanceUntilIdle()

        // First toggle to unit price mode
        viewModel.togglePriceMode()
        advanceUntilIdle()

        // Set some values in unit price state
        viewModel.onNameChanged("Unit Test Product")
        viewModel.onTaxChanged("15.0")
        viewModel.onWasteChanged("3.0")
        advanceUntilIdle()

        val unitState = viewModel.uiState.value as UnitPriceState
        unitState.name shouldBe "Unit Test Product"

        // Toggle back to package price mode
        viewModel.togglePriceMode()
        advanceUntilIdle()

        val packageState = viewModel.uiState.value
        packageState.shouldBeInstanceOf<PackagePriceState>()

        with(packageState) {
            name shouldBe "Unit Test Product"
            tax shouldBe "15.0"
            waste shouldBe "3.0"
            packagePrice shouldBe ""
            packageQuantity shouldBe ""
            packageUnit shouldBe MeasurementUnit.KILOGRAM
        }
    }

    @Test
    fun `saveIngredient should call repository save method with PackagePriceState`() = runTest(testDispatcher) {
        val mockProduct = mockk<ProductDomain>(relaxed = true)
        every { mockProduct.name } returns "Test Product"
        coEvery { createProductUseCase.invoke(any<PackagePriceState>()) } returns Result.success(mockProduct)

        initializeViewModel()
        advanceUntilIdle()

        // Set up a valid package price state
        viewModel.onNameChanged("Test Product")
        viewModel.onPackagePriceChanged("10.0")
        viewModel.onPackageQuantityChanged("2.0")
        viewModel.onWasteChanged("5.0")
        viewModel.onPackageUnitChanged(MeasurementUnit.KILOGRAM)
        advanceUntilIdle()

        viewModel.saveIngredient()
        advanceUntilIdle()

        coVerify { createProductUseCase.invoke(any<PackagePriceState>()) }
    }

    @Test
    fun `saveIngredient should call repository save method with UnitPriceState`() = runTest(testDispatcher) {
        val mockProduct = mockk<ProductDomain>(relaxed = true)
        every { mockProduct.name } returns "Test Product"
        coEvery { createProductUseCase.invoke(any<UnitPriceState>()) } returns Result.success(mockProduct)

        initializeViewModel()
        advanceUntilIdle()

        // Toggle to unit price mode
        viewModel.togglePriceMode()
        advanceUntilIdle()

        // Set up a valid unit price state
        viewModel.onNameChanged("Test Product")
        viewModel.onUnitPriceChanged("5.0")
        viewModel.onWasteChanged("3.0")
        viewModel.onUnitPriceUnitChanged(MeasurementUnit.GRAM)
        advanceUntilIdle()

        viewModel.saveIngredient()
        advanceUntilIdle()

        coVerify { createProductUseCase.invoke(any<UnitPriceState>()) }
    }

    @Test
    fun `isSaveButtonEnabled should be false when required fields are missing in PackagePriceState`() = runTest(testDispatcher) {
        every { preferences.showProductTax } returns MutableStateFlow(false)
        initializeViewModel()
        advanceUntilIdle()

        // Initial state should have save button disabled
        viewModel.isSaveButtonEnabled.first() shouldBe false

        // Set name only
        viewModel.onNameChanged("Test Product")
        advanceUntilIdle()
        viewModel.isSaveButtonEnabled.first() shouldBe false

        // Add price but still missing quantity
        viewModel.onPackagePriceChanged("10.0")
        advanceUntilIdle()
        viewModel.isSaveButtonEnabled.first() shouldBe false

        // Add quantity as 0 so still disabled
        viewModel.onPackageQuantityChanged("0")
        advanceUntilIdle()
        viewModel.isSaveButtonEnabled.first() shouldBe false

        // Set proper quantity and enable save button
        viewModel.onPackageQuantityChanged("2.0")
        advanceUntilIdle()
        viewModel.isSaveButtonEnabled.first() shouldBe true
    }

    @Test
    fun `isSaveButtonEnabled should be true when all required fields are filled in PackagePriceState`() = runTest(testDispatcher) {
        every { preferences.showProductTax } returns MutableStateFlow(false)
        initializeViewModel()
        advanceUntilIdle()

        viewModel.onNameChanged("Test Product")
        viewModel.onPackagePriceChanged("10.0")
        viewModel.onPackageQuantityChanged("2.0")
        viewModel.onWasteChanged("5.0")
        advanceUntilIdle()

        viewModel.isSaveButtonEnabled.first() shouldBe true
    }

    @Test
    fun `isSaveButtonEnabled should be true when all required fields are filled in UnitPriceState`() = runTest(testDispatcher) {
        every { preferences.showProductTax } returns MutableStateFlow(false)
        initializeViewModel()
        advanceUntilIdle()

        // Toggle to unit price mode
        viewModel.togglePriceMode()
        advanceUntilIdle()

        viewModel.onNameChanged("Test Product")
        viewModel.onUnitPriceChanged("5.0")
        viewModel.onWasteChanged("3.0")
        advanceUntilIdle()

        viewModel.isSaveButtonEnabled.first() shouldBe true
    }

    @Test
    fun `getCalculatedUnitPrice should return correct calculation for PackagePriceState`() = runTest(testDispatcher) {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.onPackagePriceChanged("10.0")
        viewModel.onPackageQuantityChanged("2.0")
        advanceUntilIdle()

        val calculatedPrice = viewModel.getCalculatedUnitPrice()
        calculatedPrice shouldBe "5"
    }

    @Test
    fun `getCalculatedUnitPrice should return null for invalid inputs in PackagePriceState`() = runTest(testDispatcher) {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.onPackagePriceChanged("10.0")
        viewModel.onPackageQuantityChanged("0") // Division by zero
        advanceUntilIdle()

        val calculatedPrice = viewModel.getCalculatedUnitPrice()
        calculatedPrice shouldBe null
    }

    @Test
    fun `getCalculatedUnitPrice should return null for UnitPriceState`() = runTest(testDispatcher) {
        initializeViewModel()
        advanceUntilIdle()

        // Toggle to unit price mode
        viewModel.togglePriceMode()
        advanceUntilIdle()

        val calculatedPrice = viewModel.getCalculatedUnitPrice()
        calculatedPrice shouldBe null
    }

    @Test
    fun `calculateWaste should update waste field with correct percentage`() = runTest(testDispatcher) {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.calculateWaste(totalQuantity = 100.0, wasteQuantity = 5.0)
        advanceUntilIdle()

        viewModel.uiState.value.waste shouldBe "5"
    }

    @Test
    fun `tax field should be set to 0_0 when showTaxPercent is false`() = runTest(testDispatcher) {
        every { preferences.showProductTax } returns MutableStateFlow(false)
        initializeViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.tax shouldBe "0.0"
    }
}

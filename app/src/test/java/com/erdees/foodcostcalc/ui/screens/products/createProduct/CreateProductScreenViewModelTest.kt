package com.erdees.foodcostcalc.ui.screens.products.createProduct

import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
class CreateProductScreenViewModelTestJUnitStyleWithMockK {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var productRepository: ProductRepository
    private lateinit var preferences: Preferences
    private lateinit var analyticsRepository: AnalyticsRepository

    private lateinit var viewModel: CreateProductScreenViewModel

    private val productBaseSlot = slot<ProductBase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        productRepository = mockk(relaxed = true)
        preferences = mockk()
        analyticsRepository = mockk(relaxed = true)

        every { preferences.metricUsed } returns flowOf(true)
        every { preferences.imperialUsed } returns flowOf(false)
        every { preferences.showHalfProducts } returns flowOf(false)
        every { preferences.defaultMargin } returns flowOf("10")
        every { preferences.defaultTax } returns flowOf("10")
        every { preferences.defaultCurrencyCode } returns flowOf("USD")

        startKoin {
            modules(module {
                single { productRepository }
                single { preferences }
                single { analyticsRepository }
            })
        }
    }

    private fun initializeViewModel() {
        viewModel = CreateProductScreenViewModel()
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
        clearAllMocks()
    }

    @Test
    fun `productTax initial value is 0_0 when showTaxPercent is false`() = runTest(testDispatcher) {
        every { preferences.showProductTax } returns MutableStateFlow(false)
        initializeViewModel()
        advanceUntilIdle()
        viewModel.productTax.value shouldBe "0.0"
    }

    @Test
    fun `updateProductTax does not update when showTaxPercent is false`() =
        runTest(testDispatcher) {
            every { preferences.showProductTax } returns MutableStateFlow(false)
            initializeViewModel()
            advanceUntilIdle()

            viewModel.updateProductTax("10.0")
            advanceUntilIdle()
            viewModel.productTax.value shouldBe "0.0"
        }

    @Test
    fun `addButtonEnabled is true when showTaxPercent is false and other fields valid`() =
        runTest(testDispatcher) {
            every { preferences.showProductTax } returns MutableStateFlow(false)
            initializeViewModel()
            advanceUntilIdle()

            viewModel.updateProductName("Test Product")
            viewModel.updateProductPrice("10.0")
            viewModel.updateProductWaste("5.0")
            viewModel.selectUnit("kg")
            advanceUntilIdle()

            viewModel.addButtonEnabled.value shouldBe true
        }

    @Test
    fun `addButtonEnabled is false when showTaxPercent is false and price is missing`() =
        runTest(testDispatcher) {
            every { preferences.showProductTax } returns MutableStateFlow(false)
            initializeViewModel()
            advanceUntilIdle()

            viewModel.updateProductName("Test Product")
            viewModel.updateProductWaste("5.0")
            viewModel.selectUnit("kg")
            advanceUntilIdle()

            viewModel.addButtonEnabled.value shouldBe false
        }

    @Test
    fun `addButtonEnabled when showTaxPercent is true and all fields valid`() = runTest(testDispatcher) {
        every { preferences.showProductTax } returns MutableStateFlow(true)
        initializeViewModel()
        advanceUntilIdle()
        viewModel.updateProductName("Test Product")
        viewModel.updateProductPrice("10.0")
        viewModel.updateProductTax("20.0")
        viewModel.updateProductWaste("5.0")
        viewModel.selectUnit("kg")
        advanceUntilIdle()

        viewModel.addButtonEnabled.value shouldBe true
    }

    @Test
    fun `addButtonEnabled when showTaxPercent is true and tax is missing`() =
        runTest(testDispatcher) {
            every { preferences.showProductTax } returns MutableStateFlow(true)
            initializeViewModel()
            advanceUntilIdle()

            viewModel.updateProductName("Test Product")
            viewModel.updateProductPrice("10.0")
            viewModel.updateProductWaste("5.0")
            viewModel.selectUnit("kg")
            viewModel.updateProductTax("")
            advanceUntilIdle()

            viewModel.addButtonEnabled.first() shouldBe false
        }

    @Test
    fun `updateProductTax updates when showTaxPercent is true`() = runTest(testDispatcher) {
        every { preferences.showProductTax } returns MutableStateFlow(true)
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateProductTax("12.5")
        advanceUntilIdle()
        viewModel.productTax.value shouldBe "12.5"
    }

    @Test
    fun `addProduct when showTaxPercent is true uses entered tax value`() =
        runTest(testDispatcher) {
            every { preferences.showProductTax } returns MutableStateFlow(true)
            coEvery { productRepository.addProduct(any()) } returns Unit

            initializeViewModel()
            advanceUntilIdle()

            viewModel.updateProductName("Test Product")
            viewModel.updateProductPrice("10.0")
            viewModel.updateProductTax("15.0")
            viewModel.updateProductWaste("5.0")
            viewModel.selectUnit("kg")
            advanceUntilIdle()

            viewModel.addProduct()
            advanceUntilIdle()

            coVerify { productRepository.addProduct(capture(productBaseSlot)) }
            val capturedProduct = productBaseSlot.captured
            capturedProduct.name shouldBe "Test Product"
            capturedProduct.pricePerUnit shouldBe 10.0
            capturedProduct.tax shouldBe 15.0
            capturedProduct.waste shouldBe 5.0
            capturedProduct.unit shouldBe "kg"

            coVerify(exactly = 1) {
                analyticsRepository.logEvent(
                    any(),
                    any()
                )
            }
        }

    @Test
    fun `addProduct when showTaxPercent is false uses 0 as tax`() =
        runTest(StandardTestDispatcher()) {
            every { preferences.showProductTax } returns MutableStateFlow(false)
            coEvery { productRepository.addProduct(any()) } returns Unit

            initializeViewModel()
            advanceUntilIdle()

            viewModel.updateProductName("Test Product")
            viewModel.updateProductPrice("10.0")
            viewModel.updateProductWaste("5.0")
            viewModel.selectUnit("kg")
            advanceUntilIdle()
            viewModel.addProduct()
            advanceUntilIdle()

            coVerify { productRepository.addProduct(capture(productBaseSlot)) }
            val capturedProduct = productBaseSlot.captured
            capturedProduct.name shouldBe "Test Product"
            capturedProduct.pricePerUnit shouldBe 10.0
            capturedProduct.tax shouldBe 0.0
            capturedProduct.waste shouldBe 5.0
            capturedProduct.unit shouldBe "kg"

            coVerify(exactly = 1) {
                analyticsRepository.logEvent(
                    any(),
                    any()
                )
            }
        }
}
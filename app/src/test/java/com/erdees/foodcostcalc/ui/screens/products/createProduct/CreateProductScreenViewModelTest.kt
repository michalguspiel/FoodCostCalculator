package com.erdees.foodcostcalc.ui.screens.products.createProduct

import android.app.Application
import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class CreateProductScreenViewModelTest : KoinTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // For LiveData if used, good practice

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var preferences: Preferences

    @Mock
    private lateinit var analyticsRepository: AnalyticsRepository

    @Mock
    private lateinit var application: Application // For Koin by inject()

    @Mock
    private lateinit var resources: Resources // For getUnits()

    private lateinit var viewModel: CreateProductScreenViewModel

    @Captor
    private lateinit var productBaseCaptor: ArgumentCaptor<ProductBase>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Default mock behavior for preferences that are always accessed
        `when`(preferences.metricUsed).thenReturn(flowOf(true))
        `when`(preferences.imperialUsed).thenReturn(flowOf(false))
        `when`(preferences.showHalfProducts).thenReturn(flowOf(false)) // Default, adjust if test needs
        `when`(preferences.defaultMargin).thenReturn(flowOf("10"))
        `when`(preferences.defaultTax).thenReturn(flowOf("10"))
        `when`(preferences.defaultCurrencyCode).thenReturn(flowOf("USD"))


        startKoin {
            modules(module {
                single { productRepository }
                single { preferences }
                single { analyticsRepository }
                single { application } // Koin needs Application for by inject()
            })
        }
    }

    private fun initializeViewModel() {
        viewModel = CreateProductScreenViewModel()
        // Call getUnits as it's called in init/LaunchedEffect in some viewmodels
        viewModel.getUnits(resources)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    // --- Test Cases ---

    @Test
    fun `test productTax initial value is 0_0 when showTaxPercent is false`() = runTest(testDispatcher) {
        `when`(preferences.showTaxPercent).thenReturn(MutableStateFlow(false))
        initializeViewModel()
        advanceUntilIdle() // Allow collect in init to complete
        assertEquals("0.0", viewModel.productTax.value)
    }

    @Test
    fun `test updateProductTax does not update when showTaxPercent is false`() = runTest(testDispatcher) {
        `when`(preferences.showTaxPercent).thenReturn(MutableStateFlow(false))
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateProductTax("10.0")
        advanceUntilIdle()
        assertEquals("0.0", viewModel.productTax.value)
    }

    @Test
    fun `test addButtonEnabled when showTaxPercent is false and other fields valid`() = runTest(testDispatcher) {
        `when`(preferences.showTaxPercent).thenReturn(MutableStateFlow(false))
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateProductName("Test Product")
        viewModel.updateProductPrice("10.0")
        viewModel.updateProductWaste("5.0")
        viewModel.selectUnit("kg")
        advanceUntilIdle()

        assertTrue("Add button should be enabled", viewModel.addButtonEnabled.value)
    }

    @Test
    fun `test addButtonEnabled when showTaxPercent is false and price is missing`() = runTest(testDispatcher) {
        `when`(preferences.showTaxPercent).thenReturn(MutableStateFlow(false))
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateProductName("Test Product")
        // viewModel.updateProductPrice("10.0") // Price is missing
        viewModel.updateProductWaste("5.0")
        viewModel.selectUnit("kg")
        advanceUntilIdle()

        assertFalse("Add button should be disabled if price is missing", viewModel.addButtonEnabled.value)
    }


    @Test
    fun `test updateProductTax updates when showTaxPercent is true`() = runTest(testDispatcher) {
        `when`(preferences.showTaxPercent).thenReturn(MutableStateFlow(true))
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateProductTax("12.5")
        advanceUntilIdle()
        assertEquals("12.5", viewModel.productTax.value)
    }

    @Test
    fun `test addButtonEnabled when showTaxPercent is true and all fields valid`() = runTest(testDispatcher) {
        `when`(preferences.showTaxPercent).thenReturn(MutableStateFlow(true))
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateProductName("Test Product")
        viewModel.updateProductPrice("10.0")
        viewModel.updateProductTax("20.0")
        viewModel.updateProductWaste("5.0")
        viewModel.selectUnit("kg")
        advanceUntilIdle()

        assertTrue("Add button should be enabled", viewModel.addButtonEnabled.value)
    }

    @Test
    fun `test addButtonEnabled when showTaxPercent is true and tax is missing`() = runTest(testDispatcher) {
        `when`(preferences.showTaxPercent).thenReturn(MutableStateFlow(true))
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateProductName("Test Product")
        viewModel.updateProductPrice("10.0")
        // viewModel.updateProductTax("20.0") // Tax is missing
        viewModel.updateProductWaste("5.0")
        viewModel.selectUnit("kg")
        advanceUntilIdle()

        assertFalse("Add button should be disabled if tax is missing and showTaxPercent is true", viewModel.addButtonEnabled.value)
    }

    @Test
    fun `test addProduct when showTaxPercent is true uses entered tax value`() = runTest(testDispatcher) {
        `when`(preferences.showTaxPercent).thenReturn(MutableStateFlow(true))
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

        verify(productRepository).addProduct(productBaseCaptor.capture())
        val capturedProduct = productBaseCaptor.value
        assertEquals("Test Product", capturedProduct.name)
        assertEquals(10.0, capturedProduct.pricePerUnit, 0.001)
        assertEquals(15.0, capturedProduct.tax, 0.001)
        assertEquals(5.0, capturedProduct.waste, 0.001)
        assertEquals("kg", capturedProduct.unit)
        verify(analyticsRepository).logEvent(any(), any())
    }

    @Test
    fun `test addProduct when showTaxPercent is false uses zero tax`() = runTest(testDispatcher) {
        val showTaxFlow = MutableStateFlow(true)
        `when`(preferences.showTaxPercent).thenReturn(showTaxFlow)
        initializeViewModel()
        advanceUntilIdle()

        // Initially set tax while showTaxPercent is true
        viewModel.updateProductTax("25.0")
        advanceUntilIdle()
        assertEquals("25.0", viewModel.productTax.value) // Ensure it was set

        // Now, set showTaxPercent to false
        showTaxFlow.value = false
        advanceUntilIdle() // Allow collection and reset of productTax to "0.0"

        assertEquals("0.0", viewModel.productTax.value) // Verify it reset

        viewModel.updateProductName("Test Product Zero Tax")
        viewModel.updateProductPrice("100.0")
        // productTax is already "0.0" due to the flow change
        viewModel.updateProductWaste("2.0")
        viewModel.selectUnit("pcs")
        advanceUntilIdle()

        assertTrue("Add button should be enabled", viewModel.addButtonEnabled.value)

        viewModel.addProduct()
        advanceUntilIdle()

        verify(productRepository).addProduct(productBaseCaptor.capture())
        val capturedProduct = productBaseCaptor.value
        assertEquals("Test Product Zero Tax", capturedProduct.name)
        assertEquals(100.0, capturedProduct.pricePerUnit, 0.001)
        assertEquals(0.0, capturedProduct.tax, 0.001) // Crucial check
        assertEquals(2.0, capturedProduct.waste, 0.001)
        assertEquals("pcs", capturedProduct.unit)
        verify(analyticsRepository).logEvent(any(), any())
    }
}

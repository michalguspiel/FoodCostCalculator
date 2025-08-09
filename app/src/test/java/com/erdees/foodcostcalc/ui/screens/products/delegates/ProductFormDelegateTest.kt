package com.erdees.foodcostcalc.ui.screens.products.delegates

import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.utils.Utils
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ProductFormDelegateTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var preferences: Preferences
    private lateinit var delegate: ProductFormDelegate

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preferences = mockk(relaxed = true)

        // Setup default preferences
        every { preferences.metricUsed } returns flowOf(true)
        every { preferences.imperialUsed } returns flowOf(false)
        every { preferences.showProductTax } returns MutableStateFlow(true)
    }

    private fun TestScope.initializeDelegate() {
        delegate = ProductFormDelegate(preferences, testScope)
        advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have default values`() = runTest {
        initializeDelegate()

        delegate.name.first() shouldBe ""
        delegate.tax.first() shouldBe ""
        delegate.waste.first() shouldBe ""
        delegate.inputMethod.first() shouldBe InputMethod.PACKAGE
        delegate.showTaxField.first() shouldBe true
    }

    @Test
    fun `initial state should set tax to 0_0 when showProductTax is false`() = runTest {
        every { preferences.showProductTax } returns MutableStateFlow(false)

        initializeDelegate()

        delegate.tax.first() shouldBe "0.0"
    }

    @Test
    fun `updateName should update name state`() = runTest {
        initializeDelegate()

        delegate.updateName("Test Product")

        delegate.name.first() shouldBe "Test Product"
    }

    @Test
    fun `updateTax should update tax state`() = runTest {
        initializeDelegate()

        delegate.updateTax("15.5")

        delegate.tax.first() shouldBe "15.5"
    }

    @Test
    fun `updateWaste should update waste state`() = runTest {
        initializeDelegate()

        delegate.updateWaste("10.0")

        delegate.waste.first() shouldBe "10.0"
    }

    @Test
    fun `toggleInputMethod should toggle between PACKAGE and UNIT`() = runTest {
        initializeDelegate()

        // Initially PACKAGE
        delegate.inputMethod.first() shouldBe InputMethod.PACKAGE

        delegate.toggleInputMethod()
        delegate.inputMethod.first() shouldBe InputMethod.UNIT

        delegate.toggleInputMethod()
        delegate.inputMethod.first() shouldBe InputMethod.PACKAGE
    }

    @Test
    fun `setInputMethod should set specific input method`() = runTest {
        initializeDelegate()

        delegate.setInputMethod(InputMethod.UNIT)
        delegate.inputMethod.first() shouldBe InputMethod.UNIT

        delegate.setInputMethod(InputMethod.PACKAGE)
        delegate.inputMethod.first() shouldBe InputMethod.PACKAGE
    }

    @Test
    fun `reset should reset all fields to defaults when tax field is shown`() = runTest {
        every { preferences.showProductTax } returns MutableStateFlow(true)
        initializeDelegate()

        // Set some values
        delegate.updateName("Test Product")
        delegate.updateTax("15.5")
        delegate.updateWaste("10.0")
        delegate.setInputMethod(InputMethod.UNIT)
        advanceUntilIdle()

        // Reset
        delegate.reset()

        delegate.name.first() shouldBe ""
        delegate.tax.first() shouldBe ""
        delegate.waste.first() shouldBe ""
        delegate.inputMethod.first() shouldBe InputMethod.PACKAGE
    }

    @Test
    fun `reset should set tax to 0_0 when tax field is hidden`() = runTest {
        every { preferences.showProductTax } returns MutableStateFlow(false)
        initializeDelegate()

        // Set some values
        delegate.updateName("Test Product")
        delegate.updateWaste("10.0")
        advanceUntilIdle()

        // Reset
        delegate.reset()

        delegate.name.first() shouldBe ""
        delegate.tax.first() shouldBe "0.0"
        delegate.waste.first() shouldBe ""
    }

    @Test
    fun `units should be loaded from preferences`() = runTest {
        initializeDelegate()

        val units = delegate.units.first()
        val expectedUnits = Utils.getCompleteUnitsSet(true, false)
        units shouldBe expectedUnits

        verify { preferences.metricUsed }
        verify { preferences.imperialUsed }
    }

    @Test
    fun `createBaseValidation should return false when name is blank`() = runTest {
        initializeDelegate()

        val validation = delegate.createBaseValidation()
        validation.first() // collect first value to initialize
        delegate.updateTax("10.0")
        advanceUntilIdle()
        validation.first() shouldBe false
    }

    @Test
    fun `createBaseValidation should return false when tax is invalid and tax field is shown`() = runTest {
        every { preferences.showProductTax } returns MutableStateFlow(true)
        initializeDelegate()

        val validation = delegate.createBaseValidation()
        validation.first() // collect first value to initialize
        delegate.updateName("Test Product")
        delegate.updateTax("invalid")
        advanceUntilIdle()

        validation.first() shouldBe false
    }

    @Test
    fun `createBaseValidation should return true when name is valid and tax field is hidden`() = runTest {
        every { preferences.showProductTax } returns MutableStateFlow(false)
        initializeDelegate()

        val validation = delegate.createBaseValidation()
        validation.first() // collect first value to initialize
        delegate.updateName("Test Product")
        advanceUntilIdle()
        validation.first() shouldBe true
    }

    @Test
    fun `createBaseValidation should return true when all required fields are valid`() = runTest {
        every { preferences.showProductTax } returns MutableStateFlow(true)
        initializeDelegate()
        val validation = delegate.createBaseValidation()
        validation.first() // collect first value to initialize
        delegate.updateName("Test Product")
        delegate.updateTax("10.0")

        advanceUntilIdle()
        validation.first() shouldBe true
    }

    @Test
    fun `tax should be set to 0_0 when showProductTax changes from true to false`() = runTest {
        val showTaxFlow = MutableStateFlow(true)
        every { preferences.showProductTax } returns showTaxFlow

        initializeDelegate()

        delegate.tax.first() shouldBe ""

        showTaxFlow.value = false
        advanceUntilIdle()

        delegate.tax.first() shouldBe "0.0"
    }
}

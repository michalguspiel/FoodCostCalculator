package com.erdees.foodcostcalc.ui.screens.dishes.forms

import com.erdees.foodcostcalc.data.model.local.HalfProductBase
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.model.local.joined.CompleteHalfProduct
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentLookupViewModel
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@ExperimentalCoroutinesApi
class ComponentLookupFormViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private val mockProductRepository: ProductRepository = mockk(relaxed = true)
    private val mockHalfProductRepository: HalfProductRepository = mockk(relaxed = true)
    private val mockAnalyticsRepository: AnalyticsRepository = mockk(relaxed = true)

    private val testModule = module {
        single<ProductRepository> { mockProductRepository }
        single<HalfProductRepository> { mockHalfProductRepository }
        single<AnalyticsRepository> { mockAnalyticsRepository }
    }

    private lateinit var viewModel: ComponentLookupViewModel

    private suspend fun TestScope.initializeViewModel() {
        viewModel = ComponentLookupViewModel()
        // Subscribe to all StateFlows to trigger lazy initialization
        viewModel.newComponentName.first()
        viewModel.selectedComponent.first()
        viewModel.suggestedComponents.first()
        viewModel.shouldShowSuggestedProducts.first()
        viewModel.uiState.first()
        advanceUntilIdle()
    }

    // Test Data
    private val testProduct1 = ProductBase(
        productId = 1L,
        name = "Chicken Breast",
        tax = 10.0,
        unit = "per kilogram",
        pricePerUnit = 15.0,
        waste = 5.0
    )

    private val testProduct2 = ProductBase(
        productId = 2L,
        name = "Chicken Thigh",
        pricePerUnit = 12.0,
        tax = 10.0,
        waste = 3.0,
        unit = "per kilogram"
    )

    private val testProduct3 = ProductBase(
        productId = 3L,
        name = "Beef Steak",
        pricePerUnit = 25.0,
        tax = 10.0,
        waste = 2.0,
        unit = "per kilogram"
    )

    private val testHalfProduct1 = CompleteHalfProduct(
        halfProductBase = HalfProductBase(
            halfProductId = 1L,
            name = "Chicken Stock",
            halfProductUnit = "liter"
        ),
        products = emptyList()
    )

    private val testHalfProduct2 = CompleteHalfProduct(
        halfProductBase = HalfProductBase(
            halfProductId = 2L,
            name = "Chicken Marinade",
            halfProductUnit = "liter"
        ),
        products = emptyList()
    )

    private val testProducts = listOf(testProduct1, testProduct2, testProduct3)
    private val testHalfProducts = listOf(testHalfProduct1, testHalfProduct2)

    @Before
    fun setup() {
        startKoin {
            modules(testModule)
        }
        Dispatchers.setMain(testDispatcher)

        // Setup default mock behavior
        coEvery { mockProductRepository.products } returns flowOf(testProducts)
        coEvery { mockHalfProductRepository.completeHalfProducts } returns flowOf(testHalfProducts)
        coEvery { mockAnalyticsRepository.logEvent(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `initial state should have empty component name and no selected component`() = runTest {
        initializeViewModel()

        viewModel.newComponentName.first() shouldBe ""
        viewModel.selectedComponent.first() shouldBe null
    }

    @Test
    fun `updateNewComponentName should update component name state`() = runTest {
        initializeViewModel()

        viewModel.updateNewComponentName("Chicken")
        advanceUntilIdle()

        viewModel.newComponentName.first() shouldBe "Chicken"
    }

    @Test
    fun `updateNewComponentName should clear selected component when name changes`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        // First select a component
        viewModel.onComponentSelected(testProduct1.toProductDomain())
        advanceUntilIdle()

        // Then change the name
        viewModel.updateNewComponentName("Different Name")
        advanceUntilIdle()

        viewModel.selectedComponent.first() shouldBe null
    }

    @Test
    fun `suggestedComponents should filter products by search term`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateNewComponentName("chicken")
        advanceUntilIdle()

        val suggestions = viewModel.suggestedComponents.first()
        suggestions.products.size shouldBe 2
        suggestions.products.all { it.name.lowercase().contains("chicken") } shouldBe true
    }

    @Test
    fun `suggestedComponents should filter half products by search term`() = runTest {
        initializeViewModel()

        viewModel.updateNewComponentName("stock")
        advanceUntilIdle()

        val suggestions = viewModel.suggestedComponents.first()
        suggestions.halfProducts.size shouldBe 1
        suggestions.halfProducts.first().name shouldBe "Chicken Stock"
    }

    @Test
    fun `suggestedComponents should be case insensitive`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateNewComponentName("CHICKEN")
        advanceUntilIdle()

        val suggestions = viewModel.suggestedComponents.first()
        suggestions.products.size shouldBe 2
    }

    @Test
    fun `shouldShowSuggestedProducts should be true when conditions are met`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateNewComponentName("chicken")
        advanceUntilIdle()

        viewModel.shouldShowSuggestedProducts.first() shouldBe true
    }

    @Test
    fun `shouldShowSuggestedProducts should be false for short search terms`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateNewComponentName("ch")
        advanceUntilIdle()

        viewModel.shouldShowSuggestedProducts.first() shouldBe false
    }

    @Test
    fun `shouldShowSuggestedProducts should be false when component is selected`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateNewComponentName("chicken")
        advanceUntilIdle()

        viewModel.onComponentSelected(testProduct1.toProductDomain())
        advanceUntilIdle()

        viewModel.shouldShowSuggestedProducts.first() shouldBe false
    }

    @Test
    fun `onComponentSelected should update selected component and name`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        val productDomain = testProduct1.toProductDomain()
        viewModel.onComponentSelected(productDomain)
        advanceUntilIdle()

        viewModel.selectedComponent.first() shouldBe productDomain
        viewModel.newComponentName.first() shouldBe productDomain.name
    }

    @Test
    fun `onComponentSelected should log analytics event`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.onComponentSelected(testProduct1.toProductDomain())
        advanceUntilIdle()

        verify { mockAnalyticsRepository.logEvent(any(), any()) }
    }

    @Test
    fun `getComponentSelectionResult should return ExistingComponent when component is selected`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        val productDomain = testProduct1.toProductDomain()
        viewModel.onComponentSelected(productDomain)
        advanceUntilIdle()

        val result = viewModel.getComponentSelectionResult()
        result.shouldBeInstanceOf<ComponentSelection.ExistingComponent>()
        result.item shouldBe productDomain
    }

    @Test
    fun `getComponentSelectionResult should return NewComponent when no component is selected`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateNewComponentName("New Component")
        advanceUntilIdle()

        val result = viewModel.getComponentSelectionResult()
        result.shouldBeInstanceOf<ComponentSelection.NewComponent>()
        result.name shouldBe "New Component"
    }

    @Test
    fun `updateSelectedComponent should match exact product name`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateNewComponentName(testProduct1.name)
        advanceUntilIdle()

        val selectedComponent = viewModel.selectedComponent.first()
        selectedComponent shouldBe testProduct1.toProductDomain()
    }

    @Test
    fun `updateSelectedComponent should match exact half product name`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateNewComponentName(testHalfProduct1.halfProductBase.name)
        advanceUntilIdle()

        val selectedComponent = viewModel.selectedComponent.first()
        selectedComponent.shouldBeInstanceOf<HalfProductDomain>()
        selectedComponent.name shouldBe testHalfProduct1.halfProductBase.name
    }

    @Test
    fun `reset should clear name and selected component`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        // Set some state
        viewModel.updateNewComponentName("Test")
        viewModel.onComponentSelected(testProduct1.toProductDomain())
        advanceUntilIdle()

        // Reset
        viewModel.reset()
        advanceUntilIdle()

        viewModel.newComponentName.first() shouldBe ""
        viewModel.selectedComponent.first() shouldBe null
    }

    @Test
    fun `uiState should combine all state correctly`() = runTest {
        initializeViewModel()
        advanceUntilIdle()

        viewModel.updateNewComponentName("chicken")
        advanceUntilIdle()

        val uiState = viewModel.uiState.first()
        uiState.newComponentName shouldBe "chicken"
        uiState.showSuggestedComponents shouldBe true
        uiState.suggestedComponents.products.size shouldBe 2
        uiState.selectedComponent shouldBe null
    }
}
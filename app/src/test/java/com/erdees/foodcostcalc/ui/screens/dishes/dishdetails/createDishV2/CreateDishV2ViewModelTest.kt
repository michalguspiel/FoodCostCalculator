package com.erdees.foodcostcalc.ui.screens.dishes.dishdetails.createDishV2

import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.onboarding.OnboardingState
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.CreateDishV2ViewModel
import com.erdees.foodcostcalc.utils.MyDispatchers
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class CreateDishV2ViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val mockDishRepository: DishRepository = mockk(relaxed = true)
    private val mockProductRepository: ProductRepository = mockk(relaxed = true)
    private val mockPreferences: Preferences = mockk(relaxed = true)
    private val mockAnalyticsRepository: AnalyticsRepository = mockk(relaxed = true)
    private val myDispatchers: MyDispatchers = mockk(relaxed = true)

    private val testModule = module {
        single { mockDishRepository }
        single { mockProductRepository }
        single { mockPreferences }
        single { mockAnalyticsRepository }
        single { myDispatchers }
    }

    private lateinit var viewModel: CreateDishV2ViewModel

    @Before
    fun setup() {
        startKoin {
            modules(testModule)
        }
        Dispatchers.setMain(testDispatcher)
        every { myDispatchers.ioDispatcher }.returns(testDispatcher)
        coEvery { mockPreferences.hasPromptedDefaultSettings } returns flowOf(false)
        coEvery { mockPreferences.onboardingState } returns flowOf(OnboardingState.FINISHED)
        coEvery { mockDishRepository.dishes } returns flowOf(emptyList())
        coEvery { mockProductRepository.products } returns flowOf(emptyList())
        coEvery { mockPreferences.defaultMargin } returns flowOf("100")
        coEvery { mockPreferences.defaultTax } returns flowOf("10")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `onSaveDishClick calls showSetAsDefaultSettingsPrompt when not prompted before`() = runTest {
        coEvery { mockPreferences.hasPromptedDefaultSettings } returns flowOf(false)
        viewModel = CreateDishV2ViewModel()
        advanceUntilIdle()

        viewModel.onSaveDishClick()
        advanceUntilIdle()

        val screenState = viewModel.screenState.first()
        screenState.shouldBeInstanceOf<ScreenState.Interaction>()
        screenState.interaction.shouldBeInstanceOf<InteractionType.SaveDefaultSettings>()
    }

    @Test
    fun `onSaveDishClick calls onSaveDish when already prompted`() = runTest {
        coEvery { mockPreferences.hasPromptedDefaultSettings } returns flowOf(true)
        coEvery { mockDishRepository.addDish(any()) } returns 1L

        viewModel = CreateDishV2ViewModel()
        viewModel.updateDishName("Test Dish")
        viewModel.updateMarginPercentInput("150")
        viewModel.updateTaxPercentInput("20")
        advanceUntilIdle()

        viewModel.onSaveDishClick()
        advanceUntilIdle()

        val screenState = viewModel.screenState.first()
        screenState.shouldNotBeInstanceOf<ScreenState.Interaction>()
        viewModel.saveDishSuccess.first() shouldBe 1L
    }
}
package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.icu.util.Currency
import androidx.lifecycle.SavedStateHandle
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish
import com.erdees.foodcostcalc.data.model.local.joined.CompleteDish
import com.erdees.foodcostcalc.data.model.local.joined.ProductAndProductDish
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishDomain
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.utils.MyDispatchers
import com.erdees.foodcostcalc.utils.Utils
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
class DishDetailsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private val mockDishRepository: DishRepository = mockk()
    private val mockPreferences: Preferences = mockk()
    private val mockAnalyticsRepository: AnalyticsRepository = mockk()
    private val myDispatchers: MyDispatchers = mockk()
    private val androidCurrency: Currency = mockk()

    private val testModule = module {
        single<DishRepository> { mockDishRepository }
        single<Preferences> { mockPreferences }
        single<AnalyticsRepository> { mockAnalyticsRepository }
        single<MyDispatchers> { myDispatchers }
    }

    private lateinit var viewModel: DishDetailsViewModel

    private lateinit var savedStateHandle: SavedStateHandle

    // TestData
    private val testDishId = 50L
    private val testDishName = "TestDish"
    private val testDish = createDishModel()

    /**
     * Created dish with a food cost of 20, single product,
     * total price of 44.0 (40.0 (food cost + 200% margin) + 10% tax).
     * */
    private fun createDishModel(dishTax: Double = 10.0, productPrice: Double = 10.0): CompleteDish {
        return CompleteDish(
            dish = DishBase(testDishId, testDishName, 200.0, dishTax, null),
            recipe = null,
            products = listOf(
                ProductAndProductDish(
                    productDish = ProductDish(0L, 0L, testDishId, 1.0, "kilogram"),
                    product = ProductBase(0L, "Broccoli", productPrice, 0.0, 50.0, "per kilogram")
                )
            ),
            halfProducts = emptyList(),
        )
    }

    @Before
    fun setup() {
        startKoin {
            modules(testModule)
        }
        Dispatchers.setMain(testDispatcher)
        every { myDispatchers.ioDispatcher }.returns(testDispatcher)
        savedStateHandle = SavedStateHandle().apply { set("dishId", 1L) }
        every { mockPreferences.currency }.returns(flowOf(androidCurrency))
        every { androidCurrency.currencyCode } returns "EUR"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `updateTotalPrice updates editableTotalPriceState`() = runTest {
        coEvery { mockDishRepository.getDish(any()) }.returns(flowOf(testDish))
        viewModel = DishDetailsViewModel(savedStateHandle)
        println(testDish.toDishDomain().foodCost)
        println(testDish.toDishDomain().products.map { it.foodCost })
        viewModel.updateTotalPrice("123.45")
        viewModel.editableTotalPrice.first() shouldBe "123.45"
    }

    @Test
    fun `setInteraction EditTotalPrice updates state correctly`() = runTest {
        // prepare viewmodel, fetch dish
        coEvery { mockDishRepository.getDish(any()) }.returns(flowOf(testDish))
        viewModel = DishDetailsViewModel(savedStateHandle)
        advanceUntilIdle()

        // act
        viewModel.setInteraction(InteractionType.EditTotalPrice)

        // verify
        val screenState = viewModel.screenState.first()
        screenState::class.java shouldBe ScreenState.Interaction::class.java
        val interaction = (screenState as ScreenState.Interaction).interaction
        interaction shouldBe InteractionType.EditTotalPrice
        val totalPrice = Utils.formatPriceWithoutSymbol(
            testDish.toDishDomain().totalPrice, mockPreferences.currency.first()?.currencyCode
        )
        viewModel.editableTotalPrice.first() shouldBe totalPrice
    }

    @Test
    fun `setInteraction EditTotalPrices updates state correctly for currency with 3 decimal points`() =
        runTest {
            // Tunisian Dinar (TND) – 3 decimal places
            // 1 dinar = 1000 millimes
            every { androidCurrency.currencyCode } returns "TND"

            // prepare viewmodel, fetch dish
            coEvery { mockDishRepository.getDish(any()) }.returns(flowOf(testDish))
            viewModel = DishDetailsViewModel(savedStateHandle)
            advanceUntilIdle()

            // act - set price for one with 3 decimal points
            val newTotalPrice = Utils.formatPriceWithoutSymbol(
                22.123, mockPreferences.currency.first()?.currencyCode
            )
            newTotalPrice shouldBe "22.123"
            viewModel.updateTotalPrice(newTotalPrice)
            viewModel.saveDishTotalPrice()
            // Then set interaction to edit total price
            viewModel.setInteraction(InteractionType.EditTotalPrice)

            // verify
            val screenState = viewModel.screenState.first()
            screenState::class.java shouldBe ScreenState.Interaction::class.java
            val interaction = (screenState as ScreenState.Interaction).interaction
            interaction shouldBe InteractionType.EditTotalPrice
            viewModel.editableTotalPrice.first() shouldBe newTotalPrice
        }

    @Test
    fun `saveDishTotalPrice updates dish margin correctly`() = runTest {
        // prepare viewmodel, fetch dish
        coEvery { mockDishRepository.getDish(any()) }.returns(flowOf(testDish))
        viewModel = DishDetailsViewModel(savedStateHandle)
        advanceUntilIdle()

        // act
        viewModel.setInteraction(InteractionType.EditTotalPrice)
        val newTotalPrice = Utils.formatPriceWithoutSymbol(
            22.0, mockPreferences.currency.first()?.currencyCode
        )
        viewModel.updateTotalPrice(newTotalPrice)
        viewModel.saveDishTotalPrice()

        // verify
        val updatedDishInViewModel = viewModel.dish.first()
        updatedDishInViewModel.shouldNotBeNull()
        updatedDishInViewModel.totalPrice shouldBe 22.0
        updatedDishInViewModel.marginPercent shouldBe 100.0
    }

    @Test
    fun `saveDishTotalPrice updates dish margin to match price`() = runTest {
        // prepare viewmodel, fetch dish

        val pricesToVerify =
            mapOf(
                56.99 to 12.12,
                100.99 to 12.00,
                123.45 to 24.50,
                45.00 to 23.00,
                89.00 to 12.00,
                123.00 to 10.00,
                500.99 to 13.00,
                1000.23 to 8.00,
                5000.69 to 10.0
            )
        pricesToVerify.forEach {
            actAndVerifySetMargin(it.key, it.value)
        }
    }


    private suspend fun TestScope.actAndVerifySetMargin(newPrice: Double, dishTax: Double) {
        coEvery { mockDishRepository.getDish(any()) }.returns(flowOf(createDishModel(dishTax, newPrice / dishTax * 3.66)))
        viewModel = DishDetailsViewModel(savedStateHandle)
        advanceUntilIdle()
        // act
        val newPriceFormatted = Utils.formatPriceWithoutSymbol(
            newPrice, mockPreferences.currency.first()?.currencyCode
        )
        viewModel.updateTotalPrice(newPriceFormatted)
        viewModel.saveDishTotalPrice()

        // verify
        val updatedDishInViewModel = viewModel.dish.first()
        updatedDishInViewModel.shouldNotBeNull()
        println(newPriceFormatted)
        newPriceFormatted shouldBe Utils.formatPriceWithoutSymbol(
            updatedDishInViewModel.totalPrice, mockPreferences.currency.first()?.currencyCode
        )
    }
}

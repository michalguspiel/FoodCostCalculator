package com.erdees.foodcostcalc.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.erdees.foodcostcalc.data.db.dao.dish.DishDao
import com.erdees.foodcostcalc.data.db.dao.dish.HalfProductDishDao
import com.erdees.foodcostcalc.data.db.dao.dish.ProductDishDao
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.Recipe
import com.erdees.foodcostcalc.data.model.local.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish
import com.erdees.foodcostcalc.data.model.local.joined.CompleteDish
import com.erdees.foodcostcalc.utils.TestCoroutineRule
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

@ExperimentalCoroutinesApi
class DishRepositoryTest : KoinTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @RelaxedMockK
    lateinit var dishDao: DishDao

    @RelaxedMockK
    lateinit var productDishDao: ProductDishDao

    @RelaxedMockK
    lateinit var halfProductDishDao: HalfProductDishDao

    private val dishRepository: DishRepository by inject()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        startKoin {
            modules(module {
                single { dishDao }
                single { productDishDao }
                single { halfProductDishDao }
                single<DishRepository> { DishRepositoryImpl() }
            })
        }
    }

    @After
    fun teardown() {
        stopKoin()
        unmockkAll()
    }

    @Test
    fun `dishes should return flow of dishes from dao`() = runTest {
        // Given
        val dishList = listOf(
            CompleteDish(DishBase(1L, "Salad", 10.0, recipeId = null), recipe = null, products = emptyList(), halfProducts = emptyList()),
            CompleteDish(DishBase(2L, "Soup", 12.5, recipeId = null), recipe = null, products = emptyList(), halfProducts = emptyList())
        )
        every { dishDao.getCompleteDishes() } returns flowOf(dishList)

        // When
        val result = dishRepository.dishes.first()

        // Then
        result shouldBe dishList
        verify { dishDao.getCompleteDishes() }
    }

    @Test
    fun `getDish should return dish from dao`() = runTest {
        // Given
        val dishId = 1L
        val completeDish = CompleteDish(DishBase(dishId, "Salad", 10.0, recipeId = null), recipe = null, products = emptyList(), halfProducts = emptyList())
        every { dishDao.getCompleteDish(dishId) } returns flowOf(completeDish)

        // When
        val result = dishRepository.getDish(dishId).first()

        // Then
        result shouldBe completeDish
        verify { dishDao.getCompleteDish(dishId) }
    }

    @Test
    fun `addDish should call dao to add dish`() = runTest {
        // Given
        val dishBase = DishBase(0L, "Pasta", 15.0, recipeId = null) // ID 0 for auto-generation
        // DishDao.addDish returns Unit. The repository is responsible for returning the ID.
        // For this unit test, we'll assume the passed dishBase.dishId would be set by Room or similar,
        // or that the repository uses a different mechanism. Here, we test the interaction.
        // If testing the ID-returning logic of the repo, it might require an integration test or more complex mock.
        coEvery { dishDao.addDish(dishBase) } just runs // Mocking DAO to just run

        // When
        // val resultId = dishRepository.addDish(dishBase) // This would require repo to return ID
        dishRepository.addDish(dishBase) // Calling the repo method.

        // Then
        // resultId shouldBe expectedDishId // Cannot easily test returned ID without more info/complexity
        coVerify { dishDao.addDish(dishBase) }
    }

    @Test
    fun `deleteDish should call dao to delete dish`() = runTest {
        // Given
        val dishId = 1L
        coEvery { dishDao.deleteDish(dishId) } returns Unit

        // When
        dishRepository.deleteDish(dishId)

        // Then
        coVerify { dishDao.deleteDish(dishId) }
    }

    @Test
    fun `updateDish should call dao to update dish`() = runTest {
        // Given
        val dishBase = DishBase(1L, "Updated Salad", 11.0, recipeId = null)
        coEvery { dishDao.editDish(dishBase) } returns Unit // Corrected method name

        // When
        dishRepository.updateDish(dishBase)

        // Then
        coVerify { dishDao.editDish(dishBase) } // Corrected method name
    }

    @Test
    fun `updateDishRecipe should call dao to update dish recipe`() = runTest {
        // Given
        val recipeId = 1L
        val dishId = 1L
        coEvery { dishDao.update(recipeId, dishId) } returns Unit // Corrected method name

        // When
        dishRepository.updateDishRecipe(recipeId, dishId)

        // Then
        coVerify { dishDao.update(recipeId, dishId) } // Corrected method name
    }

    @Test
    fun `deleteProductDish should call dao to delete product dish`() = runTest {
        // Given
        val productDish = ProductDish(1L, 1L, 1L, 100.0, "g")
        coEvery { productDishDao.deleteProductDish(productDish) } returns Unit

        // When
        dishRepository.deleteProductDish(productDish)

        // Then
        coVerify { productDishDao.deleteProductDish(productDish) }
    }

    @Test
    fun `deleteHalfProductDish should call dao to delete half product dish`() = runTest {
        // Given
        val halfProductDish = HalfProductDish(1L, 1L, 1L, 50.0, "g")
        coEvery { halfProductDishDao.delete(halfProductDish) } returns Unit // Corrected method name

        // When
        dishRepository.deleteHalfProductDish(halfProductDish)

        // Then
        coVerify { halfProductDishDao.delete(halfProductDish) } // Corrected method name
    }

    @Test
    fun `updateProductDish should call dao to update product dish`() = runTest {
        // Given
        val productDish = ProductDish(1L, 1L, 1L, 120.0, "g")
        coEvery { productDishDao.updateProductDish(productDish) } returns Unit

        // When
        dishRepository.updateProductDish(productDish)

        // Then
        coVerify { productDishDao.updateProductDish(productDish) }
    }

    @Test
    fun `updateHalfProductDish should call dao to update half product dish`() = runTest {
        // Given
        val halfProductDish = HalfProductDish(1L, 1L, 1L, 60.0, "g")
        coEvery { halfProductDishDao.updateHalfProductDish(halfProductDish) } returns Unit

        // When
        dishRepository.updateHalfProductDish(halfProductDish)

        // Then
        coVerify { halfProductDishDao.updateHalfProductDish(halfProductDish) }
    }
}

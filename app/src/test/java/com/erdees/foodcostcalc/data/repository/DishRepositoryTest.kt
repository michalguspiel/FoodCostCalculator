package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.dish.DishDao
import com.erdees.foodcostcalc.data.db.dao.dish.HalfProductDishDao
import com.erdees.foodcostcalc.data.db.dao.dish.ProductDishDao
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish
import com.erdees.foodcostcalc.data.model.local.joined.CompleteDish
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DishRepositoryTest {

    private val dishDao = mockk<DishDao>(relaxed = true)
    private val productDishDao = mockk<ProductDishDao>(relaxed = true)
    private val halfProductDishDao = mockk<HalfProductDishDao>(relaxed = true)

    // Test implementation that accepts mocked DAOs
    private val testRepository = object : DishRepository {
        override val dishes = dishDao.getCompleteDishes()
        override suspend fun getDish(id: Long) = dishDao.getCompleteDish(id)
        override suspend fun getDishCount() = dishDao.getDishCount()
        override suspend fun addDish(dish: DishBase) = dishDao.addDish(dish)
        override suspend fun deleteDish(dishId: Long) = dishDao.deleteDish(dishId)
        override suspend fun updateDish(dish: DishBase) = dishDao.editDish(dish)
        override suspend fun updateDishRecipe(recipeId: Long, dishId: Long) = dishDao.update(recipeId, dishId)
        override suspend fun deleteProductDish(productDish: ProductDish) = productDishDao.deleteProductDish(productDish)
        override suspend fun deleteHalfProductDish(halfProductDish: HalfProductDish) = halfProductDishDao.delete(halfProductDish)
        override suspend fun updateProductDish(productDish: ProductDish) = productDishDao.updateProductDish(productDish)
        override suspend fun updateHalfProductDish(halfProductDish: HalfProductDish) = halfProductDishDao.updateHalfProductDish(halfProductDish)
    }

    @Test
    fun `dishes should return flow from dao`() = runTest {
        // Given
        val expectedDishes = listOf(
            createTestCompleteDish(1L, "Test Dish 1"),
            createTestCompleteDish(2L, "Test Dish 2")
        )
        every { dishDao.getCompleteDishes() } returns flowOf(expectedDishes)

        // When
        val result = testRepository.dishes.first()

        // Then
        result shouldBe expectedDishes
    }

    @Test
    fun `getDish should return complete dish flow from dao`() = runTest {
        // Given
        val dishId = 1L
        val expectedDish = createTestCompleteDish(dishId, "Test Dish")
        every { dishDao.getCompleteDish(dishId) } returns flowOf(expectedDish)

        // When
        val result = testRepository.getDish(dishId).first()

        // Then
        result shouldBe expectedDish
    }

    @Test
    fun `getDishCount should return count from dao`() = runTest {
        // Given
        val expectedCount = 42
        coEvery { dishDao.getDishCount() } returns expectedCount

        // When
        val result = testRepository.getDishCount()

        // Then
        result shouldBe expectedCount
        coVerify { dishDao.getDishCount() }
    }

    @Test
    fun `addDish should call dao addDish and return result`() = runTest {
        // Given
        val dish = createTestDish(0L, "New Dish")
        val expectedId = 123L
        coEvery { dishDao.addDish(dish) } returns expectedId

        // When
        val result = testRepository.addDish(dish)

        // Then
        result shouldBe expectedId
        coVerify { dishDao.addDish(dish) }
    }

    @Test
    fun `deleteDish should call dao deleteDish`() = runTest {
        // Given
        val dishId = 1L

        // When
        testRepository.deleteDish(dishId)

        // Then
        coVerify { dishDao.deleteDish(dishId) }
    }

    @Test
    fun `updateDish should call dao editDish`() = runTest {
        // Given
        val dish = createTestDish(1L, "Updated Dish")

        // When
        testRepository.updateDish(dish)

        // Then
        coVerify { dishDao.editDish(dish) }
    }

    @Test
    fun `updateDishRecipe should call dao update`() = runTest {
        // Given
        val recipeId = 5L
        val dishId = 10L

        // When
        testRepository.updateDishRecipe(recipeId, dishId)

        // Then
        coVerify { dishDao.update(recipeId, dishId) }
    }

    @Test
    fun `deleteProductDish should call productDishDao deleteProductDish`() = runTest {
        // Given
        val productDish = createTestProductDish(1L, 2L)

        // When
        testRepository.deleteProductDish(productDish)

        // Then
        coVerify { productDishDao.deleteProductDish(productDish) }
    }

    @Test
    fun `deleteHalfProductDish should call halfProductDishDao delete`() = runTest {
        // Given
        val halfProductDish = createTestHalfProductDish(1L, 2L)

        // When
        testRepository.deleteHalfProductDish(halfProductDish)

        // Then
        coVerify { halfProductDishDao.delete(halfProductDish) }
    }

    @Test
    fun `updateProductDish should call productDishDao updateProductDish`() = runTest {
        // Given
        val productDish = createTestProductDish(1L, 2L)

        // When
        testRepository.updateProductDish(productDish)

        // Then
        coVerify { productDishDao.updateProductDish(productDish) }
    }

    @Test
    fun `updateHalfProductDish should call halfProductDishDao updateHalfProductDish`() = runTest {
        // Given
        val halfProductDish = createTestHalfProductDish(1L, 2L)

        // When
        testRepository.updateHalfProductDish(halfProductDish)

        // Then
        coVerify { halfProductDishDao.updateHalfProductDish(halfProductDish) }
    }

    @Test
    fun `addDish with recipe should work correctly`() = runTest {
        // Given
        val dish = DishBase(
            dishId = 0L,
            name = "Dish with Recipe",
            marginPercent = 150.0,
            dishTax = 0.10,
            recipeId = 42L
        )
        val expectedId = 999L
        coEvery { dishDao.addDish(dish) } returns expectedId

        // When
        val result = testRepository.addDish(dish)

        // Then
        result shouldBe expectedId
        coVerify { dishDao.addDish(dish) }
    }

    @Test
    fun `addDish without recipe should work correctly`() = runTest {
        // Given
        val dish = DishBase(
            dishId = 0L,
            name = "Dish without Recipe",
            marginPercent = 200.0,
            dishTax = 0.15,
            recipeId = null
        )
        val expectedId = 555L
        coEvery { dishDao.addDish(dish) } returns expectedId

        // When
        val result = testRepository.addDish(dish)

        // Then
        result shouldBe expectedId
        coVerify { dishDao.addDish(dish) }
    }

    private fun createTestDish(id: Long, name: String) = DishBase(
        dishId = id,
        name = name,
        marginPercent = 100.0,
        dishTax = 0.20,
        recipeId = null
    )

    private fun createTestCompleteDish(id: Long, name: String) = CompleteDish(
        dish = createTestDish(id, name),
        recipe = null,
        products = emptyList(),
        halfProducts = emptyList()
    )

    private fun createTestProductDish(productId: Long, dishId: Long) = ProductDish(
        productDishId = 0L,
        productId = productId,
        dishId = dishId,
        quantity = 100.0,
        quantityUnit = MeasurementUnit.GRAM
    )

    private fun createTestHalfProductDish(halfProductId: Long, dishId: Long) = HalfProductDish(
        halfProductDishId = 0L,
        halfProductId = halfProductId,
        dishId = dishId,
        quantity = 50.0,
        quantityUnit = MeasurementUnit.GRAM
    )
}
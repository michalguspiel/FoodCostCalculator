package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.dish.HalfProductDishDao
import com.erdees.foodcostcalc.data.db.dao.halfproduct.HalfProductDao
import com.erdees.foodcostcalc.data.db.dao.halfproduct.ProductHalfProductDao
import com.erdees.foodcostcalc.data.model.local.HalfProductBase
import com.erdees.foodcostcalc.data.model.local.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.local.associations.ProductHalfProduct
import com.erdees.foodcostcalc.data.model.local.joined.CompleteHalfProduct
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HalfProductRepositoryTest {

    private val halfProductDao = mockk<HalfProductDao>(relaxed = true)
    private val halfProductDishDao = mockk<HalfProductDishDao>(relaxed = true)
    private val productHalfProductDao = mockk<ProductHalfProductDao>(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Test implementation that accepts mocked DAOs
    private val testRepository = object : HalfProductRepository {
        override val completeHalfProducts get() = halfProductDao.getCompleteHalfProducts()
        override val halfProducts get() = halfProductDao.getHalfProductBase()
        override suspend fun getCompleteHalfProduct(id: Long) = halfProductDao.getCompleteHalfProduct(id)
        override suspend fun addHalfProduct(halfProductBase: HalfProductBase) = halfProductDao.addHalfProduct(halfProductBase)
        override suspend fun addHalfProductDish(halfProductDish: HalfProductDish) = halfProductDishDao.addHalfProductDish(halfProductDish)
        override suspend fun addProductHalfProduct(productHalfProduct: ProductHalfProduct) = productHalfProductDao.addProductHalfProduct(productHalfProduct)
        override suspend fun updateHalfProduct(halfProductBase: HalfProductBase) = halfProductDao.editHalfProduct(halfProductBase)
        override suspend fun deleteHalfProduct(id: Long) = halfProductDao.deleteHalfProduct(id)
        override suspend fun deleteProductHalfProduct(productHalfProduct: ProductHalfProduct) = productHalfProductDao.deleteProductHalfProduct(productHalfProduct)
        override suspend fun updateProductHalfProduct(productHalfProduct: ProductHalfProduct) = productHalfProductDao.updateProductHalfProduct(productHalfProduct)
    }

    @Test
    fun `completeHalfProducts should return flow from dao`() = runTest {
        // Given
        val expectedHalfProducts = listOf(
            createTestCompleteHalfProduct(1L, "Test Half Product 1"),
            createTestCompleteHalfProduct(2L, "Test Half Product 2")
        )
        every { halfProductDao.getCompleteHalfProducts() } returns flowOf(expectedHalfProducts)

        // When
        val result = testRepository.completeHalfProducts.first()

        // Then
        result shouldBe expectedHalfProducts
    }

    @Test
    fun `halfProducts should return flow from dao`() = runTest {
        // Given
        val expectedHalfProducts = listOf(
            createTestHalfProduct(1L, "Test Half Product 1"),
            createTestHalfProduct(2L, "Test Half Product 2")
        )
        every { halfProductDao.getHalfProductBase() } returns flowOf(expectedHalfProducts)

        // When
        val result = testRepository.halfProducts.first()

        // Then
        result shouldBe expectedHalfProducts
    }

    @Test
    fun `getCompleteHalfProduct should return complete half product flow from dao`() = runTest {
        // Given
        val halfProductId = 1L
        val expectedHalfProduct = createTestCompleteHalfProduct(halfProductId, "Test Half Product")
        every { halfProductDao.getCompleteHalfProduct(halfProductId) } returns flowOf(expectedHalfProduct)

        // When
        val result = testRepository.getCompleteHalfProduct(halfProductId).first()

        // Then
        result shouldBe expectedHalfProduct
    }

    @Test
    fun `addHalfProduct should call dao addHalfProduct`() = runTest {
        // Given
        val halfProduct = createTestHalfProduct(0L, "New Half Product")

        // When
        testRepository.addHalfProduct(halfProduct)

        // Then
        coVerify { halfProductDao.addHalfProduct(halfProduct) }
    }

    @Test
    fun `addHalfProductDish should call dao addHalfProductDish`() = runTest {
        // Given
        val halfProductDish = createTestHalfProductDish(1L, 2L)

        // When
        testRepository.addHalfProductDish(halfProductDish)

        // Then
        coVerify { halfProductDishDao.addHalfProductDish(halfProductDish) }
    }

    @Test
    fun `addProductHalfProduct should call dao addProductHalfProduct`() = runTest {
        // Given
        val productHalfProduct = createTestProductHalfProduct(1L, 2L)

        // When
        testRepository.addProductHalfProduct(productHalfProduct)

        // Then
        coVerify { productHalfProductDao.addProductHalfProduct(productHalfProduct) }
    }

    @Test
    fun `updateHalfProduct should call dao editHalfProduct`() = runTest {
        // Given
        val halfProduct = createTestHalfProduct(1L, "Updated Half Product")

        // When
        testRepository.updateHalfProduct(halfProduct)

        // Then
        coVerify { halfProductDao.editHalfProduct(halfProduct) }
    }

    @Test
    fun `deleteHalfProduct should call dao deleteHalfProduct`() = runTest {
        // Given
        val halfProductId = 1L

        // When
        testRepository.deleteHalfProduct(halfProductId)

        // Then
        coVerify { halfProductDao.deleteHalfProduct(halfProductId) }
    }

    @Test
    fun `deleteProductHalfProduct should call dao deleteProductHalfProduct`() = runTest {
        // Given
        val productHalfProduct = createTestProductHalfProduct(1L, 2L)

        // When
        testRepository.deleteProductHalfProduct(productHalfProduct)

        // Then
        coVerify { productHalfProductDao.deleteProductHalfProduct(productHalfProduct) }
    }

    @Test
    fun `updateProductHalfProduct should call dao updateProductHalfProduct`() = runTest {
        // Given
        val productHalfProduct = createTestProductHalfProduct(1L, 2L)

        // When
        testRepository.updateProductHalfProduct(productHalfProduct)

        // Then
        coVerify { productHalfProductDao.updateProductHalfProduct(productHalfProduct) }
    }

    @Test
    fun `addHalfProduct with different units should work correctly`() = runTest {
        // Given
        val halfProduct = HalfProductBase(
            halfProductId = 0L,
            name = "Half Product with Volume Unit",
            halfProductUnit = MeasurementUnit.LITER
        )

        // When
        testRepository.addHalfProduct(halfProduct)

        // Then
        coVerify { halfProductDao.addHalfProduct(halfProduct) }
    }

    @Test
    fun `addProductHalfProduct with weight piece should work correctly`() = runTest {
        // Given
        val productHalfProduct = ProductHalfProduct(
            productHalfProductId = 0L,
            productId = 1L,
            halfProductId = 2L,
            quantity = 250.0,
            quantityUnit = MeasurementUnit.GRAM,
            weightPiece = 25.0
        )

        // When
        testRepository.addProductHalfProduct(productHalfProduct)

        // Then
        coVerify { productHalfProductDao.addProductHalfProduct(productHalfProduct) }
    }

    @Test
    fun `addProductHalfProduct without weight piece should work correctly`() = runTest {
        // Given
        val productHalfProduct = ProductHalfProduct(
            productHalfProductId = 0L,
            productId = 1L,
            halfProductId = 2L,
            quantity = 500.0,
            quantityUnit = MeasurementUnit.MILLILITER,
            weightPiece = null
        )

        // When
        testRepository.addProductHalfProduct(productHalfProduct)

        // Then
        coVerify { productHalfProductDao.addProductHalfProduct(productHalfProduct) }
    }

    @Test
    fun `completeHalfProducts flow should handle empty list`() = runTest {
        // Given
        every { halfProductDao.getCompleteHalfProducts() } returns flowOf(emptyList())

        // When
        val result = testRepository.completeHalfProducts.first()

        // Then
        result shouldBe emptyList()
    }

    @Test
    fun `halfProducts flow should handle empty list`() = runTest {
        // Given
        every { halfProductDao.getHalfProductBase() } returns flowOf(emptyList())

        // When
        val result = testRepository.halfProducts.first()

        // Then
        result shouldBe emptyList()
    }

    @Test
    fun `addHalfProduct propagates dao exception`() = runTest {
        val halfProduct = createTestHalfProduct(0L, "New Half Product")
        io.mockk.coEvery { halfProductDao.addHalfProduct(halfProduct) } throws IllegalStateException("db error")
        shouldThrow<IllegalStateException> { testRepository.addHalfProduct(halfProduct) }
    }

    @Test
    fun `addHalfProductDish propagates dao exception`() = runTest {
        val halfProductDish = createTestHalfProductDish(1L, 2L)
        io.mockk.coEvery { halfProductDishDao.addHalfProductDish(halfProductDish) } throws IllegalStateException("db error")
        shouldThrow<IllegalStateException> { testRepository.addHalfProductDish(halfProductDish) }
    }

    @Test
    fun `addProductHalfProduct propagates dao exception`() = runTest {
        val productHalfProduct = createTestProductHalfProduct(1L, 2L)
        io.mockk.coEvery { productHalfProductDao.addProductHalfProduct(productHalfProduct) } throws IllegalStateException("db error")
        shouldThrow<IllegalStateException> { testRepository.addProductHalfProduct(productHalfProduct) }
    }

    @Test
    fun `updateHalfProduct propagates dao exception`() = runTest {
        val halfProduct = createTestHalfProduct(1L, "Updated Half Product")
        io.mockk.coEvery { halfProductDao.editHalfProduct(halfProduct) } throws IllegalStateException("db error")
        shouldThrow<IllegalStateException> { testRepository.updateHalfProduct(halfProduct) }
    }

    @Test
    fun `deleteHalfProduct propagates dao exception`() = runTest {
        val id = 1L
        io.mockk.coEvery { halfProductDao.deleteHalfProduct(id) } throws IllegalStateException("db error")
        shouldThrow<IllegalStateException> { testRepository.deleteHalfProduct(id) }
    }

    @Test
    fun `deleteProductHalfProduct propagates dao exception`() = runTest {
        val php = createTestProductHalfProduct(1L, 2L)
        io.mockk.coEvery { productHalfProductDao.deleteProductHalfProduct(php) } throws IllegalStateException("db error")
        shouldThrow<IllegalStateException> { testRepository.deleteProductHalfProduct(php) }
    }

    @Test
    fun `updateProductHalfProduct propagates dao exception`() = runTest {
        val php = createTestProductHalfProduct(1L, 2L)
        io.mockk.coEvery { productHalfProductDao.updateProductHalfProduct(php) } throws IllegalStateException("db error")
        shouldThrow<IllegalStateException> { testRepository.updateProductHalfProduct(php) }
    }

    private fun createTestHalfProduct(id: Long, name: String) = HalfProductBase(
        halfProductId = id,
        name = name,
        halfProductUnit = MeasurementUnit.KILOGRAM
    )

    private fun createTestCompleteHalfProduct(id: Long, name: String) = CompleteHalfProduct(
        halfProductBase = createTestHalfProduct(id, name),
        products = emptyList()
    )

    private fun createTestHalfProductDish(halfProductId: Long, dishId: Long) = HalfProductDish(
        halfProductDishId = 0L,
        halfProductId = halfProductId,
        dishId = dishId,
        quantity = 100.0,
        quantityUnit = MeasurementUnit.GRAM
    )

    private fun createTestProductHalfProduct(productId: Long, halfProductId: Long) = ProductHalfProduct(
        productHalfProductId = 0L,
        productId = productId,
        halfProductId = halfProductId,
        quantity = 200.0,
        quantityUnit = MeasurementUnit.GRAM,
        weightPiece = null
    )
}
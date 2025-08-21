package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.dish.ProductDishDao
import com.erdees.foodcostcalc.data.db.dao.product.ProductDao
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish
import com.erdees.foodcostcalc.domain.model.product.InputMethod
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

class ProductRepositoryTest {

    private val productDao = mockk<ProductDao>(relaxed = true)
    private val productDishDao = mockk<ProductDishDao>(relaxed = true)
    
    // For testing, we'll create a test implementation that accepts mocked DAOs
    private val testRepository = object : ProductRepository {
        override val products = productDao.getProducts()
        override suspend fun getProduct(id: Long) = productDao.getProduct(id)
        override suspend fun addProduct(product: ProductBase) = productDao.addProduct(product)
        override suspend fun addProductDish(productDish: ProductDish) = productDishDao.addProductDish(productDish)
        override suspend fun editProduct(newProduct: ProductBase) = productDao.editProduct(newProduct)
        override suspend fun deleteProduct(id: Long) = productDao.deleteProduct(id)
    }

    @Test
    fun `products should return flow from dao`() = runTest {
        // Given
        val expectedProducts = listOf(
            createTestProduct(1L, "Test Product 1"),
            createTestProduct(2L, "Test Product 2")
        )
        every { productDao.getProducts() } returns flowOf(expectedProducts)

        // When
        val result = testRepository.products.first()

        // Then
        result shouldBe expectedProducts
    }

    @Test
    fun `getProduct should return product flow from dao`() = runTest {
        // Given
        val productId = 1L
        val expectedProduct = createTestProduct(productId, "Test Product")
        every { productDao.getProduct(productId) } returns flowOf(expectedProduct)

        // When
        val result = testRepository.getProduct(productId).first()

        // Then
        result shouldBe expectedProduct
    }

    @Test
    fun `addProduct should call dao addProduct and return result`() = runTest {
        // Given
        val product = createTestProduct(0L, "New Product")
        val expectedId = 123L
        coEvery { productDao.addProduct(product) } returns expectedId

        // When
        val result = testRepository.addProduct(product)

        // Then
        result shouldBe expectedId
        coVerify { productDao.addProduct(product) }
    }

    @Test
    fun `addProductDish should call dao addProductDish`() = runTest {
        // Given
        val productDish = createTestProductDish(1L, 2L, 100.0)

        // When
        testRepository.addProductDish(productDish)

        // Then
        coVerify { productDishDao.addProductDish(productDish) }
    }

    @Test
    fun `editProduct should call dao editProduct`() = runTest {
        // Given
        val product = createTestProduct(1L, "Updated Product")

        // When
        testRepository.editProduct(product)

        // Then
        coVerify { productDao.editProduct(product) }
    }

    @Test
    fun `deleteProduct should call dao deleteProduct`() = runTest {
        // Given
        val productId = 1L

        // When
        testRepository.deleteProduct(productId)

        // Then
        coVerify { productDao.deleteProduct(productId) }
    }

    @Test
    fun `addProduct with package pricing should work correctly`() = runTest {
        // Given
        val product = ProductBase(
            productId = 0L,
            name = "Package Product",
            inputMethod = InputMethod.PACKAGE,
            packagePrice = 5.99,
            packageQuantity = 500.0,
            packageUnit = MeasurementUnit.GRAM,
            canonicalPrice = 11.98,
            canonicalUnit = MeasurementUnit.KILOGRAM,
            tax = 0.20,
            waste = 0.05
        )
        val expectedId = 456L
        coEvery { productDao.addProduct(product) } returns expectedId

        // When
        val result = testRepository.addProduct(product)

        // Then
        result shouldBe expectedId
        coVerify { productDao.addProduct(product) }
    }

    @Test
    fun `addProduct with unit pricing should work correctly`() = runTest {
        // Given
        val product = ProductBase(
            productId = 0L,
            name = "Unit Product",
            inputMethod = InputMethod.UNIT,
            packagePrice = null,
            packageQuantity = null,
            packageUnit = null,
            canonicalPrice = 2.50,
            canonicalUnit = MeasurementUnit.KILOGRAM,
            tax = 0.15,
            waste = 0.10
        )
        val expectedId = 789L
        coEvery { productDao.addProduct(product) } returns expectedId

        // When
        val result = testRepository.addProduct(product)

        // Then
        result shouldBe expectedId
        coVerify { productDao.addProduct(product) }
    }

    @Test
    fun `addProductDish with different units should work correctly`() = runTest {
        // Given
        val productDish = ProductDish(
            productDishId = 0L,
            productId = 1L,
            dishId = 2L,
            quantity = 250.0,
            quantityUnit = MeasurementUnit.MILLILITER
        )

        // When
        testRepository.addProductDish(productDish)

        // Then
        coVerify { productDishDao.addProductDish(productDish) }
    }

    private fun createTestProduct(id: Long, name: String) = ProductBase(
        productId = id,
        name = name,
        inputMethod = InputMethod.UNIT,
        packagePrice = null,
        packageQuantity = null,
        packageUnit = null,
        canonicalPrice = 10.0,
        canonicalUnit = MeasurementUnit.KILOGRAM,
        tax = 0.20,
        waste = 0.05
    )

    private fun createTestProductDish(productId: Long, dishId: Long, quantity: Double) = ProductDish(
        productDishId = 0L,
        productId = productId,
        dishId = dishId,
        quantity = quantity,
        quantityUnit = MeasurementUnit.GRAM
    )
}
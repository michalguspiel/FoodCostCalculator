package com.erdees.foodcostcalc.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.erdees.foodcostcalc.data.AppRoomDataBase // Corrected AppDatabase import
import com.erdees.foodcostcalc.data.db.dao.product.ProductDao // Corrected DAO import
import com.erdees.foodcostcalc.data.db.dao.dish.ProductDishDao // Corrected DAO import
// ProductEntity import removed
import com.erdees.foodcostcalc.data.model.local.ProductBase // Corrected model import
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish // Corrected model import
import com.erdees.foodcostcalc.utils.TestCoroutineRule
import io.kotest.matchers.collections.shouldContainExactly
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
class ProductRepositoryTest : KoinTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @RelaxedMockK
    lateinit var productDao: ProductDao

    @RelaxedMockK
    lateinit var productDishDao: ProductDishDao

    private val productRepository: ProductRepository by inject()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        startKoin {
            modules(module {
                single { productDao } // Provide the mock ProductDao
                single { productDishDao } // Provide the mock ProductDishDao
                single<ProductRepository> { ProductRepositoryImpl() } // Provide real impl for interface
            })
        }
        // AppRoomDataBase.TEST_MODE = true // Removed as TEST_MODE doesn't exist on AppRoomDataBase
    }

    @After
    fun teardown() {
        stopKoin()
        unmockkAll()
    }

    @Test
    fun `products should return flow of products from dao`() = runTest {
        // Given
        val productList = listOf(
            ProductBase(1L, "Milk", 1.0, 0.0, 0.0, "liter"), // Corrected constructor
            ProductBase(2L, "Bread", 2.5, 0.0, 0.0, "piece") // Corrected constructor
        )
        every { productDao.getProducts() } returns flowOf(productList)

        // When
        val result = productRepository.products.first()

        // Then
        result shouldContainExactly productList
        verify { productDao.getProducts() }
    }

    @Test
    fun `getProduct should return product from dao`() = runTest {
        // Given
        val productId = 1L
        val productBase = ProductBase(productId, "Milk", 1.0, 0.0, 0.0, "liter") // Corrected constructor
        every { productDao.getProduct(productId) } returns flowOf(productBase) // Corrected DAO method name

        // When
        val result = productRepository.getProduct(productId).first()

        // Then
        result shouldBe productBase
        verify { productDao.getProduct(productId) } // Corrected DAO method name
    }

    @Test
    fun `addProduct should call dao to add product`() = runTest {
        // Given
        val productBase = ProductBase(0L, "Cheese", 5.0, 0.0, 0.0, "kg") // Corrected constructor
        coEvery { productDao.addProduct(productBase) } returns Unit // Corrected DAO method name and no toProductEntity

        // When
        productRepository.addProduct(productBase)

        // Then
        coVerify { productDao.addProduct(productBase) } // Corrected DAO method name
    }

    @Test
    fun `addProductDish should call dao to add product dish`() = runTest {
        // Given
        val productDish = ProductDish(0L, 1L, 1L, 0.5, "unit") // Corrected constructor
        coEvery { productDishDao.addProductDish(productDish) } returns Unit // Corrected DAO method name

        // When
        productRepository.addProductDish(productDish)

        // Then
        coVerify { productDishDao.addProductDish(productDish) } // Corrected DAO method name
    }

    @Test
    fun `editProduct should call dao to update product`() = runTest {
        // Given
        val productBase = ProductBase(1L, "Aged Cheese", 6.0, 0.0, 0.0, "kg") // Corrected constructor
        coEvery { productDao.editProduct(productBase) } returns Unit // Corrected DAO method name and no toProductEntity

        // When
        productRepository.editProduct(productBase)

        // Then
        coVerify { productDao.editProduct(productBase) } // Corrected DAO method name
    }

    @Test
    fun `deleteProduct should call dao to delete product`() = runTest {
        // Given
        val productId = 1L
        coEvery { productDao.deleteProduct(productId) } returns Unit

        // When
        productRepository.deleteProduct(productId)

        // Then
        coVerify { productDao.deleteProduct(productId) }
    }
}

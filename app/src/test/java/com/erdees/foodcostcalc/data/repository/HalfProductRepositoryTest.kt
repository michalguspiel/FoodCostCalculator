package com.erdees.foodcostcalc.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.erdees.foodcostcalc.data.db.dao.halfproduct.HalfProductDao
import com.erdees.foodcostcalc.data.db.dao.dish.HalfProductDishDao
import com.erdees.foodcostcalc.data.db.dao.halfproduct.ProductHalfProductDao
import com.erdees.foodcostcalc.data.model.local.HalfProductBase
import com.erdees.foodcostcalc.data.model.local.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.local.associations.ProductHalfProduct
import com.erdees.foodcostcalc.data.model.local.joined.CompleteHalfProduct
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
class HalfProductRepositoryTest : KoinTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @RelaxedMockK
    lateinit var halfProductDao: HalfProductDao

    @RelaxedMockK
    lateinit var halfProductDishDao: HalfProductDishDao

    @RelaxedMockK
    lateinit var productHalfProductDao: ProductHalfProductDao

    private val halfProductRepository: HalfProductRepository by inject()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        startKoin {
            modules(module {
                single { halfProductDao }
                single { halfProductDishDao }
                single { productHalfProductDao }
                single<HalfProductRepository> { HalfProductRepositoryImpl() }
            })
        }
    }

    @After
    fun teardown() {
        stopKoin()
        unmockkAll()
    }

    @Test
    fun `completeHalfProducts should return flow of complete half products from dao`() = runTest {
        // Given
        val halfProductList = listOf(
            CompleteHalfProduct(HalfProductBase(1L, "Sauce Base", "liter"), emptyList()),
            CompleteHalfProduct(HalfProductBase(2L, "Dough", "kg"), emptyList())
        )
        every { halfProductDao.getCompleteHalfProducts() } returns flowOf(halfProductList)

        // When
        val result = halfProductRepository.completeHalfProducts.first()

        // Then
        result shouldContainExactly halfProductList
        verify { halfProductDao.getCompleteHalfProducts() }
    }

    @Test
    fun `halfProducts should return flow of half products from dao`() = runTest {
        // Given
        val basicHalfProductList = listOf(
            HalfProductBase(1L, "Sauce Base", "liter"),
            HalfProductBase(2L, "Dough", "kg")
        )
        every { halfProductDao.getHalfProductBase() } returns flowOf(basicHalfProductList) // Corrected DAO method

        // When
        val result = halfProductRepository.halfProducts.first()

        // Then
        result shouldContainExactly basicHalfProductList
        verify { halfProductDao.getHalfProductBase() } // Corrected DAO method
    }

    @Test
    fun `getCompleteHalfProduct should return complete half product from dao`() = runTest {
        // Given
        val halfProductId = 1L
        val completeHalfProduct = CompleteHalfProduct(HalfProductBase(halfProductId, "Sauce Base", "liter"), emptyList())
        every { halfProductDao.getCompleteHalfProduct(halfProductId) } returns flowOf(completeHalfProduct)

        // When
        val result = halfProductRepository.getCompleteHalfProduct(halfProductId).first()

        // Then
        result shouldBe completeHalfProduct
        verify { halfProductDao.getCompleteHalfProduct(halfProductId) }
    }

    @Test
    fun `addHalfProduct should call dao to add half product`() = runTest {
        // Given
        val halfProductBase = HalfProductBase(0L, "New Sauce", "liter")
        coEvery { halfProductDao.addHalfProduct(halfProductBase) } just runs // DAO returns Unit

        // When
        // val resultId = halfProductRepository.addHalfProduct(halfProductBase) // Repository handles ID logic
        halfProductRepository.addHalfProduct(halfProductBase)


        // Then
        // resultId shouldBe 1L // Cannot easily test returned ID from mock DAO returning Unit
        coVerify { halfProductDao.addHalfProduct(halfProductBase) }
    }

    @Test
    fun `addHalfProductDish should call dao to add half product dish`() = runTest {
        // Given
        val halfProductDish = HalfProductDish(0L, 1L, 1L, 0.2, "liter")
        coEvery { halfProductDishDao.addHalfProductDish(halfProductDish) } returns Unit

        // When
        halfProductRepository.addHalfProductDish(halfProductDish)

        // Then
        coVerify { halfProductDishDao.addHalfProductDish(halfProductDish) }
    }

    @Test
    fun `addProductHalfProduct should call dao to add product half product`() = runTest {
        // Given
        val productHalfProduct = ProductHalfProduct(0L, 1L, 1L, 0.5, "kg", null) // Added weightPiece
        coEvery { productHalfProductDao.addProductHalfProduct(productHalfProduct) } returns Unit // Corrected DAO method

        // When
        halfProductRepository.addProductHalfProduct(productHalfProduct)

        // Then
        coVerify { productHalfProductDao.addProductHalfProduct(productHalfProduct) } // Corrected DAO method
    }

    @Test
    fun `updateHalfProduct should call dao to update half product`() = runTest {
        // Given
        val halfProductBase = HalfProductBase(1L, "Updated Sauce", "liter")
        coEvery { halfProductDao.editHalfProduct(halfProductBase) } returns Unit // Corrected DAO method

        // When
        halfProductRepository.updateHalfProduct(halfProductBase)

        // Then
        coVerify { halfProductDao.editHalfProduct(halfProductBase) } // Corrected DAO method
    }

    @Test
    fun `deleteHalfProduct should call dao to delete half product`() = runTest {
        // Given
        val halfProductId = 1L
        coEvery { halfProductDao.deleteHalfProduct(halfProductId) } returns Unit

        // When
        halfProductRepository.deleteHalfProduct(halfProductId)

        // Then
        coVerify { halfProductDao.deleteHalfProduct(halfProductId) }
    }

    @Test
    fun `deleteProductHalfProduct should call dao to delete product half product`() = runTest {
        // Given
        val productHalfProduct = ProductHalfProduct(1L, 1L, 1L, 0.5, "kg", null) // Added weightPiece
        coEvery { productHalfProductDao.deleteProductHalfProduct(productHalfProduct) } returns Unit // Corrected DAO method

        // When
        halfProductRepository.deleteProductHalfProduct(productHalfProduct)

        // Then
        coVerify { productHalfProductDao.deleteProductHalfProduct(productHalfProduct) } // Corrected DAO method
    }

    @Test
    fun `updateProductHalfProduct should call dao to update product half product`() = runTest {
        // Given
        val productHalfProduct = ProductHalfProduct(1L, 1L, 1L, 0.6, "kg", null) // Added weightPiece
        coEvery { productHalfProductDao.updateProductHalfProduct(productHalfProduct) } returns Unit

        // When
        halfProductRepository.updateProductHalfProduct(productHalfProduct)

        // Then
        coVerify { productHalfProductDao.updateProductHalfProduct(productHalfProduct) }
    }
}

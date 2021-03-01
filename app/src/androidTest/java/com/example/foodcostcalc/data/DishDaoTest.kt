package com.example.foodcostcalc.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.foodcostcalc.data.dish.DishDao
import com.example.foodcostcalc.getOrAwaitValue
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product
import com.example.foodcostcalc.model.ProductIncluded
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class DishDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppRoomDataBase
    private lateinit var dao: DishDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppRoomDataBase::class.java
        ).allowMainThreadQueries().build()
        dao = database.dishDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getProductIncludedByDishId() = runBlockingTest {
        val simpleDish = Dish(1, "1")
        val simpleProduct = Product(1, "1", 1.0, 1.0, 1.0, "1")
        val singleProduct = ProductIncluded(1, simpleProduct, 1, simpleDish, 1, 1.0, "1")
        dao.addProductToDish(singleProduct)
        val allProductsIncluded = dao.getAllProductsIncluded().getOrAwaitValue()
        assertThat(allProductsIncluded).contains(singleProduct)
    }

    @Test
    fun testIfProductWasDeleted_false() = runBlockingTest {
        val simpleDish = Dish(2, "1")
        val simpleProduct = Product(2, "1", 1.0, 1.0, 1.0, "1")
        val secondProduct = ProductIncluded(2, simpleProduct, 2, simpleDish, 2, 2.0, "2")
        dao.addProductToDish(secondProduct)
        dao.deleteProductIncluded(secondProduct)

        val allProducts = dao.getAllProductsIncluded().getOrAwaitValue()
        assertThat(allProducts).doesNotContain(secondProduct)
    }

    @Test
    fun testIfAllProductsWillBeAdded() = runBlockingTest {
        val firstDish = Dish(1, "1")
        val secondDish = Dish(2, "2")
        val firstProduct = Product(1, "1", 1.0, 1.0, 1.0, "1")
        val secondProduct = Product(2, "2", 1.0, 1.0, 1.0, "1")
        val thirdProduct = Product(3, "3", 1.0, 1.0, 1.0, "1")
        val fifthProduct = Product(5, "5", 1.0, 1.0, 1.0, "1")
        val firstPI = ProductIncluded(1, firstProduct, 1, firstDish, 1, 2.0, "2")
        val secondPI = ProductIncluded(2, secondProduct, 1, firstDish, 2, 2.0, "2")
        val thirdPI = ProductIncluded(3, thirdProduct, 1, firstDish, 3, 2.0, "2")
        val fourthPI = ProductIncluded(4, thirdProduct, 1, firstDish, 3, 2.0, "2")
        val fifthPI = ProductIncluded(5, firstProduct, 2, secondDish, 1, 2.0, "2")
        val sixthPI = ProductIncluded(6, fifthProduct, 2, secondDish, 5, 2.0, "2")
        val seventhPI = ProductIncluded(7, firstProduct, 2, secondDish, 1, 2.0, "2")

        val list = listOf(
            firstPI,
            secondPI,
            thirdPI,
            fourthPI,
            fifthPI,
            sixthPI,
            seventhPI
        )
        for (productIncluded in list) {
            dao.addProductToDish(productIncluded)
        }
        val allproducts = dao.getAllProductsIncluded().getOrAwaitValue()

        assertThat(allproducts).contains(firstPI)
        assertThat(allproducts).contains(secondPI)
        assertThat(allproducts).contains(thirdPI)
        assertThat(allproducts).contains(thirdPI)
        assertThat(allproducts).contains(fourthPI)
        assertThat(allproducts).contains(fifthPI)
        assertThat(allproducts).contains(sixthPI)
        assertThat(allproducts).contains(seventhPI)

    }

    @Test
    fun testIfEverythingIsDeletedProperly() = runBlockingTest {
        val firstDish = Dish(1, "1")
        val secondDish = Dish(2, "2")
        val firstProduct = Product(1, "1", 1.0, 1.0, 1.0, "1")
        val secondProduct = Product(2, "2", 1.0, 1.0, 1.0, "1")
        val thirdProduct = Product(3, "3", 1.0, 1.0, 1.0, "1")
        val fifthProduct = Product(5, "5", 1.0, 1.0, 1.0, "1")
        val firstPI = ProductIncluded(1, firstProduct, 1, firstDish, 1, 2.0, "2")
        val secondPI = ProductIncluded(2, secondProduct, 1, firstDish, 2, 2.0, "2")
        val thirdPI = ProductIncluded(3, thirdProduct, 1, firstDish, 3, 2.0, "2")
        val fourthPI = ProductIncluded(4, thirdProduct, 1, firstDish, 3, 2.0, "2")
        val fifthPI = ProductIncluded(5, firstProduct, 2, secondDish, 1, 2.0, "2")
        val sixthPI = ProductIncluded(6, fifthProduct, 2, secondDish, 5, 2.0, "2")
        val seventhPI = ProductIncluded(7, firstProduct, 2, secondDish, 1, 2.0, "2")

        val list = listOf(
            firstPI,
            secondPI,
            thirdPI,
            fourthPI,
            fifthPI,
            sixthPI,
            seventhPI
        )

        for (productIncluded in list) {
            dao.addProductToDish(productIncluded)
        }
        for (productIncluded in list) {
            dao.deleteProductIncluded(productIncluded)
        }
        val allProductIncluded = dao.getAllProductsIncluded().getOrAwaitValue()

        assertThat(allProductIncluded).isEmpty()

    }

    @Test
    fun moreAutomatedTestToCheckIfEverythingIsDeletedProperly() = runBlockingTest {
        for (eachNumber in 1..99) {
             dao.addProductToDish( ProductIncluded(
                    eachNumber.toLong(),
                    Product(
                        eachNumber.toLong(),
                        eachNumber.toString(),
                        eachNumber.toDouble(),
                        eachNumber.toDouble(),
                        eachNumber.toDouble(),
                        eachNumber.toString()
                    ),
                    eachNumber.toLong(),
                    Dish(eachNumber.toLong(), eachNumber.toString()),
                    eachNumber.toLong(),
                    eachNumber.toDouble(),
                    eachNumber.toString()
                )
             )
        }

        for (eachNumber in 1..99) {
            dao.deleteProductIncluded( ProductIncluded(
                    eachNumber.toLong(),
                    Product(
                        eachNumber.toLong(),
                        eachNumber.toString(),
                        eachNumber.toDouble(),
                        eachNumber.toDouble(),
                        eachNumber.toDouble(),
                        eachNumber.toString()
                    ),
                    eachNumber.toLong(),
                    Dish(eachNumber.toLong(), eachNumber.toString()),
                    eachNumber.toLong(),
                    eachNumber.toDouble(),
                    eachNumber.toString()
                )
            )
        }
        val allProductIncluded = dao.getAllProductsIncluded().getOrAwaitValue()
        assertThat(allProductIncluded).hasSize(0) // to check if everything was deleted properly
    }

    @Test
    fun testIfGetProductIncludedByDishIdWorksProperly() = runBlockingTest {
        for (eachNumber in 1..99) {
            dao.addProductToDish( ProductIncluded(
                eachNumber.toLong(),
                Product(
                    eachNumber.toLong(),
                    eachNumber.toString(),
                    eachNumber.toDouble(),
                    eachNumber.toDouble(),
                    eachNumber.toDouble(),
                    eachNumber.toString()
                ),
                1,
                Dish(eachNumber.toLong(), eachNumber.toString()),
                eachNumber.toLong(),
                eachNumber.toDouble(),
                eachNumber.toString()
            )
            )
        }
        val productIncludedByThisDishId = dao.getProductIncludedFromDishId(1).getOrAwaitValue()

        assertThat(productIncludedByThisDishId).hasSize(99)

    }


}
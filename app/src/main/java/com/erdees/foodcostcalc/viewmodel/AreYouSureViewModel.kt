package com.erdees.foodcostcalc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.dishWithProductIncluded.DishWithProductIncludedRepository
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishRepository
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AreYouSureViewModel(application: Application): AndroidViewModel(application) {

    val readAllProductData: LiveData<List<Product>>
    val readAllDishData: LiveData<List<Dish>>
    val readAllHalfProductData: LiveData<List<HalfProduct>>
    val readAllProductIncludedInHalfProductData: LiveData<List<ProductIncludedInHalfProduct>>
    val readAllProductIncludedInHalfProductDataNotAsc: LiveData<List<ProductIncludedInHalfProduct>>

    private val productRepository: ProductRepository
    private val dishRepository: DishRepository
    private val productIncludedRepository : ProductIncludedRepository
    private val dishWithProductIncludedRepository : DishWithProductIncludedRepository

    private val halfProductRepository: HalfProductRepository
    private val productIncludedInHalfProductRepository: ProductIncludedInHalfProductRepository
    private val halfProductWithProductsIncludedRepository: HalfProductWithProductsIncludedRepository
    private val halfProductIncludedInDishRepository : HalfProductIncludedInDishRepository

    private val basicRepository: BasicRepository

    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        val dishWithProductIncludedDao = AppRoomDataBase.getDatabase(application).dishWithProductIncludedDao()
        val basicDao = BasicDataBase.getInstance().basicDao
        val productIncludedDao = AppRoomDataBase.getDatabase(application).productIncludedDao()
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        val productIncludedInHalfProductDao =
            AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()
        val halfProductWithProductIncludedDao = AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
        val halfProductIncludedInDishDao = AppRoomDataBase.getDatabase(application).halfProductIncludedInDishDao()

        productRepository = ProductRepository(productDao)
        dishRepository = DishRepository(dishDao)
        dishWithProductIncludedRepository = DishWithProductIncludedRepository(dishWithProductIncludedDao)
        basicRepository = BasicRepository(basicDao)
        productIncludedRepository = ProductIncludedRepository(productIncludedDao)
        halfProductRepository = HalfProductRepository(halfProductDao)
        productIncludedInHalfProductRepository =
            ProductIncludedInHalfProductRepository(productIncludedInHalfProductDao)
        halfProductWithProductsIncludedRepository = HalfProductWithProductsIncludedRepository(halfProductWithProductIncludedDao)
        halfProductIncludedInDishRepository = HalfProductIncludedInDishRepository(halfProductIncludedInDishDao)

        readAllProductData = productRepository.readAllData
        readAllDishData = dishRepository.readAllData
        readAllHalfProductData = halfProductRepository.readAllData
        readAllProductIncludedInHalfProductData = productIncludedInHalfProductRepository.readAllData
        readAllProductIncludedInHalfProductDataNotAsc = productIncludedInHalfProductRepository.readAllDataNotAsc


    }
    /**Basic repository methods*/
    fun getPosition() = basicRepository.getPosition()

    fun setFlag(boolean: Boolean) {
        basicRepository.setFlag(boolean)
    }

    fun getProductIncluded() = basicRepository.getProductIncluded()

    fun getProductIncludedInHalfProduct() = basicRepository.getProductIncludedInHalfProduct()

    fun getHalfProductIncluded() = basicRepository.getHalfProductIncluded()

    /**DishWithProductIncluded repository methods*/

    fun getDishesWithProductsIncluded() = dishWithProductIncludedRepository.getDishesWithProductsIncluded()

    /**ProductIncluded Repository methods*/

    fun getProductIncludedByDishId(dishId: Long) = productIncludedRepository.getProductIncludedByDishID(dishId)

    /**ProductsIncludedInHalfProduct Repository methods*/

    fun getProductsIncludedFromHalfProduct(halfProductId: Long)
            = productIncludedInHalfProductRepository.getProductsIncludedFromHalfProduct(halfProductId)

    /**HalfProductIncludedInDish Repository methods*/
    fun getHalfProductsIncludedInDishFromDishByHalfProduct(productId: Long) = halfProductIncludedInDishRepository.getHalfProductsIncludedInDishFromDishByHalfProduct(productId)

    /**HalfProductWithProductsIncluded Repository methods*/
    fun getHalfProductWithProductIncluded() = halfProductWithProductsIncludedRepository.readAllData

    /**Product Repository methods*/
    fun deleteProduct(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.deleteProduct(product)
        }
    }
    /**Dish Repository methods*/

    fun deleteDish(dish: Dish) {
        viewModelScope.launch(Dispatchers.IO) {
            dishRepository.deleteDish(dish)
        }
    }
    /**ProductIncluded Repository methods*/

    fun deleteProductIncluded(productIncluded: ProductIncluded) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedRepository.deleteProductIncluded(productIncluded)
        }
    }
    /**ProductIncludedInHalfProduct Repository methods*/

    fun deleteProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct){
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedInHalfProductRepository.deleteProductIncludedInHalfProduct(productIncludedInHalfProduct)
        }
    }
    /**HalfProduct Repository methods*/

    fun deleteHalfProducts(halfProduct: HalfProduct) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.deleteHalfProduct(halfProduct)
        }

    }
    /**HalfProductIncludedInDish Repository methods*/

    fun deleteHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish){
        viewModelScope.launch(Dispatchers.IO){
            halfProductIncludedInDishRepository
                .deleteHalfProductIncludedInDish(halfProductIncludedInDish)
        }
    }

}
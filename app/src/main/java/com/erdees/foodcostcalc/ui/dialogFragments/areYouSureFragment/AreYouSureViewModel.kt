package com.erdees.foodcostcalc.ui.dialogFragments.areYouSureFragment

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
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.ProductIncludedInHalfProductModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AreYouSureViewModel(application: Application) : AndroidViewModel(application) {

    val readAllProductModelData: LiveData<List<ProductModel>>
    val readAllHalfProductModelData: LiveData<List<HalfProductModel>>
    private val readAllProductIncludedInHalfProductModelData: LiveData<List<ProductIncludedInHalfProductModel>>
    private val readAllProductIncludedInHalfProductModelDataNotAsc: LiveData<List<ProductIncludedInHalfProductModel>>

    private val productRepository: ProductRepository
    private val dishRepository: DishRepository
    private val productIncludedRepository: ProductIncludedRepository
    private val dishWithProductIncludedRepository: DishWithProductIncludedRepository

    private val halfProductRepository: HalfProductRepository
    private val productIncludedInHalfProductRepository: ProductIncludedInHalfProductRepository
    private val halfProductWithProductsIncludedRepository: HalfProductWithProductsIncludedRepository
    private val halfProductIncludedInDishRepository: HalfProductIncludedInDishRepository

    private val basicRepository: BasicRepository

    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        val dishWithProductIncludedDao =
            AppRoomDataBase.getDatabase(application).dishWithProductIncludedDao()
        val basicDao = BasicDataBase.getInstance().basicDao
        val productIncludedDao = AppRoomDataBase.getDatabase(application).productIncludedDao()
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        val productIncludedInHalfProductDao =
            AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()
        val halfProductWithProductIncludedDao =
            AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
        val halfProductIncludedInDishDao =
            AppRoomDataBase.getDatabase(application).halfProductIncludedInDishDao()

        productRepository = ProductRepository(productDao)
        dishRepository = DishRepository(dishDao)
        dishWithProductIncludedRepository =
            DishWithProductIncludedRepository(dishWithProductIncludedDao)
        basicRepository = BasicRepository(basicDao)
        productIncludedRepository = ProductIncludedRepository(productIncludedDao)
        halfProductRepository = HalfProductRepository(halfProductDao)
        productIncludedInHalfProductRepository =
            ProductIncludedInHalfProductRepository(productIncludedInHalfProductDao)
        halfProductWithProductsIncludedRepository =
            HalfProductWithProductsIncludedRepository(halfProductWithProductIncludedDao)
        halfProductIncludedInDishRepository =
            HalfProductIncludedInDishRepository(halfProductIncludedInDishDao)

        readAllProductModelData = productRepository.readAllData
        readAllHalfProductModelData = halfProductRepository.readAllData
        readAllProductIncludedInHalfProductModelData =
            productIncludedInHalfProductRepository.readAllData
        readAllProductIncludedInHalfProductModelDataNotAsc =
            productIncludedInHalfProductRepository.readAllDataNotAsc


    }

    /**Basic repository methods*/
    fun getPosition() = basicRepository.getPosition()

    fun setFlag(boolean: Boolean) {
        basicRepository.setFlag(boolean)
    }

    fun getProductIncludedInHalfProduct() = basicRepository.getProductIncludedInHalfProduct()

    /**ProductsIncludedInHalfProduct Repository methods*/

    fun getProductsIncludedFromHalfProduct(halfProductId: Long) =
        productIncludedInHalfProductRepository.getProductsIncludedFromHalfProduct(halfProductId)

    /**HalfProductIncludedInDishModel Repository methods*/
    fun getHalfProductsIncludedInDishFromDishByHalfProduct(productId: Long) =
        halfProductIncludedInDishRepository.getHalfProductsIncludedInDishFromDishByHalfProduct(
            productId
        )

    /**HalfProductWithProductsIncludedModel Repository methods*/
    fun getHalfProductWithProductIncluded() = halfProductWithProductsIncludedRepository.readAllData

    /**ProductModel Repository methods*/
    fun deleteProduct(productModel: ProductModel) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.deleteProduct(productModel)
        }
    }


    /**ProductIncludedInHalfProductModel Repository methods*/

    fun deleteProductIncludedInHalfProduct(productIncludedInHalfProductModel: ProductIncludedInHalfProductModel) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedInHalfProductRepository.deleteProductIncludedInHalfProduct(
                productIncludedInHalfProductModel
            )
        }
    }

    /**HalfProductModel Repository methods*/

    fun deleteHalfProducts(halfProductModel: HalfProductModel) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.deleteHalfProduct(halfProductModel)
        }

    }

    /**HalfProductIncludedInDishModel Repository methods*/

    fun deleteHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductIncludedInDishRepository
                .deleteHalfProductIncludedInDish(halfProductIncludedInDishModel)
        }
    }

}
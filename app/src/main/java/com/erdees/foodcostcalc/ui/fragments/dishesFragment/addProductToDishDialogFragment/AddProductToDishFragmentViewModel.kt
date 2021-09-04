package com.erdees.foodcostcalc.ui.fragments.dishesFragment.addProductToDishDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedRepository
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.DishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddProductToDishFragmentViewModel(application: Application) : AndroidViewModel(application) {


    val readAllHalfProductModelData: LiveData<List<HalfProductModel>>
    val readAllProductModelData: LiveData<List<ProductModel>>
    val readAllDishModelData: LiveData<List<DishModel>>

    private val productRepository: ProductRepository
    private val halfProductRepository: HalfProductRepository
    private val dishRepository: DishRepository
    private val basicRepository: BasicRepository
    private val productIncludedRepository: ProductIncludedRepository
    private val halfProductIncludedInDishRepository: HalfProductIncludedInDishRepository

    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        val productIncludedDao = AppRoomDataBase.getDatabase(application).productIncludedDao()
        val halfProductIncludedInDishDao =
            AppRoomDataBase.getDatabase(application).halfProductIncludedInDishDao()
        val basicDao = BasicDataBase.getInstance().basicDao

        basicRepository = BasicRepository(basicDao)
        halfProductRepository = HalfProductRepository(halfProductDao)
        productRepository = ProductRepository(productDao)
        dishRepository = DishRepository(dishDao)
        productIncludedRepository = ProductIncludedRepository(productIncludedDao)
        halfProductIncludedInDishRepository =
            HalfProductIncludedInDishRepository(halfProductIncludedInDishDao)

        readAllHalfProductModelData = halfProductRepository.readAllData
        readAllProductModelData = productRepository.readAllData
        readAllDishModelData = dishRepository.readAllData

    }

    fun getDishToDialog() = basicRepository.getDishToDialog()

    fun getHalfProducts() = halfProductRepository.readAllData

    fun addProductToDish(product: ProductIncluded) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedRepository.addProductToDish(product)
        }
    }

    fun addHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductIncludedInDishRepository
                .addHalfProductIncludedInDish(halfProductIncludedInDishModel)
        }
    }


}
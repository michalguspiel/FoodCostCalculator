package com.erdees.foodcostcalc.viewmodel.adaptersViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishRepository
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedRepository
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.DishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditDishAdapterViewModel(application: Application):AndroidViewModel(application) {

    private val dishRepository: DishRepository
    private val productIncludedRepository: ProductIncludedRepository
    private val halfProductIncludedInDishRepository: HalfProductIncludedInDishRepository
    val basicRepository: BasicRepository

    init {
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        val productIncludedDao = AppRoomDataBase.getDatabase(application).productIncludedDao()
        val halfProductIncludedInDishDao =
            AppRoomDataBase.getDatabase(application).halfProductIncludedInDishDao()
        val basicDao = BasicDataBase.getInstance().basicDao

        dishRepository = DishRepository(dishDao)
        productIncludedRepository = ProductIncludedRepository(productIncludedDao)
        halfProductIncludedInDishRepository =
            HalfProductIncludedInDishRepository(halfProductIncludedInDishDao)
        basicRepository = BasicRepository(basicDao)

    }

    fun editDish(dishModel: DishModel) {
        viewModelScope.launch(Dispatchers.IO) {
            dishRepository.editDish(dishModel)
        }
    }

    fun editHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductIncludedInDishRepository
                .editHalfProductIncludedInDish(halfProductIncludedInDishModel)
        }
    }


    fun editProductsIncluded(productIncluded: ProductIncluded) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedRepository.editProductsIncluded(productIncluded)
        }
    }

    /**Basic repository methods*/
    fun setProductIncluded(product: ProductIncluded) {
        basicRepository.setProductIncluded(product)
    }

    fun setHalfProductIncluded(halfProductIncludedModel: HalfProductIncludedInDishModel) {
        basicRepository.setHalfProductIncluded(halfProductIncludedModel)
    }

}


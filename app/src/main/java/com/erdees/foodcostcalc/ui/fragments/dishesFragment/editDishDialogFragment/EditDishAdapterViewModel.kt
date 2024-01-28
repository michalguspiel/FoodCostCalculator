package com.erdees.foodcostcalc.ui.fragments.dishesFragment.editDishDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishRepository
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedRepository
import com.erdees.foodcostcalc.domain.model.dish.GrandDishModel
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.domain.model.product.ProductIncluded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditDishAdapterViewModel(application: Application) : AndroidViewModel(application) {

    private val dishRepository: DishRepository
    private val productIncludedRepository: ProductIncludedRepository
    private val halfProductIncludedInDishRepository: HalfProductIncludedInDishRepository

    init {
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        val productIncludedDao = AppRoomDataBase.getDatabase(application).productIncludedDao()
        val halfProductIncludedInDishDao =
            AppRoomDataBase.getDatabase(application).halfProductIncludedInDishDao()

        dishRepository = DishRepository.getInstance(dishDao)
        productIncludedRepository = ProductIncludedRepository.getInstance(productIncludedDao)
        halfProductIncludedInDishRepository =
            HalfProductIncludedInDishRepository.getInstance(halfProductIncludedInDishDao)
    }

    var cloneOfListOfProductsIncluded: MutableList<ProductIncluded> = mutableListOf()
    var cloneOfListOfHalfProductModels: MutableList<HalfProductIncludedInDishModel> =
        mutableListOf()


    fun updateClonesOfLists(grandDishModel: GrandDishModel) {
        cloneOfListOfProductsIncluded = grandDishModel.productsIncluded.toMutableList()
        cloneOfListOfHalfProductModels = grandDishModel.halfProducts.toMutableList()
    }

    fun saveLists() {
        cloneOfListOfProductsIncluded.forEach { editProductsIncluded(it) }
        cloneOfListOfHalfProductModels.forEach { editHalfProductIncludedInDish(it) }
    }

    private fun editHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductIncludedInDishRepository
                .editHalfProductIncludedInDish(halfProductIncludedInDishModel)
        }
    }


    private fun editProductsIncluded(productIncluded: ProductIncluded) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedRepository.editProductsIncluded(productIncluded)
        }
    }

    fun deleteHalfProductIncluded(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductIncludedInDishRepository.deleteHalfProductIncludedInDish(
                halfProductIncludedInDishModel
            )
        }
    }

    fun deleteProductIncluded(productIncluded: ProductIncluded) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedRepository.deleteProductIncluded(productIncluded)
        }
    }

}


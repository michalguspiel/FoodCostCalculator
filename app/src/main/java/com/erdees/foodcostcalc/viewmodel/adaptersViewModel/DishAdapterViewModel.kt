package com.erdees.foodcostcalc.viewmodel.adaptersViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.DishModel

class DishAdapterViewModel(application: Application):AndroidViewModel(application) {

    val halfProductWithProductsIncludedRepository : HalfProductWithProductsIncludedRepository
    val basicRepository : BasicRepository
    init {
        val halfProductWithProductsIncludedDao = AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
        val basicDao = BasicDataBase.getInstance().basicDao
        halfProductWithProductsIncludedRepository = HalfProductWithProductsIncludedRepository(halfProductWithProductsIncludedDao)
        basicRepository = BasicRepository(basicDao)
    }

    fun setOpenCreateDishFlag(boolean: Boolean) = basicRepository.setOpenCreateDishFlag(boolean)

    fun getCertainHalfProductWithProductsIncluded(halfProductId: Long)
            = halfProductWithProductsIncludedRepository.getCertainHalfProductWithProductsIncluded(halfProductId)

    fun passDishToDialog(dishModel: DishModel) = basicRepository.passDishToDialog(dishModel)

}
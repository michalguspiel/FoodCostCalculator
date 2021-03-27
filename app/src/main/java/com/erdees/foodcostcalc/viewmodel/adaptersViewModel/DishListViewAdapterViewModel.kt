package com.erdees.foodcostcalc.viewmodel.adaptersViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedDao
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository

class DishListViewAdapterViewModel(application: Application):AndroidViewModel(application) {

    val halfProductWithProductsIncludedRepository : HalfProductWithProductsIncludedRepository

    init {
        val halfProductWithProductsIncludedDao  = AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
        halfProductWithProductsIncludedRepository = HalfProductWithProductsIncludedRepository(halfProductWithProductsIncludedDao)
    }

    fun getCertainHalfProductWithProductsIncluded(halfProductId: Long)
            = halfProductWithProductsIncludedRepository.getCertainHalfProductWithProductsIncluded(halfProductId)
}
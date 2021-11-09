package com.erdees.foodcostcalc.ui.fragments.dishesFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository

class DishListViewAdapterViewModel(application: Application):AndroidViewModel(application) {

    private val halfProductWithProductsIncludedRepository: HalfProductWithProductsIncludedRepository

    init {
        val halfProductWithProductsIncludedDao  = AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
        halfProductWithProductsIncludedRepository = HalfProductWithProductsIncludedRepository(halfProductWithProductsIncludedDao)
    }
    fun getCertainHalfProductWithProductsIncluded(halfProductId: Long)
            = halfProductWithProductsIncludedRepository.getCertainHalfProductWithProductsIncluded(halfProductId)

}
package com.erdees.foodcostcalc.data.halfProductWithProductsIncluded

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductWithProductsIncludedModel

@Dao
interface HalfProductWithProductsIncludedDao {

    @Transaction
    @Query("SELECT * FROM HALFPRODUCT ORDER BY name ASC")
    fun getHalfProductsWithProductsIncluded(): LiveData<List<HalfProductWithProductsIncludedModel>>

    @Transaction
    @Query("SELECT * FROM HALFPRODUCT WHERE halfProductId = :halfProductId ORDER BY name ASC")
    fun getCertainHalfProductWithProductsIncluded(halfProductId: Long): LiveData<HalfProductWithProductsIncludedModel>


}
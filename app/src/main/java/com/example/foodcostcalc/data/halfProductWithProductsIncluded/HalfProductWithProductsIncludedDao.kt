package com.example.foodcostcalc.data.halfProductWithProductsIncluded

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.foodcostcalc.model.HalfProductWithProductsIncluded

@Dao
interface HalfProductWithProductsIncludedDao {

    @Transaction
    @Query("SELECT * FROM HALFPRODUCT ORDER BY name ASC")
    fun getHalfProductsWithProductsIncluded(): LiveData<List<HalfProductWithProductsIncluded>>

    @Transaction
    @Query("SELECT * FROM HALFPRODUCT WHERE halfProductId = :halfProductId ORDER BY name ASC")
    fun getCertainHalfProductWithProductsIncluded(halfProductId: Long): LiveData<HalfProductWithProductsIncluded>


}
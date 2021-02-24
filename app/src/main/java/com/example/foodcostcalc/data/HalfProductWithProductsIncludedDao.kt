package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.foodcostcalc.model.HalfProductWithProductsIncluded

@Dao
interface HalfProductWithProductsIncludedDao {

    @Transaction
    @Query("SELECT * FROM HALFPRODUCT ORDER BY name ASC")
    fun getHalfProductsWithProductsIncluded(): LiveData<List<HalfProductWithProductsIncluded>>

}
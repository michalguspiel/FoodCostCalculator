package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.foodcostcalc.model.DishWithHalfProductCrossRef
import com.example.foodcostcalc.model.HalfProductWithProductsIncluded
import com.example.foodcostcalc.model.HalfProductWithProductsIncludedCrossRef

@Dao
interface HalfProductWithProductsIncludedDao {

    @Transaction
    @Query("SELECT * FROM HALFPRODUCT ORDER BY name ASC")
    fun getHalfProductsWithProductsIncluded(): LiveData<List<HalfProductWithProductsIncluded>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHalfProductWithProductsIncludedCrossRef(halfProductWithProductsIncludedCrossRef: HalfProductWithProductsIncludedCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHalfProductToDish(dishWithHalfProductCrossRef: DishWithHalfProductCrossRef)

}
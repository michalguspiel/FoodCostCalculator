package com.erdees.foodcostcalc.data.halfProductIncludedInDish

import androidx.lifecycle.LiveData
import androidx.room.*
import com.erdees.foodcostcalc.model.HalfProductIncludedInDish

@Dao
interface HalfProductIncludedInDishDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)

    @Query("SELECT * FROM HALFPRODUCTINCLUDEDINDISH WHERE dishOwnerId = :dishId ")
    fun getHalfProductsIncludedInDishFromDish(dishId: Long): LiveData<List<HalfProductIncludedInDish>>

    @Query("SELECT * FROM HALFPRODUCTINCLUDEDINDISH WHERE halfProductId =:halfProductId")
    fun getHalfProductsIncludedInDishFromDishByHalfProduct(halfProductId: Long): LiveData<List<HalfProductIncludedInDish>>

    @Update
    suspend fun editHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)

    @Delete
    suspend fun deleteHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)


}
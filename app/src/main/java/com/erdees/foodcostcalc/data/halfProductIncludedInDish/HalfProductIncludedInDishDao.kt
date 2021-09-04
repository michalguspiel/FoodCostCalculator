package com.erdees.foodcostcalc.data.halfProductIncludedInDish

import androidx.lifecycle.LiveData
import androidx.room.*
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel

@Dao
interface HalfProductIncludedInDishDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel)

    @Query("SELECT * FROM HALFPRODUCTINCLUDEDINDISH WHERE dishOwnerId = :dishId ")
    fun getHalfProductsIncludedInDishFromDish(dishId: Long): LiveData<List<HalfProductIncludedInDishModel>>

    @Query("SELECT * FROM HALFPRODUCTINCLUDEDINDISH WHERE halfProductId =:halfProductId")
    fun getHalfProductsIncludedInDishFromDishByHalfProduct(halfProductId: Long): LiveData<List<HalfProductIncludedInDishModel>>

    @Update
    suspend fun editHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel)

    @Delete
    suspend fun deleteHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel)


}
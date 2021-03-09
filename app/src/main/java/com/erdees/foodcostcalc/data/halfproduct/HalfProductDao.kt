package com.erdees.foodcostcalc.data.halfproduct

import androidx.lifecycle.LiveData
import androidx.room.*
import com.erdees.foodcostcalc.model.HalfProduct
import com.erdees.foodcostcalc.model.HalfProductIncludedInDish

@Dao
interface HalfProductDao {

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun addHalfProduct(halfProduct: HalfProduct)

@Query("SELECT * FROM HALFPRODUCT ORDER BY name ASC")
fun getHalfProducts(): LiveData<List<HalfProduct>>

@Insert(onConflict = OnConflictStrategy.REPLACE)
fun addHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)

@Query("SELECT * FROM HALFPRODUCTINCLUDEDINDISH WHERE dishOwnerId = :dishId ")
fun getHalfProductsFromDish(dishId: Long): LiveData<List<HalfProductIncludedInDish>>

@Query("SELECT * FROM HALFPRODUCTINCLUDEDINDISH WHERE halfProductId =:halfProductId")
fun getHalfProductsFromDishByHalfProduct(halfProductId: Long): LiveData<List<HalfProductIncludedInDish>>

@Update
suspend fun editHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)

@Delete
suspend fun deleteHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)

@Update
suspend fun editHalfProduct(halfProduct: HalfProduct)

@Delete
suspend fun deleteHalfProduct(halfProduct: HalfProduct)
}
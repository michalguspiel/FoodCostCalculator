package com.example.foodcostcalc.data.halfproduct

import android.util.Half
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.foodcostcalc.model.HalfProduct
import com.example.foodcostcalc.model.HalfProductIncludedInDish
import com.example.foodcostcalc.model.HalfProductWithProductsIncluded

@Dao
interface HalfProductDao {

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun addHalfProduct(halfProduct: HalfProduct)

@Query("SELECT * FROM HALFPRODUCT ORDER BY name ASC")
fun getHalfProducts(): LiveData<List<HalfProduct>>

@Insert(onConflict = OnConflictStrategy.REPLACE)
fun addHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)

@Query("SELECT * FROM HALFPRODUCTINCLUDEDINDISH WHERE dishOwnerId = :dishId ORDER BY name ASC")
fun getHalfProductsFromDish(dishId: Long): LiveData<List<HalfProductIncludedInDish>>

@Update
suspend fun editHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)

@Delete
suspend fun deleteHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)

@Update
suspend fun editHalfProduct(halfProduct: HalfProduct)

@Delete
suspend fun deleteHalfProduct(halfProduct: HalfProduct)
}
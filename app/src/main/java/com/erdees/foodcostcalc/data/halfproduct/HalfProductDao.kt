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

@Update
suspend fun editHalfProduct(halfProduct: HalfProduct)

@Delete
suspend fun deleteHalfProduct(halfProduct: HalfProduct)
}
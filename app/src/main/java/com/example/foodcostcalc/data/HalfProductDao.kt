package com.example.foodcostcalc.data

import android.util.Half
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.foodcostcalc.model.HalfProduct
import com.example.foodcostcalc.model.HalfProductWithProductsIncluded

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
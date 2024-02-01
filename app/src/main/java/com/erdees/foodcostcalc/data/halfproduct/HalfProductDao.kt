package com.erdees.foodcostcalc.data.halfproduct

import androidx.lifecycle.LiveData
import androidx.room.*
import com.erdees.foodcostcalc.entities.HalfProduct

@Dao
interface HalfProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHalfProduct(halfProduct: HalfProduct)

    @Query("SELECT * FROM HALFPRODUCT ORDER BY name ASC")
    fun getHalfProducts(): LiveData<List<HalfProduct>>

    @Update
    suspend fun editHalfProduct(halfProduct: HalfProduct)

    @Query("DELETE FROM HalfProduct WHERE HalfProductId =:id")
    fun deleteHalfProduct(id: Long)
}

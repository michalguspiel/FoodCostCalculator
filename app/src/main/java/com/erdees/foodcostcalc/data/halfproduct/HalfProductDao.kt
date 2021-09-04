package com.erdees.foodcostcalc.data.halfproduct

import androidx.lifecycle.LiveData
import androidx.room.*
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel

@Dao
interface HalfProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHalfProduct(halfProductModel: HalfProductModel)

    @Query("SELECT * FROM HALFPRODUCT ORDER BY name ASC")
    fun getHalfProducts(): LiveData<List<HalfProductModel>>

    @Update
    suspend fun editHalfProduct(halfProductModel: HalfProductModel)

    @Delete
    suspend fun deleteHalfProduct(halfProductModel: HalfProductModel)
}
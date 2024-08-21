package com.erdees.foodcostcalc.data.db.dao.halfproduct

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.erdees.foodcostcalc.data.model.HalfProductBase
import com.erdees.foodcostcalc.data.model.joined.CompleteHalfProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface HalfProductDao {

  @Transaction
  @Query("SELECT * FROM HalfProduct ORDER BY name ASC")
  fun getCompleteHalfProducts(): Flow<List<CompleteHalfProduct>>

  @Query("SELECT * FROM HalfProduct ORDER BY name ASC")
  fun getHalfProductBase(): Flow<List<HalfProductBase>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun addHalfProduct(halfProductBase: HalfProductBase)

  @Update
  suspend fun editHalfProduct(halfProductBase: HalfProductBase)

  @Query("DELETE FROM HalfProduct WHERE HalfProductId =:id")
  suspend fun deleteHalfProduct(id: Long)

  @Query("DELETE FROM Product_HalfProduct WHERE halfProductId = :id")
  suspend fun deleteProductHalfProduct(id: Long)

  @Transaction
  suspend fun deleteHalfProductWithRelations(id: Long) {
    deleteHalfProduct(id)
    deleteProductHalfProduct(id)
  }
}

package com.erdees.foodcostcalc.data.db.dao.halfproduct

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.erdees.foodcostcalc.data.model.HalfProduct
import com.erdees.foodcostcalc.data.model.joined.HalfProductWithProducts
import kotlinx.coroutines.flow.Flow

@Dao
interface HalfProductDao {

  @Query("SELECT * FROM HalfProduct ORDER BY name ASC")
  fun getHalfProducts(): Flow<List<HalfProductWithProducts>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun addHalfProduct(halfProduct: HalfProduct)

  @Update
  suspend fun editHalfProduct(halfProduct: HalfProduct)

  @Query("DELETE FROM HalfProduct WHERE HalfProductId =:id")
  suspend fun deleteHalfProduct(id: Long)

  @Query("SELECT * FROM Product_HalfProduct WHERE halfProductId = :id")
  suspend fun deleteProductHalfProduct(id: Long)

  @Transaction
  suspend fun deleteHalfProductWithRelations(id: Long) {
    deleteHalfProduct(id)
    deleteProductHalfProduct(id)
  }
}

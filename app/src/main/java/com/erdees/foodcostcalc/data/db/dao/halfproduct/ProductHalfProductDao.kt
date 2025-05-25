package com.erdees.foodcostcalc.data.db.dao.halfproduct

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.erdees.foodcostcalc.data.model.local.associations.ProductHalfProduct

@Dao
interface ProductHalfProductDao {
  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun addProductHalfProduct(productHalfProduct: ProductHalfProduct)

  @Update
  suspend fun updateProductHalfProduct(productHalfProduct: ProductHalfProduct)

  @Query("DELETE FROM Product_HalfProduct WHERE productHalfProductId = :id")
  suspend fun delete(id: Long)

  @Delete
  suspend fun deleteProductHalfProduct(productHalfProduct: ProductHalfProduct)
}

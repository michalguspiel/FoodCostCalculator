package com.erdees.foodcostcalc.data.db.dao.product

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.erdees.foodcostcalc.data.model.ProductBase
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
  @Query("SELECT * from products ORDER BY product_name ASC")
  fun getProducts(): Flow<List<ProductBase>>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun addProduct(product: ProductBase)

  @Update
  suspend fun editProduct(newProduct: ProductBase)

  @Delete
  suspend fun deleteProduct(product: ProductBase)
}
package com.erdees.foodcostcalc.data.db.dao.dish

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.erdees.foodcostcalc.data.model.ProductDish
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDishDao {
  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun addProductDish(productDish: ProductDish)

  @Update
  suspend fun updateProductDish(productDish: ProductDish)

  @Query("DELETE FROM Product_Dish WHERE productDishId = :id")
  suspend fun delete(id: Long)

  @Delete
  suspend fun deleteProductDish(productDish: ProductDish)
}

package com.erdees.foodcostcalc.data.db.dao.dish

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.erdees.foodcostcalc.data.model.HalfProductDish
import kotlinx.coroutines.flow.Flow

@Dao
interface HalfProductDishDao {

  @Query("SELECT * FROM HalfProduct_Dish WHERE dishId = :dishId")
  fun getHalfProductDishes(dishId: Long): Flow<List<HalfProductDish>>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun addHalfProductDish(halfProductDish: HalfProductDish)

  @Update
  suspend fun updateHalfProductDish(halfProductDish: HalfProductDish)

  @Query("DELETE FROM HalfProduct_Dish WHERE halfProductDishId = :id")
  suspend fun delete(id: Long)

  @Query("DELETE FROM HalfProduct_Dish WHERE dishId = :id")
  suspend fun deleteByDishId(id: Long)

  @Query("DELETE FROM HalfProduct_Dish WHERE halfProductId = :id")
  suspend fun deleteByHalfProductId(id: Long)
}

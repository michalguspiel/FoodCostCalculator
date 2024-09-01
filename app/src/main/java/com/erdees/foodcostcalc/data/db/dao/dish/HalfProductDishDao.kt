package com.erdees.foodcostcalc.data.db.dao.dish

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.erdees.foodcostcalc.data.model.associations.HalfProductDish
import kotlinx.coroutines.flow.Flow

@Dao
interface HalfProductDishDao {

  @Query("SELECT * FROM HalfProduct_Dish WHERE dishId = :dishId")
  fun getHalfProductDishes(dishId: Long): Flow<List<HalfProductDish>>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun addHalfProductDish(halfProductDish: HalfProductDish)

  @Update
  suspend fun updateHalfProductDish(halfProductDish: HalfProductDish)

  @Delete
  suspend fun delete(halfProductDish: HalfProductDish)
}

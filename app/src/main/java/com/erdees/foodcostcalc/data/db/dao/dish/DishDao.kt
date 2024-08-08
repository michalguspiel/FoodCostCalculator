package com.erdees.foodcostcalc.data.db.dao.dish

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.erdees.foodcostcalc.data.model.Dish
import com.erdees.foodcostcalc.data.model.joined.CompleteDish
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {

  @Query("SELECT * FROM dishes ORDER BY dish_name ASC")
  fun getDishes(): Flow<List<Dish>>

  @Query("SELECT * FROM DISHES ORDER BY DISH_NAME ASC")
  fun getCompleteDishes(): Flow<List<CompleteDish>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun addDish(dish: Dish)

  @Update
  suspend fun editDish(dish: Dish)

  @Query("DELETE FROM dishes WHERE dishId =:id")
  suspend fun deleteDish(id: Long)

  @Query("DELETE FROM HalfProduct_Dish WHERE dishId = :id")
  suspend fun deleteHalfProductDish(id: Long)

  @Query("DELETE FROM Product_Dish WHERE dishId = :id")
  suspend fun deleteProductDish(id: Long)

  @Query("SELECT * FROM DISHES WHERE dishId = :dishId ")
  suspend fun getCompleteDishByID(dishId: Long): CompleteDish

  @Transaction
  suspend fun deleteDishWithRelations(dishId: Long) {
    deleteDish(dishId)
    deleteHalfProductDish(dishId)
    deleteProductDish(dishId)
  }
}

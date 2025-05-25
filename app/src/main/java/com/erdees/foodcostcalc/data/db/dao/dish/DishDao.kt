package com.erdees.foodcostcalc.data.db.dao.dish

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.joined.CompleteDish
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Transaction
    @Query("SELECT * FROM DISHES ORDER BY DISH_NAME ASC")
    fun getCompleteDishes(): Flow<List<CompleteDish>>

    @Transaction
    @Query("SELECT * FROM dishes WHERE dishId =:id")
    fun getCompleteDish(id: Long): Flow<CompleteDish>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDish(dish: DishBase)

    @Update
    suspend fun editDish(dish: DishBase)

    @Query("DELETE FROM dishes WHERE dishId =:id")
    suspend fun deleteDish(id: Long)

    @Query("UPDATE dishes SET recipeId=:recipeId WHERE dishId=:dishId ")
    suspend fun update(recipeId: Long, dishId: Long)
}
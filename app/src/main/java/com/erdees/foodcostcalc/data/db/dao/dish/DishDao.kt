package com.erdees.foodcostcalc.data.db.dao.dish

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.erdees.foodcostcalc.data.model.DishBase
import com.erdees.foodcostcalc.data.model.joined.CompleteDish
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Transaction
    @Query("SELECT * FROM DISHES ORDER BY DISH_NAME ASC")
    fun getCompleteDishes(): Flow<List<CompleteDish>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDish(dish: DishBase)

    @Update
    suspend fun editDish(dish: DishBase)

    @Query("DELETE FROM dishes WHERE dishId =:id")
    suspend fun deleteDish(id: Long)
}
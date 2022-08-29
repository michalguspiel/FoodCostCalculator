package com.erdees.foodcostcalc.data.dish

import androidx.lifecycle.LiveData
import androidx.room.*
import com.erdees.foodcostcalc.domain.model.dish.DishModel

@Dao
interface DishDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDish(dishModel: DishModel)

    @Query("SELECT * FROM dishes ORDER BY dish_name ASC")
    fun getDishes(): LiveData<List<DishModel>>

    @Update
    suspend fun editDish(dishModel: DishModel)

    @Delete
    suspend fun deleteDish(dishModel: DishModel)

}

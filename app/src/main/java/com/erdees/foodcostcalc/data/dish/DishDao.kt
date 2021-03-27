package com.erdees.foodcostcalc.data.dish

import androidx.lifecycle.LiveData
import androidx.room.*
import com.erdees.foodcostcalc.model.Dish
import com.erdees.foodcostcalc.model.DishWithProductsIncluded
import com.erdees.foodcostcalc.model.GrandDish
import com.erdees.foodcostcalc.model.ProductIncluded

@Dao
interface DishDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDish(dish: Dish)

    @Query("SELECT * FROM dishes ORDER BY dish_name ASC")
    fun getDishes(): LiveData<List<Dish>>

    @Update
    suspend fun editDish(dish: Dish)

    @Delete
    suspend fun deleteDish(dish: Dish)

}
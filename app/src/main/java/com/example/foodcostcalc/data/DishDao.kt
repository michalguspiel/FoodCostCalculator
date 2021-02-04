package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.DishWithProductsIncluded
import com.example.foodcostcalc.model.ProductIncluded

@Dao
interface DishDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addDish(dish: Dish)

    @Query("SELECT * FROM dishes ORDER BY dish_name ASC" )
    fun getDishes(): LiveData<List<Dish>>

    @Update
    suspend fun editDish(dish: Dish)

    @Delete
    suspend fun deleteDish(dish: Dish)

    @Transaction
    @Query("SELECT * FROM dishes ORDER BY dish_name ASC")
    fun getDishesWithProductsIncluded(): LiveData<List<DishWithProductsIncluded>>

    @Transaction
    @Query("SELECT * FROM productincluded WHERE dishOwnerId = :dishId ORDER BY product_name ASC")
    fun getIngredientsFromDish(dishId: Long): LiveData<List<ProductIncluded>>

    @Update
    suspend fun editProductsIncluded(productIncluded: ProductIncluded)

    @Delete
    suspend fun deleteProductIncluded(productIncluded: ProductIncluded)


}
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

    @Query("SELECT * FROM dishes ORDER BY dish_name ASC")
    fun getDishes(): LiveData<List<Dish>>

    @Update
    suspend fun editDish(dish: Dish)

    @Delete
    suspend fun deleteDish(dish: Dish)

    @Transaction
    @Query("SELECT * FROM dishes ORDER BY dish_name ASC")
    fun getDishesWithProductsIncluded(): LiveData<List<DishWithProductsIncluded>>

    @Transaction
    @Query("SELECT * FROM dishes WHERE dish_name = :name  ORDER BY dish_name ASC")
    fun getDishesByName(name: String): LiveData<List<DishWithProductsIncluded>>


    @Query("SELECT * FROM productincluded ORDER BY product_name ASC")
    fun getAllProductsIncluded(): LiveData<List<ProductIncluded>>

    @Transaction
    @Query("SELECT * FROM productincluded WHERE dishOwnerId = :dishId ORDER BY product_name ASC")
    fun getIngredientsFromDish(dishId: Long): LiveData<List<ProductIncluded>>

    @Transaction
    @Query("SELECT * FROM productincluded WHERE productId = :id ")
    fun getCertainProductsIncluded(id: Long) : LiveData<List<ProductIncluded>>

    @Transaction
    @Query("SELECT * FROM productincluded WHERE dishOwnerId = :id ")
    fun  getProductIncludedFromDishId(id: Long) : LiveData<List<ProductIncluded>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProductToDish(productIncluded: ProductIncluded)

    @Update
    suspend fun editProductsIncluded(productIncluded: ProductIncluded)

    @Delete
    suspend fun deleteProductIncluded(productIncluded: ProductIncluded)



}
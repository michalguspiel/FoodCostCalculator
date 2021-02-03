package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product

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

    /**
    fun addProductToDish(dish: Dish, product: Product, weight: Double){
        dish.productsIncluded.add(product)
        dish.weightList.add(weight)
        dishes.value = dishList
    }

    fun editDish(dish: Dish,listOfProducts: MutableList<Pair<Product, Double>>) {
        dish.productsIncluded.clear()
        dish.weightList.clear()
        dish.productsIncluded.addAll(listOfProducts.map { it.first })
        dish.weightList.addAll(listOfProducts.map{it.second})
        dishes.value = dishList

    }


    fun deleteProductFromDish(dish:Dish ,product: Product){
        dish.productsIncluded.remove(product)
        dishes.value = dishList
    }
*/
}
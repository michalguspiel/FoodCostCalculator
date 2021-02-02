package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product

/** DATA ACCESS OBJECT */
@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProduct(product: Product)

    @Query("SELECT * from products ORDER BY name ASC")
    fun getProducts(): LiveData<List<Product>>

    @Update
    suspend fun editProduct(newProduct: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProductToDish(productIncluded: ProductIncluded)





}

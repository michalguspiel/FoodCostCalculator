package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.foodcostcalc.model.Product
import com.example.foodcostcalc.model.ProductIncluded

/** DATA ACCESS OBJECT */
@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProduct(product: Product)

    @Query("SELECT * from products ORDER BY product_name ASC")
    fun getProducts(): LiveData<List<Product>>

    @Update
    suspend fun editProduct(newProduct: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProductToDish(productIncluded: ProductIncluded)

    @Query("SELECT * FROM products WHERE productId = :id")
    fun getProduct(id: Long): LiveData<Product>





}

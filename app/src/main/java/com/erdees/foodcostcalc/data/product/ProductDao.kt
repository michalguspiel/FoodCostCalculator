package com.erdees.foodcostcalc.data.product

import androidx.lifecycle.LiveData
import androidx.room.*
import com.erdees.foodcostcalc.domain.model.product.ProductModel

/** DATA ACCESS OBJECT */
@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addProduct(productModel: ProductModel)

    @Query("SELECT * from products ORDER BY product_name ASC")
    fun getProducts(): LiveData<List<ProductModel>>

    @Update
    suspend fun editProduct(newProductModel: ProductModel)

    @Delete
    suspend fun deleteProduct(productModel: ProductModel)
}

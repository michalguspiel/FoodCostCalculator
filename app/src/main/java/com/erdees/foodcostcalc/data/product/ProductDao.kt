package com.erdees.foodcostcalc.data.product

import androidx.lifecycle.LiveData
import androidx.room.*
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel

/** DATA ACCESS OBJECT */
@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProduct(productModel: ProductModel)

    @Query("SELECT * from products ORDER BY product_name ASC")
    fun getProducts(): LiveData<List<ProductModel>>

    @Update
    suspend fun editProduct(newProductModel: ProductModel)

    @Delete
    suspend fun deleteProduct(productModel: ProductModel)

}

package com.example.foodcostcalc.data.productIncludedInHalfProduct

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.foodcostcalc.model.ProductIncluded
import com.example.foodcostcalc.model.ProductIncludedInHalfProduct

@Dao
interface ProductIncludedInHalfProductDao {

    @Query("SELECT * FROM PRODUCTINCLUDEDINHALFPRODUCT ORDER BY product_name ASC")
    fun getAllProductIncludedInHalfProduct(): LiveData<List<ProductIncludedInHalfProduct>>

    @Query("SELECT * FROM PRODUCTINCLUDEDINHALFPRODUCT")
    fun getAllProductIncludedInHalfProductNotAsc(): LiveData<List<ProductIncludedInHalfProduct>>

    @Query("SELECT * FROM productincludedinhalfproduct WHERE productId = :productId")
    fun getCertainProductsIncluded(productId: Long): LiveData<List<ProductIncludedInHalfProduct>>

    @Insert
    suspend fun addProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct)

    @Update
    suspend fun editProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct)

    @Delete
    suspend fun deleteProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct)

    @Transaction
    @Query("SELECT * FROM ProductIncludedInHalfProduct WHERE halfProductId = :halfProductId ORDER BY product_name ASC")
    fun getProductsFromHalfProduct(halfProductId: Long): LiveData<List<ProductIncludedInHalfProduct>>


}
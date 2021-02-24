package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.foodcostcalc.model.ProductIncludedInHalfProduct

@Dao
interface ProductIncludedInHalfProductDao {

@Query("SELECT * FROM PRODUCTINCLUDEDINHALFPRODUCT ORDER BY product_name ASC")
fun getAllProductIncludedInHalfProduct() : LiveData<List<ProductIncludedInHalfProduct>>

@Insert
suspend fun addProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct)

@Update
suspend fun editProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct)

@Delete
suspend fun deleteProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct)


}
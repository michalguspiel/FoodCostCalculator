package com.erdees.foodcostcalc.data.productIncludedInHalfProduct

import androidx.lifecycle.LiveData
import androidx.room.*
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.ProductIncludedInHalfProductModel

@Dao
interface ProductIncludedInHalfProductDao {

    @Query("SELECT * FROM PRODUCTINCLUDEDINHALFPRODUCT ORDER BY product_name ASC")
    fun getAllProductIncludedInHalfProduct(): LiveData<List<ProductIncludedInHalfProductModel>>

    @Query("SELECT * FROM PRODUCTINCLUDEDINHALFPRODUCT")
    fun getAllProductIncludedInHalfProductNotAsc(): LiveData<List<ProductIncludedInHalfProductModel>>

    @Query("SELECT * FROM productincludedinhalfproduct WHERE productId = :productId")
    fun getCertainProductsIncluded(productId: Long): LiveData<List<ProductIncludedInHalfProductModel>>

    @Insert
    suspend fun addProductIncludedInHalfProduct(productIncludedInHalfProductModel: ProductIncludedInHalfProductModel)

    @Update
    suspend fun editProductIncludedInHalfProduct(productIncludedInHalfProductModel: ProductIncludedInHalfProductModel)

    @Delete
    suspend fun deleteProductIncludedInHalfProduct(productIncludedInHalfProductModel: ProductIncludedInHalfProductModel)

    @Transaction
    @Query("SELECT * FROM PRODUCTINCLUDEDINHALFPRODUCT WHERE halfProductId = :halfProductId ORDER BY product_name ASC")
    fun getProductsFromHalfProduct(halfProductId: Long): LiveData<List<ProductIncludedInHalfProductModel>>


}
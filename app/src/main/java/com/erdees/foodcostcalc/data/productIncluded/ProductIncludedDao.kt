package com.erdees.foodcostcalc.data.productIncluded

import androidx.lifecycle.LiveData
import androidx.room.*
import com.erdees.foodcostcalc.domain.model.product.ProductIncluded

@Dao
interface ProductIncludedDao {
  @Transaction
    @Query("SELECT * FROM productincluded WHERE productId = :id ")
    fun getCertainProductsIncluded(id: Long): LiveData<List<ProductIncluded>>

    @Transaction
    @Query("SELECT * FROM productincluded WHERE dishOwnerId = :id ")
    fun getProductIncludedFromDishId(id: Long): LiveData<List<ProductIncluded>>

    @Query("DELETE FROM productincluded WHERE dishOwnerId = :id")
    fun deleteAllProductsIncludedInDish(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProductToDish(productIncluded: ProductIncluded)

    @Update
    suspend fun editProductsIncluded(productIncluded: ProductIncluded)

    @Delete
    suspend fun deleteProductIncluded(productIncluded: ProductIncluded)
}

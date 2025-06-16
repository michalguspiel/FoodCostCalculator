package com.erdees.foodcostcalc.data.db.dao.product

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.erdees.foodcostcalc.data.model.local.ProductBase
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * from products ORDER BY product_name ASC")
    fun getProducts(): Flow<List<ProductBase>>

    @Query("SELECT * FROM products WHERE productId = :id")
    fun getProduct(id: Long): Flow<ProductBase>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addProduct(product: ProductBase) : Long

    @Update
    suspend fun editProduct(newProduct: ProductBase)

    @Query("DELETE FROM products WHERE productId = :id")
    suspend fun deleteProduct(id: Long)
}
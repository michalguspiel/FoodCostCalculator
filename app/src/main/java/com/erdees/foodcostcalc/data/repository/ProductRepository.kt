package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.dish.ProductDishDao
import com.erdees.foodcostcalc.data.db.dao.product.ProductDao
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ProductRepository {
  val products: Flow<List<ProductBase>>
  suspend fun getProduct(id: Long) : Flow<ProductBase>
  suspend fun addProduct(product: ProductBase): Long
  suspend fun addProductDish(productDish: ProductDish)
  suspend fun editProduct(newProduct: ProductBase)
  suspend fun deleteProduct(id: Long)
}

class ProductRepositoryImpl : ProductRepository, KoinComponent {

  private val productDao: ProductDao by inject()
  private val productDishDao: ProductDishDao by inject()

  override val products: Flow<List<ProductBase>> = productDao.getProducts()

  override suspend fun getProduct(id: Long) = productDao.getProduct(id)

  override suspend fun addProduct(product: ProductBase) = productDao.addProduct(product)

  override suspend fun addProductDish(productDish: ProductDish) =
    productDishDao.addProductDish(productDish)

  override suspend fun editProduct(newProduct: ProductBase) = productDao.editProduct(newProduct)

  override suspend fun deleteProduct(id: Long) = productDao.deleteProduct(id)
}
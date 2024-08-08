package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.product.ProductDao
import com.erdees.foodcostcalc.data.model.Product
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ProductRepository {
  val products: Flow<List<Product>>
  suspend fun addProduct(product: Product)
  suspend fun editProduct(newProduct: Product)
  suspend fun deleteProduct(product: Product)
}

class ProductRepositoryImpl : ProductRepository, KoinComponent {

  private val productDao: ProductDao by inject()

  override val products: Flow<List<Product>> = productDao.getProducts()

  override suspend fun addProduct(product: Product) = productDao.addProduct(product)

  override suspend fun editProduct(newProduct: Product) = productDao.editProduct(newProduct)

  override suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
}

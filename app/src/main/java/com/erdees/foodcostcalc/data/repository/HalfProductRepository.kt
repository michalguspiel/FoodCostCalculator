package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.halfproduct.HalfProductDao
import com.erdees.foodcostcalc.data.model.HalfProduct
import com.erdees.foodcostcalc.data.model.joined.HalfProductWithProducts
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface HalfProductRepository {
  val halfProducts: Flow<List<HalfProductWithProducts>>
  suspend fun addHalfProduct(halfProduct: HalfProduct)
  suspend fun editHalfProduct(halfProduct: HalfProduct)
  suspend fun deleteHalfProduct(id: Long)
}

class HalfProductRepositoryImpl : HalfProductRepository, KoinComponent {

  private val halfProductDao: HalfProductDao by inject()

  override val halfProducts: Flow<List<HalfProductWithProducts>> = halfProductDao.getHalfProducts()

  override suspend fun addHalfProduct(halfProduct: HalfProduct) =
    halfProductDao.addHalfProduct(halfProduct)

  override suspend fun editHalfProduct(halfProduct: HalfProduct) =
    halfProductDao.editHalfProduct(halfProduct)

  override suspend fun deleteHalfProduct(id: Long) {
    halfProductDao.deleteHalfProductWithRelations(id)
  }
}

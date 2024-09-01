package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.dish.HalfProductDishDao
import com.erdees.foodcostcalc.data.db.dao.halfproduct.HalfProductDao
import com.erdees.foodcostcalc.data.db.dao.halfproduct.ProductHalfProductDao
import com.erdees.foodcostcalc.data.model.HalfProductBase
import com.erdees.foodcostcalc.data.model.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.associations.ProductHalfProduct
import com.erdees.foodcostcalc.data.model.joined.CompleteHalfProduct
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface HalfProductRepository {
    val completeHalfProducts: Flow<List<CompleteHalfProduct>>
    val halfProducts: Flow<List<HalfProductBase>>
    suspend fun addHalfProduct(halfProductBase: HalfProductBase)
    suspend fun addHalfProductDish(halfProductDish: HalfProductDish)
    suspend fun addProductHalfProduct(productHalfProduct: ProductHalfProduct)
    suspend fun updateHalfProduct(halfProductBase: HalfProductBase)
    suspend fun deleteHalfProduct(id: Long)

    suspend fun deleteProductHalfProduct(productHalfProduct: ProductHalfProduct)
    suspend fun updateProductHalfProduct(productHalfProduct: ProductHalfProduct)
}

class HalfProductRepositoryImpl : HalfProductRepository, KoinComponent {

    private val halfProductDao: HalfProductDao by inject()
    private val halfProductDishDao: HalfProductDishDao by inject()
    private val productHalfProductDao: ProductHalfProductDao by inject()

    override val completeHalfProducts: Flow<List<CompleteHalfProduct>> =
        halfProductDao.getCompleteHalfProducts()

    override val halfProducts: Flow<List<HalfProductBase>> = halfProductDao.getHalfProductBase()

    override suspend fun addHalfProduct(halfProductBase: HalfProductBase) =
        halfProductDao.addHalfProduct(halfProductBase)

    override suspend fun addHalfProductDish(halfProductDish: HalfProductDish) =
        halfProductDishDao.addHalfProductDish(halfProductDish)

    override suspend fun addProductHalfProduct(productHalfProduct: ProductHalfProduct) {
        productHalfProductDao.addProductHalfProduct(productHalfProduct)
    }

    override suspend fun updateHalfProduct(halfProductBase: HalfProductBase) =
        halfProductDao.editHalfProduct(halfProductBase)

    override suspend fun deleteHalfProduct(id: Long) {
        halfProductDao.deleteHalfProduct(id)
    }

    override suspend fun deleteProductHalfProduct(productHalfProduct: ProductHalfProduct) {
        productHalfProductDao.deleteProductHalfProduct(productHalfProduct)
    }

    override suspend fun updateProductHalfProduct(productHalfProduct: ProductHalfProduct) {
        productHalfProductDao.updateProductHalfProduct(productHalfProduct)
    }
}

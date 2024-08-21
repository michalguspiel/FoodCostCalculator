package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.dish.DishDao
import com.erdees.foodcostcalc.data.db.dao.dish.HalfProductDishDao
import com.erdees.foodcostcalc.data.db.dao.dish.ProductDishDao
import com.erdees.foodcostcalc.data.model.DishBase
import com.erdees.foodcostcalc.data.model.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.associations.ProductDish
import com.erdees.foodcostcalc.data.model.joined.CompleteDish
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface DishRepository {
  val dishes: Flow<List<CompleteDish>>

  suspend fun getDishById(dishId: Long): CompleteDish?

  suspend fun addDish(dish: DishBase)
  suspend fun deleteDish(dish: DishBase)
  suspend fun deleteDish(dishId: Long)
  suspend fun updateDish(dish: DishBase)

  suspend fun deleteProductDish(productDish: ProductDish)
  suspend fun deleteProductDish(id: Long)

  suspend fun deleteHalfProductDish(halfProductDish: HalfProductDish)
  suspend fun deleteHalfProductDish(id: Long)

  suspend fun updateProductDish(productDish: ProductDish)
  suspend fun updateHalfProductDish(halfProductDish: HalfProductDish)
}

class DishRepositoryImpl : DishRepository, KoinComponent {

  private val dishDao: DishDao by inject()
  private val productDishDao: ProductDishDao by inject()
  private val halfProductDishDao: HalfProductDishDao by inject()

  override val dishes: Flow<List<CompleteDish>> = dishDao.getCompleteDishes()

  override suspend fun getDishById(dishId: Long): CompleteDish? {
    return dishDao.getCompleteDishByID(dishId)
  }

  override suspend fun addDish(dish: DishBase) {
    dishDao.addDish(dish)
  }

  override suspend fun deleteDish(dish: DishBase) {
    dishDao.deleteDishWithRelations(dish.dishId)
  }

  override suspend fun deleteDish(dishId: Long) {
    dishDao.deleteDishWithRelations(dishId)
  }

  override suspend fun updateDish(dish: DishBase) {
    dishDao.editDish(dish)
  }

  override suspend fun deleteProductDish(productDish: ProductDish) {
    productDishDao.deleteProductDish(productDish)
  }

  override suspend fun deleteProductDish(id: Long) {
    productDishDao.delete(id)
  }

  override suspend fun deleteHalfProductDish(halfProductDish: HalfProductDish) {
    halfProductDishDao.delete(halfProductDish)
  }

  override suspend fun deleteHalfProductDish(id: Long) {
    halfProductDishDao.delete(id)
  }

  override suspend fun updateProductDish(productDish: ProductDish) {
    productDishDao.updateProductDish(productDish)
  }

  override suspend fun updateHalfProductDish(halfProductDish: HalfProductDish) {
    halfProductDishDao.updateHalfProductDish(halfProductDish)
  }
}

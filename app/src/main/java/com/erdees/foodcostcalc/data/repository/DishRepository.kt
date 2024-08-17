package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.dish.DishDao
import com.erdees.foodcostcalc.data.model.DishBase
import com.erdees.foodcostcalc.data.model.joined.CompleteDish
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface DishRepository {
  val dishes: Flow<List<CompleteDish>>
  suspend fun addDish(dish: DishBase)
  suspend fun deleteDish(dish: DishBase)
  suspend fun editDish(dish: DishBase)
}

class DishRepositoryImpl : DishRepository, KoinComponent {

  private val dishDao: DishDao by inject()

  override val dishes: Flow<List<CompleteDish>> = dishDao.getCompleteDishes()

  override suspend fun addDish(dish: DishBase) {
    dishDao.addDish(dish)
  }

  override suspend fun deleteDish(dish: DishBase) {
    dishDao.deleteDishWithRelations(dish.dishId)
  }

  override suspend fun editDish(dish: DishBase) {
    dishDao.editDish(dish)
  }
}

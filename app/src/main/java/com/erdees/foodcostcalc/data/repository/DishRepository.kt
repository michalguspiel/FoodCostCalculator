package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.dish.DishDao
import com.erdees.foodcostcalc.data.model.Dish
import com.erdees.foodcostcalc.data.model.joined.CompleteDish
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface DishRepository {
  val dishes: Flow<List<CompleteDish>>
  suspend fun addDish(dish: Dish)
  suspend fun deleteDish(dish: Dish)
  suspend fun editDish(dish: Dish)
}

class DishRepositoryImpl : DishRepository, KoinComponent {

  private val dishDao: DishDao by inject()

  override val dishes: Flow<List<CompleteDish>> = dishDao.getCompleteDishes()

  override suspend fun addDish(dish: Dish) {
    dishDao.addDish(dish)
  }

  override suspend fun deleteDish(dish: Dish) {
    dishDao.deleteDishWithRelations(dish.dishId)
  }

  override suspend fun editDish(dish: Dish) {
    dishDao.editDish(dish)
  }
}

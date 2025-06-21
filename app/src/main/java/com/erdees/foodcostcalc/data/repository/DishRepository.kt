package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.dish.DishDao
import com.erdees.foodcostcalc.data.db.dao.dish.HalfProductDishDao
import com.erdees.foodcostcalc.data.db.dao.dish.ProductDishDao
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish
import com.erdees.foodcostcalc.data.model.local.joined.CompleteDish
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface DishRepository {
    val dishes: Flow<List<CompleteDish>>
    suspend fun getDish(id: Long): Flow<CompleteDish>

    suspend fun addDish(dish: DishBase)
    suspend fun deleteDish(dishId: Long)
    suspend fun updateDish(dish: DishBase)
    suspend fun updateDishRecipe(recipeId: Long, dishId: Long)
    suspend fun deleteProductDish(productDish: ProductDish)
    suspend fun deleteHalfProductDish(halfProductDish: HalfProductDish)
    suspend fun updateProductDish(productDish: ProductDish)
    suspend fun updateHalfProductDish(halfProductDish: HalfProductDish)
}

class DishRepositoryImpl : DishRepository, KoinComponent {

    private val dishDao: DishDao by inject()
    private val productDishDao: ProductDishDao by inject()
    private val halfProductDishDao: HalfProductDishDao by inject()

    override val dishes: Flow<List<CompleteDish>> = dishDao.getCompleteDishes()

    override suspend fun getDish(id: Long) = dishDao.getCompleteDish(id)

    override suspend fun addDish(dish: DishBase): Long = dishDao.addDish(dish)

    override suspend fun deleteDish(dishId: Long) {
        dishDao.deleteDish(dishId)
    }

    override suspend fun updateDish(dish: DishBase) {
        dishDao.editDish(dish)
    }

    override suspend fun updateDishRecipe(recipeId: Long, dishId: Long) {
        dishDao.update(recipeId, dishId)
    }

    override suspend fun deleteProductDish(productDish: ProductDish) {
        productDishDao.deleteProductDish(productDish)
    }

    override suspend fun deleteHalfProductDish(halfProductDish: HalfProductDish) {
        halfProductDishDao.delete(halfProductDish)
    }

    override suspend fun updateProductDish(productDish: ProductDish) {
        productDishDao.updateProductDish(productDish)
    }

    override suspend fun updateHalfProductDish(halfProductDish: HalfProductDish) {
        halfProductDishDao.updateHalfProductDish(halfProductDish)
    }
}

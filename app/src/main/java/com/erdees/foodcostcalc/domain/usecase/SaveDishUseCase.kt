package com.erdees.foodcostcalc.domain.usecase

import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishBase
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDish
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDish
import com.erdees.foodcostcalc.domain.model.dish.DishActionResult
import com.erdees.foodcostcalc.domain.model.dish.DishDetailsActionResultType
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.utils.MyDispatchers
import kotlinx.coroutines.withContext

class SaveDishUseCase(
    private val dishRepository: DishRepository,
    private val productRepository: ProductRepository,
    private val halfProductRepository: HalfProductRepository,
    private val myDispatchers: MyDispatchers
) {
    suspend operator fun invoke(
        dish: DishDomain,
        originalProducts: List<UsedProductDomain>,
        originalHalfProducts: List<UsedHalfProductDomain>,
        actionResultType: DishDetailsActionResultType
    ): Result<DishActionResult> = withContext(myDispatchers.ioDispatcher) {
        try {
            val editedProducts =
                dish.products.filterNot { it in originalProducts }.map { it.toProductDish() }
            val editedHalfProducts = dish.halfProducts.filterNot { it in originalHalfProducts }
                .map { it.toHalfProductDish() }

            val removedProducts = originalProducts.filterNot {
                it.id in dish.products.map { product -> product.id }
            }.map { it.toProductDish() }

            val removedHalfProducts = originalHalfProducts.filterNot {
                it.id in dish.halfProducts.map { halfProduct -> halfProduct.id }
            }.map { it.toHalfProductDish() }

            // Process all operations
            removedProducts.forEach { dishRepository.deleteProductDish(it) }
            removedHalfProducts.forEach { dishRepository.deleteHalfProductDish(it) }

            editedProducts.forEach { dishRepository.updateProductDish(it) }
            editedHalfProducts.forEach { dishRepository.updateHalfProductDish(it) }

            dish.productsNotSaved.map { it.toProductDish(dish.id) }.forEach {
                productRepository.addProductDish(it)
            }
            dish.halfProductsNotSaved.map { it.toHalfProductDish(dish.id) }.forEach {
                halfProductRepository.addHalfProductDish(it)
            }

            dishRepository.updateDish(dish.toDishBase())

            Result.success(
                DishActionResult(
                    type = actionResultType,
                    dishId = dish.id
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

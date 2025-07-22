package com.erdees.foodcostcalc.domain.usecase

import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.domain.model.dish.DishActionResult
import com.erdees.foodcostcalc.domain.model.dish.DishDetailsActionResultType
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.MyDispatchers
import kotlinx.coroutines.withContext

/**
 * Use case to handle dish deletion including any associated analytics tracking
 */
class DeleteDishUseCase(
    private val dishRepository: DishRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val myDispatchers: MyDispatchers
) {
    suspend operator fun invoke(dishId: Long): Result<DishActionResult> =
        withContext(myDispatchers.ioDispatcher) {
            try {
                dishRepository.deleteDish(dishId)
                logDishDeletionEvent(dishRepository.getDishCount())

                Result.success(
                    DishActionResult(
                        type = DishDetailsActionResultType.DELETED,
                        dishId = dishId
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun logDishDeletionEvent(dishCount: Int) {
        analyticsRepository.logEvent(Constants.Analytics.DishV2.DELETED, null)
        analyticsRepository.setUserProperty(
            Constants.Analytics.UserProperties.DISH_COUNT,
            dishCount.toString()
        )
    }
}

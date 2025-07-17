package com.erdees.foodcostcalc.domain.usecase

import android.content.Context
import android.content.Intent
import android.icu.util.Currency
import android.os.Bundle
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.ext.toShareableText
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.MyDispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Use case that handles sharing dish information.
 * This includes generating the text to share, logging analytics, and creating the share intent.
 */
class ShareDishUseCase(
    private val analyticsRepository: AnalyticsRepository,
    private val myDispatchers: MyDispatchers
) {
    /**
     * Share dish information using Android's share functionality
     *
     * @param context Android context needed to start the share intent
     * @param dish The dish to be shared
     * @param currency Currency information for formatting prices
     * @return Result with either success (with the generated share intent) or failure
     */
    suspend operator fun invoke(
        context: Context,
        dish: DishDomain,
        currency: Currency?
    ): Result<Intent> = withContext(myDispatchers.ioDispatcher) {
        try {
            // Log analytics event for dish sharing
            logShareEvent(dish.name)

            // Generate shareable text representation of the dish
            val shareableText = dish.toShareableText(context, currency).also {
                Timber.i(it)
            }

            // Create and configure the share intent
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareableText)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            Result.success(shareIntent)
        } catch (e: Exception) {
            Timber.e(e, "Error sharing dish")
            Result.failure(e)
        }
    }

    private fun logShareEvent(dishName: String?) {
        analyticsRepository.logEvent(Constants.Analytics.DISH_SHARE, Bundle().apply {
            putString(Constants.Analytics.DISH_NAME, dishName)
        })
    }
}

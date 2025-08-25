package com.erdees.foodcostcalc.ui.navigation

import androidx.annotation.Keep
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

annotation class Screen(val description: String = "Screen annotation for screen composable that doesn't require modifier")

@Keep
@Serializable
sealed class FCCScreen(
    val nameStringRes: Int = -1,
    val iconResourceId: Int = -1,
) {
    /**Bottom nav*/
    @Keep
    @Serializable
    data object Products : FCCScreen(R.string.products, R.drawable.products)

    @Keep
    @Serializable
    data object HalfProducts : FCCScreen(R.string.half_products, R.drawable.half_products)

    @Keep
    @Serializable
    data object Dishes : FCCScreen(R.string.dishes, R.drawable.dishes)

    @Keep
    @Serializable
    data object Settings : FCCScreen(R.string.settings, R.drawable.settings)

    /** Rest */
    @Serializable
    data object CreateIngredient : FCCScreen()

    @Keep
    @Serializable
    data object DataBackup : FCCScreen(R.string.data_backup, R.drawable.online)

    @Serializable
    data class AddItemToHalfProduct(val id: Long, val name: String, val unit: MeasurementUnit) : FCCScreen()

    @Serializable
    data class AddItemsToDish(val dishId: Long, val dishName: String) : FCCScreen()

    @Serializable
    data class DishDetails(
        @SerialName(DISH_ID_KEY) val dishId: Long,
        @SerialName(IS_COPIED) val isCopied: Boolean = false
    ) : FCCScreen()

    @Serializable
    data object CreateDishStart : FCCScreen()

    @Serializable
    data object CreateDishSummary : FCCScreen()

    @Serializable
    data class EditHalfProduct(@SerialName(HALF_PRODUCT_ID_KEY) val halfProductId: Long) :
        FCCScreen()

    @Serializable
    data class EditProduct(@SerialName(PRODUCT_ID_KEY) val productId: Long) : FCCScreen()

    @Serializable
    data object Subscription : FCCScreen()

    @Serializable
    data object Recipe : FCCScreen()

    @Serializable
    data object FeatureRequest : FCCScreen()

    @Serializable
    data object FeatureRequestList : FCCScreen()

    @Serializable
    data object Onboarding : FCCScreen()

    companion object {
        const val IS_COPIED = "isCopied"
        const val DISH_ID_KEY = "dishId"
        const val PRODUCT_ID_KEY = "productId"
        const val HALF_PRODUCT_ID_KEY = "halfProductId"
    }
}
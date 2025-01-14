package com.erdees.foodcostcalc.ui.navigation

import androidx.annotation.Keep
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import kotlinx.serialization.Serializable

@Keep
@Serializable
sealed class FCCScreen(
    val nameStringRes: Int?,
    val iconResourceId: Int? = null,
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

    /** Rest */
    @Serializable
    data object CreateProduct : FCCScreen(null, null)

    @Keep
    @Serializable
    data object Settings : FCCScreen(R.string.settings, R.drawable.settings)

    @Keep
    @Serializable
    data object DataBackup : FCCScreen(R.string.data_backup, R.drawable.online)

    @Serializable
    data class AddItemToHalfProduct(val halfProductDomain: HalfProductDomain) :
        FCCScreen(null, null)

    @Serializable
    data class AddItemsToDish(val dishId: Long, val dishName: String) :
        FCCScreen(null, null)

    @Serializable
    data class EditDish(val dishDomain: DishDomain) : FCCScreen(null, null)

    @Serializable
    data object CreateDish : FCCScreen(null, null)

    @Serializable
    data class EditHalfProduct(val halfProductDomain: HalfProductDomain) :
        FCCScreen(null, null)

    @Serializable
    data class EditProduct(val productDomain: ProductDomain) : FCCScreen(null, null)

    @Serializable
    data object Subscription : FCCScreen(null, null)

    companion object {
        val bottomNavigationScreens = listOf(Products, HalfProducts, Dishes, Settings)
    }
}

package com.erdees.foodcostcalc.ui.navigation

import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import kotlinx.serialization.Serializable

// Todo, name as string resource, icon optional
@Serializable
sealed class FCCScreen(
    val name: String,
    val iconResourceId: Int? = null,
) {
    /**Bottom nav*/
    @Serializable
    data object Products : FCCScreen("Products", R.drawable.products)

    @Serializable
    data object HalfProducts : FCCScreen("Half Products", R.drawable.half_products)

    @Serializable
    data object Dishes : FCCScreen("Dishes", R.drawable.dishes)

    /** Rest */
    @Serializable
    data object CreateProduct : FCCScreen("Create Product")

    @Serializable
    data object Settings : FCCScreen("Settings", R.drawable.settings)

    @Serializable
    data object OnlineData : FCCScreen("Online Data", R.drawable.online)

    @Serializable
    data class AddItemToHalfProduct(val halfProductDomain: HalfProductDomain) :
        FCCScreen("Add Half Product")

    @Serializable
    data class AddItemsToDish(val dishId: Long, val dishName: String) :
        FCCScreen("Add Items to Dish")

    @Serializable
    data class EditDish(val dishDomain: DishDomain) : FCCScreen("Edit Dish")

    @Serializable
    data object CreateDish : FCCScreen("Add Dish")

    @Serializable
    data class EditHalfProduct(val halfProductDomain: HalfProductDomain) :
        FCCScreen("Edit Half Product")

    @Serializable
    data class EditProduct(val productDomain: ProductDomain) : FCCScreen("Edit Product")

    companion object {
        val bottomNavigationScreens = listOf(Products, HalfProducts, Dishes, Settings)
    }
}

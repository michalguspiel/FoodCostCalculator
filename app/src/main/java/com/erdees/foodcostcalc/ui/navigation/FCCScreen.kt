package com.erdees.foodcostcalc.ui.navigation

import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import kotlinx.serialization.Serializable

// Todo, name as string resource, icon optional
@Serializable
sealed class FCCScreen(val name: String, val iconResourceId: Int? = null) {
  /**Bottom nav*/
  @Serializable
  data object Products : FCCScreen("Products", R.drawable.ic_apple_1_)

  @Serializable
  data object HalfProducts : FCCScreen("Half Products", R.drawable.ic_jam)

  @Serializable
  data object Dishes : FCCScreen("Dishes", R.drawable.ic_food)

  /** Rest */
  @Serializable
  data object CreateProduct : FCCScreen("Create Product")

  @Serializable
  data object Settings : FCCScreen("Settings", R.drawable.ic_settings)

  @Serializable
  data object OnlineData : FCCScreen("Online Data", R.drawable.ic_browser)

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

  companion object {
    val bottomNavigationScreens = listOf(Products, HalfProducts, Dishes, Settings)
  }
}

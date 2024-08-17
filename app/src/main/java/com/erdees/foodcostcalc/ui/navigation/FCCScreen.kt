package com.erdees.foodcostcalc.ui.navigation

import com.erdees.foodcostcalc.R
import kotlinx.serialization.Serializable

// Todo, name as string resource
@Serializable
sealed class FCCScreen(val name: String, val iconResourceId: Int) {
  /**Bottom nav*/
  @Serializable
  data object Products : FCCScreen("Products", R.drawable.ic_apple_1_)

  @Serializable
  data object HalfProducts : FCCScreen("Half Products", R.drawable.ic_jam)

  @Serializable
  data object Dishes : FCCScreen("Dishes", R.drawable.ic_food)

  /** Rest */
  @Serializable
  data object CreateProduct : FCCScreen("Create Product", R.drawable.ic_add)

  @Serializable
  data object CreateHalfProduct :
    FCCScreen("Create Half Product", R.drawable.ic_croissant)

  @Serializable
  data object Settings : FCCScreen("Settings", R.drawable.ic_settings)

  @Serializable
  data object OnlineData : FCCScreen("Online Data", R.drawable.ic_browser)

  @Serializable
  data object AddProductToHalfProduct :
    FCCScreen("Add Half Product", R.drawable.ic_bread)

  @Serializable
  data class AddItemsToDish(val dishId: Long, val dishName: String) :
    FCCScreen("Add Items to Dish", R.drawable.ic_grocery)

  @Serializable
  data object CreateDish : FCCScreen("Add Dish", R.drawable.ic_serving_dish)

  companion object {
    val bottomNavigationScreens = listOf(Products, HalfProducts, Dishes, Settings)
  }
}

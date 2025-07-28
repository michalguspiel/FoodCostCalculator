package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.content.Context
import com.erdees.foodcostcalc.R

object DishDetailsUtil {
    fun getCopyDishPrefilledName(name: String?, context: Context): String {
        return context.getString(R.string.copy_dish_prefilled_name, name)
    }
}

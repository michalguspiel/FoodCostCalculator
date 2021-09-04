package com.erdees.foodcostcalc.data.grandDish

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.GrandDishModel

@Dao
interface GrandDishDao {
    @Transaction
    @Query("SELECT * FROM DISHES ORDER BY DISH_NAME ASC")
    fun getGrandDishes(): LiveData<List<GrandDishModel>>

    @Query("SELECT * FROM DISHES WHERE dishId = :dishId ")
    fun getGrandDishByDishID(dishId: Long): LiveData<GrandDishModel>
}
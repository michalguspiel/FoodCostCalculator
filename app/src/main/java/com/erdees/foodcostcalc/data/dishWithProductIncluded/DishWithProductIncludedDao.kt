package com.erdees.foodcostcalc.data.dishWithProductIncluded

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.DishWithProductsIncludedModel

@Dao
interface DishWithProductIncludedDao {

    @Transaction
    @Query("SELECT * FROM dishes ORDER BY dish_name ASC")
    fun getDishesWithProductsIncluded(): LiveData<List<DishWithProductsIncludedModel>>


}
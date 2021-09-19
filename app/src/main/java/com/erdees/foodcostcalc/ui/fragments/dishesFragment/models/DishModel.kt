package com.erdees.foodcostcalc.ui.fragments.dishesFragment.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dishes")
data class DishModel(
    @PrimaryKey(autoGenerate = true) val dishId: Long,
    @ColumnInfo(name = "dish_name") val name: String,
    @ColumnInfo(name = "margin_percent") val marginPercent: Double = 100.0,
    @ColumnInfo(name = "dish_tax") val dishTax: Double = 0.0
) {
    override fun toString(): String {
        return name
    }
}
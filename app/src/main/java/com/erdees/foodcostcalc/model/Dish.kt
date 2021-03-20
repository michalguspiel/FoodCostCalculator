package com.erdees.foodcostcalc.model

import androidx.room.*


/** Data class provides an Dish */
@Entity(tableName = "dishes")
data class Dish(@PrimaryKey(autoGenerate = true) val dishId: Long,
                @ColumnInfo(name = "dish_name") val name: String,
                @ColumnInfo(name = "margin_percent") val marginPercent: Double = 100.0,
                @ColumnInfo(name ="dish_tax") val dishTax: Double = 0.0) {


    override fun toString(): String {
        return name
    }

}
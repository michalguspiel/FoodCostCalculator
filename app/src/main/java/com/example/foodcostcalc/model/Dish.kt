package com.example.foodcostcalc.model

import androidx.room.*


/** Data class provides an Dish */
@Entity(tableName = "dishes")
data class Dish(@PrimaryKey(autoGenerate = true) val dishId : Long,
                @ColumnInfo(name = "dish_name") val name: String) {


    override fun toString(): String {
        return "$name"
    }

}
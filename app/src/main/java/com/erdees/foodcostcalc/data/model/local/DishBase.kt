package com.erdees.foodcostcalc.data.model.local

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "dishes",
    indices = [Index("recipeId")],
    foreignKeys = [
    ForeignKey(
        entity = Recipe::class,
        parentColumns = ["recipeId"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    ),
])
data class DishBase(
    @PrimaryKey(autoGenerate = true) val dishId: Long,
    @ColumnInfo(name = "dish_name") val name: String,
    @ColumnInfo(name = "margin_percent") val marginPercent: Double = 100.0,
    @ColumnInfo(name = "dish_tax") val dishTax: Double = 0.0,
    @ColumnInfo(name = "recipeId") val recipeId: Long?
)
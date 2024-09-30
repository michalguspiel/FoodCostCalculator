package com.erdees.foodcostcalc.data.model.associations

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.erdees.foodcostcalc.data.model.DishBase
import com.erdees.foodcostcalc.data.model.HalfProductBase

@Keep
@Entity(
    tableName = "HalfProduct_Dish",
    indices = [Index("halfProductId"), Index("dishId")],
    foreignKeys = [
        ForeignKey(
            entity = HalfProductBase::class,
            parentColumns = ["halfProductId"],
            childColumns = ["halfProductId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DishBase::class,
            parentColumns = ["dishId"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HalfProductDish(
    @PrimaryKey(autoGenerate = true) val halfProductDishId: Long,
    val halfProductId: Long,
    val dishId: Long,
    val quantity: Double,
    val quantityUnit: String
)
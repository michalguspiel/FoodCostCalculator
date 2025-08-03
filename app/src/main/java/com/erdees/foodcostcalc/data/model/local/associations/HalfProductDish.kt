package com.erdees.foodcostcalc.data.model.local.associations

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.erdees.foodcostcalc.data.db.converters.UnitConverters
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.HalfProductBase
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

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
@TypeConverters(UnitConverters::class)
data class HalfProductDish(
    @PrimaryKey(autoGenerate = true) val halfProductDishId: Long,
    val halfProductId: Long,
    val dishId: Long,
    val quantity: Double,
    val quantityUnit: MeasurementUnit
)
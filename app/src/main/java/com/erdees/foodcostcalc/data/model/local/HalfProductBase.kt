package com.erdees.foodcostcalc.data.model.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.erdees.foodcostcalc.data.db.converters.UnitConverters
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

@Keep
@Entity(tableName = "HalfProduct")
@TypeConverters(UnitConverters::class)
data class HalfProductBase(
    @PrimaryKey(autoGenerate = true) val halfProductId: Long,
    val name: String,
    val halfProductUnit: MeasurementUnit
)

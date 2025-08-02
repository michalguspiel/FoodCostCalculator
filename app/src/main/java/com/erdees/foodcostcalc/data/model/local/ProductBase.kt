package com.erdees.foodcostcalc.data.model.local

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.erdees.foodcostcalc.data.db.converters.UnitConverters
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

@Keep
@Entity(tableName = "products")
@TypeConverters(UnitConverters::class)
data class ProductBase(
  @PrimaryKey(autoGenerate = true) val productId: Long,
  @ColumnInfo(name = "product_name") val name: String,
  val pricePerUnit: Double,
  val tax: Double,
  val waste: Double,
  val unit: MeasurementUnit
)

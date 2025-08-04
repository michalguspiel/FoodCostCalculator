package com.erdees.foodcostcalc.data.model.local

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.erdees.foodcostcalc.data.db.converters.InputMethodConverters
import com.erdees.foodcostcalc.data.db.converters.UnitConverters
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

@Keep
@Entity(tableName = "products")
@TypeConverters(UnitConverters::class, InputMethodConverters::class)
data class ProductBase(
  @PrimaryKey(autoGenerate = true) val productId: Long,
  @ColumnInfo(name = "product_name") val name: String,

  @ColumnInfo(name = "input_method") val inputMethod: InputMethod,
  @ColumnInfo(name = "package_price") val packagePrice: Double?,
  @ColumnInfo(name = "package_quantity") val packageQuantity: Double?,
  @ColumnInfo(name = "package_unit") val packageUnit: MeasurementUnit?,

  @ColumnInfo(name = "price_per_unit") val pricePerUnit: Double,
  @ColumnInfo(name = "unit") val unit: MeasurementUnit,

  val tax: Double,
  val waste: Double
)

package com.erdees.foodcostcalc.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "HalfProduct")
data class HalfProduct(
    @PrimaryKey(autoGenerate = true) val halfProductId: Long,
    val name: String,
    val halfProductUnit: String
)

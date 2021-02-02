package com.example.foodcostcalc.data

import androidx.room.*
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product

@Entity
data class ProductIncluded(
    @PrimaryKey(autoGenerate = true) val productIncludedId: Long,
    val dishOwnerId: Long,
    val productOwnerId: Long,
    val weight: Double
)









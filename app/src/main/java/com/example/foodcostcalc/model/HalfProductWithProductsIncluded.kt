package com.example.foodcostcalc.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation

data class HalfProductWithProductsIncluded(
    @Embedded val halfProduct: HalfProduct,
    @Relation(
        parentColumn = "halfProductId",
        entityColumn = "productIncludedInHalfProductId",
        associateBy = Junction(HalfProductWithProductsIncludedCrossRef::class)
    )
    val halfProductsList: List<ProductIncludedInHalfProduct>
) {}

@Entity(primaryKeys = ["dishId", "halfProductId"])
data class DishWithHalfProductCrossRef(
    val dishId: Long,
    val halfProductId: Long,
    val weight: Double
)

data class DishWithHalfProducts(
    @Embedded val dish: Dish,
    @Relation(
        parentColumn = "dishId",
        entityColumn = "halfProductId",
        associateBy = Junction(DishWithHalfProductCrossRef::class)
    )
    val halfProductWithProductsIncludedList: List<HalfProductWithProductsIncluded>
)
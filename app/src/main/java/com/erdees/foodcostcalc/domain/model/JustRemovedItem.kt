package com.erdees.foodcostcalc.domain.model

/**
 * Represents an item that has been removed from dish or half product.
 * */
data class JustRemovedItem(
    val item: ItemUsageEntry,
    val index: Int
)

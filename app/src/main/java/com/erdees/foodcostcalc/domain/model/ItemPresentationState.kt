package com.erdees.foodcostcalc.domain.model

/**
 * Presentation state for [Item]
 *
 * @param isExpanded whether the item is expanded or not
 * @param quantity quantity of the item
 */
data class ItemPresentationState(
    val isExpanded: Boolean = false,
    val quantity: Double = 1.0
)
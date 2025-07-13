package com.erdees.foodcostcalc.domain.model.dish

/**
 * Represents the result of an action performed on a dish.
 * Used with ScreenState.Success to communicate specific outcomes to the UI.
 */
enum class DishActionResultType {
    /** Dish has been created */
    CREATED,

    /** Dish has been updated */
    UPDATED,

    /** Dish has been copied */
    COPIED,

    /** Dish has been deleted */
    DELETED
}

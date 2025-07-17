package com.erdees.foodcostcalc.domain.model.dish

/**
 * Represents the result of an action performed on a dish.
 * Used with ScreenState.Success to communicate specific outcomes to the UI.
 */
enum class DishDetailsActionResultType {
    /** Dish has been updated */
    UPDATED_NAVIGATE,

    /** Dish has been updated but we want to stay in the screen*/
    UPDATED_STAY,

    /** Dish has been copied */
    COPIED,

    /** Dish has been deleted */
    DELETED
}

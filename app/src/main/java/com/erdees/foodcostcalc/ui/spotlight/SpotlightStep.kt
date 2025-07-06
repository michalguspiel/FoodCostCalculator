package com.erdees.foodcostcalc.ui.spotlight

import androidx.annotation.StringRes
import com.erdees.foodcostcalc.R

enum class SpotlightStep(
    @StringRes val info: Int,
    val shape: SpotlightShape = SpotlightShape.RoundedRectangle,
    val hasNextButton: Boolean = false,
    val canHideNavBar: Boolean = false
) {
    ExampleDishCard(
        info = R.string.spotlight_example_dish_card,
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = false,
    ),
    ExampleDishCardExpanded(
        info = R.string.spotlight_example_dish_card_expanded,
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true
    ),
    DishDetails(
        info = R.string.spotlight_dish_details,
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true,
        canHideNavBar = true
    ),
    IngredientsList(
        info = R.string.spotlight_ingredients_list,
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true,
        canHideNavBar = true
    ),
    PriceSummary(
        info = R.string.spotlight_price_summary,
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true,
        canHideNavBar = true
    ),
    DetailsButton(
        info = R.string.spotlight_details_button,
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true,
        canHideNavBar = true
    ),
    AddIngredientsButton(
        info = R.string.spotlight_add_ingredients_button,
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true,
        canHideNavBar = true
    ),
    CreateDishFAB(
        info = R.string.spotlight_create_dish_fab,
        shape = SpotlightShape.Circle,
        hasNextButton = false
    );

    fun toSpotlightTarget(
        onClickAction: (() -> Unit)? = null,
        scrollToElement: (suspend () -> Unit)? = null,
    ) = SpotlightTarget(
        order = ordinal,
        info = info,
        rect = null,
        shape = shape,
        hasNextButton = hasNextButton,
        onClickAction = onClickAction,
        scrollToElement = scrollToElement,
        canHideNavBar = canHideNavBar
    )
}
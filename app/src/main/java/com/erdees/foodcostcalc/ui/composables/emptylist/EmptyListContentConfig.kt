package com.erdees.foodcostcalc.ui.composables.emptylist

import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.navigation.FCCScreen

data class EmptyListContentConfig(
    val titleRes: Int,
    val descriptionRes: Int,
    val iconRes: Int,
    val iconContentDescriptionRes: Int,
    val buttonTextRes: Int
)

fun FCCScreen.emptyListConfig(): EmptyListContentConfig = when (this) {
    FCCScreen.Products -> EmptyListContentConfig(
        titleRes = R.string.empty_product_list_content_title,
        descriptionRes = R.string.empty_product_list_content_description,
        iconRes = R.drawable.shopping_basket_24px,
        iconContentDescriptionRes = R.string.cd_icon_products,
        buttonTextRes = R.string.add_product
    )

    FCCScreen.HalfProducts -> EmptyListContentConfig(
        titleRes = R.string.empty_half_product_list_content_title,
        descriptionRes = R.string.empty_half_product_list_content_description,
        iconRes = R.drawable.bakery_dining_24px,
        iconContentDescriptionRes = R.string.cd_icon_half_products,
        buttonTextRes = R.string.add_half_product
    )

    is FCCScreen.Dishes -> EmptyListContentConfig(
        titleRes = R.string.empty_dish_list_content_title,
        descriptionRes = R.string.empty_dish_list_content_description,
        iconRes = R.drawable.restaurant_24px,
        iconContentDescriptionRes = R.string.cd_icon_dishes,
        buttonTextRes = R.string.create_dish
    )

    FCCScreen.FeatureRequestList -> EmptyListContentConfig(
        titleRes = R.string.empty_feature_request_list_content_title,
        descriptionRes = R.string.empty_feature_request_list_content_description,
        iconRes = R.drawable.contact_support,
        iconContentDescriptionRes = R.string.feature_request,
        buttonTextRes = R.string.submit_feature_request
    )

    else -> error("EmptyListContent not supported for this screen")
}

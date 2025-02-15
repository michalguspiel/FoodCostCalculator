package com.erdees.foodcostcalc.utils

object Constants {

    const val SUBSCRIPTION_STATE = "subscription_state"

    object Ads {
        const val PRODUCTS_AD_FREQUENCY = 4
        const val HALF_PRODUCTS_AD_FREQUENCY = 3
        const val DISHES_AD_FREQUENCY = 3
        const val PREMIUM_FREQUENCY = 0

        const val ADMOB_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
        const val ADMOB_PRODUCTS_AD_UNIT_ID = "ca-app-pub-5093191239349402/7646868536"
        const val ADMOB_DISHES_AD_UNIT_ID = "ca-app-pub-5093191239349402/9127239654"
        const val ADMOB_HALF_PRODUCTS_AD_UNIT_ID = "ca-app-pub-5093191239349402/9594143514"
    }

    object Analytics {
        const val DISH_CREATED = "dish_created"
        const val HALF_PRODUCT_CREATED = "half_product_created"
        const val PRODUCT_CREATED = "product_created"
        const val PRODUCT_NAME = "product_name"
        const val PRODUCT_UNIT = "product_unit"
        const val PRODUCT_WASTE = "product_waste"
        const val PRODUCT_TAX = "product_tax"
        const val PRODUCT_PRICE_PER_UNIT = "product_price_per_unit"
        const val SCREEN_NAME = "screen_name"
        const val NAV_EVENT = "navigation_event"

        const val AD_FAILED_TO_LOAD =  "ad_failed_to_load"

        const val LOAD_DATABASE = "load_database"
        const val SAVE_DATABASE = "save_database"
        const val DATABASE_OPERATION_FAILURE = "database_operation_failure"
        const val DATABASE_OPERATION_SUCCESS = "database_operation_success"
        const val DATABASE_OPERATION_ERROR = "database_operation_error"

        object Buttons{
            const val DISHES_EDIT_DISPLAYED_PORTIONS = "dishes_edit_displayed_portions"
            const val RECIPE_EDIT_DISPLAYED_PORTIONS = "recipe_edit_displayed_portions"
            const val HALF_PRODUCTS_EDIT_QUANTITY = "half_products_edit_quantity"
        }
    }

    const val METRIC = "metric"
    const val IMPERIAL = "usa"

    const val MARGIN = "margin"
    const val TAX = "tax"
    const val PREFERRED_CURRENCY_CODE = "preferred_currency"

    const val BASIC_MARGIN = 100
    const val BASIC_TAX = 23
}
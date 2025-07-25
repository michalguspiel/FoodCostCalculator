package com.erdees.foodcostcalc.utils

object Constants {


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

        object UserProperties {
            const val DISH_COUNT = "dish_count"
        }

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

        const val DISH_SHARE = "dish_share_click"
        const val DISH_NAME = "dish_name"

        const val AD_FAILED_TO_LOAD = "ad_failed_to_load"

        const val LOAD_DATABASE = "load_database"
        const val SAVE_DATABASE = "save_database"
        const val DATABASE_OPERATION_FAILURE = "database_operation_failure"
        const val DATABASE_OPERATION_SUCCESS = "database_operation_success"
        const val DATABASE_OPERATION_ERROR = "database_operation_error"

        const val REVIEW_FAILURE = "review_failure"
        const val REVIEW_SUCCESS = "review_success"

        object Onboarding {
            const val STARTED = "onboarding_started"
            const val SKIPPED = "onboarding_skipped"
            const val STEP_SHOWN = "onboarding_step_shown"
            const val STEP_INDEX = "onboarding_step_index"
            const val STEP_NAME = "onboarding_step_name"
            const val UNKNOWN_STEP = "unknown_step"
            const val COMPLETED = "onboarding_completed"
            const val OUTSIDE_SPOTLIGHT_AREA_CLICKED = "onboarding_outside_spotlight_area_clicked"
        }

        object Buttons {
            const val DISHES_EDIT_DISPLAYED_PORTIONS = "dishes_edit_displayed_portions"
            const val RECIPE_EDIT_DISPLAYED_PORTIONS = "recipe_edit_displayed_portions"
            const val HALF_PRODUCTS_EDIT_QUANTITY = "half_products_edit_quantity"
        }

        object Exceptions {
            const val EVENT = "caught_logged_exception"
            const val MESSAGE = "exception_message"
        }

        object Settings {
            const val MARGIN_CHANGED = "settings_margin_changed"
            const val TAX_CHANGED = "settings_tax_changed"
            const val CURRENCY_CHANGED = "settings_currency_changed"
            const val METRIC_UNITS_USED_CHANGED = "settings_metric_units_used_changed"
            const val IMPERIAL_UNITS_USED_CHANGED = "settings_imperial_units_used_changed"
            const val SHOW_HALF_PRODUCTS_CHANGED = "settings_show_half_products_changed"
            const val SHOW_PRODUCT_TAX_CHANGED = "settings_show_product_tax_changed"
            const val SAVED = "settings_saved"

            // Parameter names
            const val MARGIN_VALUE = "margin_value"
            const val TAX_VALUE = "tax_value"
            const val CURRENCY_CODE = "currency_code"
            const val IS_ENABLED = "is_enabled"
        }

        object DishV2 {
            const val COPY = "dish_copy"
            const val DELETE = "dish_delete_clicked"
            const val DELETED = "dish_deleted"
            const val PRODUCT_CREATED = "product_created_dishv2"
            const val DISH_CREATION_STARTED =
                "dish_creation_started" // When the screen is first entered
            const val DISH_SAVE_ATTEMPT = "dish_save_attempt"
            const val DISH_SAVE_SUCCESS =
                "dish_save_success"       // Already have DISH_CREATED, this is more specific to save action
            const val DISH_SAVE_FAILURE = "dish_save_failure"
            const val DISH_INGREDIENT_ADDED = "dish_ingredient_added"
            const val DISH_INGREDIENT_TYPE = "dish_ingredient_type" // "new" or "existing"
            const val DISH_INGREDIENT_NAME = "dish_ingredient_name"
            const val DISH_INGREDIENT_QUANTITY = "dish_ingredient_quantity"
            const val DISH_INGREDIENT_UNIT = "dish_ingredient_unit"
            const val NEW_PRODUCT_SAVE_ATTEMPT_FROM_DISH =
                "new_product_save_attempt_from_dish" // When trying to save a new product *during* dish creation
            const val NEW_PRODUCT_SAVE_SUCCESS_FROM_DISH = "new_product_save_success_from_dish"
            const val NEW_PRODUCT_SAVE_FAILURE_FROM_DISH = "new_product_save_failure_from_dish"
            const val ERROR_DISPLAYED_USER =
                "error_displayed_user"
            const val ERROR_TYPE =
                "error_type"
            const val ERROR_MESSAGE_RES_ID =
                "error_message_res_id"
            const val SUGGESTION_SELECTED = "suggestion_selected"
            const val SUGGESTIONS_MANUALLY_DISMISSED = "suggestions_manually_dismissed"

            const val NUMBER_OF_INGREDIENTS = "number_of_ingredients"
            const val DISH_MARGIN = "dish_margin"
            const val DISH_TAX = "dish_tax"

            const val ADD_INGREDIENT_CLICKED = "add_ingredient_clicked"
            const val ADD_INGREDIENT_TYPE_INTENT = "add_ingredient_type_intent"

            const val DEFAULT_SETTINGS_PROMPT_SHOWN = "default_settings_prompt_shown"
            const val DEFAULT_SETTINGS_SAVED = "default_settings_saved"
            const val DEFAULT_SETTINGS_DISMISSED = "default_settings_dismissed"
        }

        object Products {
            const val DELETE = "product_delete"
            const val DELETED = "product_deleted"
        }

        object HalfProducts {
            const val DELETE = "half_product_delete"
            const val DELETED = "half_product_deleted"
        }

    }

    object UI {
        const val SEARCH_DEBOUNCE_MS = 500L
        const val COMPACT_HEIGHT_THRESHOLD_DP = 500
    }

    object Preferences {
        const val METRIC = "metric"
        const val IMPERIAL = "usa"

        const val MARGIN = "margin"
        const val TAX = "tax"
        const val PREFERRED_CURRENCY_CODE = "preferred_currency"
        const val SUBSCRIPTION_STATE = "subscription_state"
        const val SHOW_HALF_PRODUCTS = "show_half_products"
        const val SHOW_PRODUCT_TAX_PERCENT = "show_product_tax_percent"
        const val ONBOARDING_STATE = "onboarding_state"
        const val HAS_PROMPTED_DEFAULT_SETTINGS = "has_prompted_default_settings"
    }


    const val BASIC_MARGIN = 100
    const val BASIC_TAX = 23
}
package com.erdees.foodcostcalc.ui.screens.dishes

import android.os.Bundle
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import com.erdees.foodcostcalc.utils.Constants

class DishAnalyticsHelper(val analyticsRepository: AnalyticsRepository) {

    fun logFlowStarted(){
        analyticsRepository.logEvent(Constants.Analytics.DishV2.DISH_CREATION_STARTED, null)
    }

    fun logNewProductSaveAttempt(productName: String) {
        analyticsRepository.logEvent(
            Constants.Analytics.DishV2.NEW_PRODUCT_SAVE_ATTEMPT_FROM_DISH,
            Bundle().apply {
                putString(Constants.Analytics.PRODUCT_NAME, productName)
            })
    }

    fun logNewProductSaveSuccess(newlyCreatedProduct: ProductDomain) {
        analyticsRepository.logEvent(
            Constants.Analytics.DishV2.NEW_PRODUCT_SAVE_SUCCESS_FROM_DISH,
            Bundle().apply {
                putString(Constants.Analytics.PRODUCT_NAME, newlyCreatedProduct.name)
            })

        analyticsRepository.logEvent(
            Constants.Analytics.DishV2.PRODUCT_CREATED,
            Bundle().apply {
                putString(Constants.Analytics.PRODUCT_NAME, newlyCreatedProduct.name)
                putString(Constants.Analytics.PRODUCT_UNIT, newlyCreatedProduct.unit)
                putDouble(Constants.Analytics.PRODUCT_WASTE, newlyCreatedProduct.waste)
                putDouble(
                    Constants.Analytics.PRODUCT_PRICE_PER_UNIT,
                    newlyCreatedProduct.pricePerUnit
                )
            })
    }

    fun logNewProductSaveFailure() {
        analyticsRepository.logEvent(Constants.Analytics.DishV2.NEW_PRODUCT_SAVE_FAILURE_FROM_DISH)
    }

    fun logProductAddedToDishList(productName: String, selection: ComponentSelection?, quantityAddedToDish: Double, unit: String){
        analyticsRepository.logEvent(
            Constants.Analytics.DishV2.DISH_INGREDIENT_ADDED,
            Bundle().apply {
                putString(Constants.Analytics.DishV2.DISH_INGREDIENT_NAME, productName)
                putString(
                    Constants.Analytics.DishV2.DISH_INGREDIENT_TYPE,
                    if (selection is ComponentSelection.NewComponent) "new_in_context" else "existing"
                )
                putDouble(Constants.Analytics.DishV2.DISH_INGREDIENT_QUANTITY, quantityAddedToDish)
                putString(Constants.Analytics.DishV2.DISH_INGREDIENT_UNIT, unit)
            })
    }

    fun logHalfProductAddedToDishList() {
        analyticsRepository.logEvent(Constants.Analytics.DishV2.HALF_PRODUCT_ADDED_TO_DISH)
    }

    fun logHandleError(throwable: Throwable, errorResId: Int) {
        analyticsRepository.logEvent(
            Constants.Analytics.DishV2.ERROR_DISPLAYED_USER,
            Bundle().apply {
                putString(Constants.Analytics.DishV2.ERROR_TYPE, throwable::class.java.simpleName)
                putInt(Constants.Analytics.DishV2.ERROR_MESSAGE_RES_ID, errorResId)
            })
    }

    fun logDishSaveFailureAnalytics(dishName: String) {
        analyticsRepository.logEvent(
            Constants.Analytics.DishV2.DISH_SAVE_FAILURE,
            Bundle().apply {
                putString(Constants.Analytics.DISH_NAME, dishName)
            })
    }

    fun logDishSaveSuccess(dishName: String, addedProductsSize: Int, dishCount: Int) {
        analyticsRepository.logEvent(
            Constants.Analytics.DishV2.DISH_SAVE_SUCCESS,
            Bundle().apply {
                putString(Constants.Analytics.DISH_NAME, dishName)
                putInt(
                    Constants.Analytics.DishV2.NUMBER_OF_INGREDIENTS,
                    addedProductsSize
                )
            })
        analyticsRepository.logEvent(Constants.Analytics.DISH_CREATED, Bundle().apply {
            putString(Constants.Analytics.DISH_NAME, dishName)
        })
        analyticsRepository.setUserProperty(Constants.Analytics.UserProperties.DISH_COUNT, dishCount.toString())
    }

    fun logDishSaveAttempt(
        dishName: String,
        marginPercentInput: String,
        taxPercentInput: String,
        addedProductsSize: Int
    ) {
        analyticsRepository.logEvent(Constants.Analytics.DishV2.DISH_SAVE_ATTEMPT, Bundle().apply {
            putString(Constants.Analytics.DISH_NAME, dishName)
            putInt(Constants.Analytics.DishV2.NUMBER_OF_INGREDIENTS, addedProductsSize)
            putString(Constants.Analytics.DishV2.DISH_MARGIN, marginPercentInput)
            putString(Constants.Analytics.DishV2.DISH_TAX, taxPercentInput)
        })
    }

    fun logSuggestionSelected(productName: String){
        analyticsRepository.logEvent(
            Constants.Analytics.DishV2.SUGGESTION_SELECTED,
            Bundle().apply {
                putString(Constants.Analytics.PRODUCT_NAME, productName)
            })
    }

    fun logAddIngredientClick(component: ComponentSelection?){
        val bundle = Bundle()
        when(component) {
            is ComponentSelection.ExistingComponent -> {
                bundle.putString(Constants.Analytics.DishV2.ADD_INGREDIENT_TYPE_INTENT, "existing_component")
            }
            is ComponentSelection.NewComponent -> {
                bundle.putString(Constants.Analytics.DishV2.ADD_INGREDIENT_TYPE_INTENT, "new_component")
            }

            null -> {
                bundle.putString(Constants.Analytics.DishV2.ADD_INGREDIENT_TYPE_INTENT, "unknown")
            }
        }
        analyticsRepository.logEvent(Constants.Analytics.DishV2.ADD_INGREDIENT_CLICKED, bundle)
    }

    fun logExistingComponentAdded(){
        analyticsRepository.logEvent(Constants.Analytics.DishV2.EXISTING_COMPONENT_ADDED)
    }
}

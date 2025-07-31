package com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup

/**
 * Extension function to create ComponentLookupFormActions from the ComponentLookupViewModel
 * and a callback for the onNext action.
 */
fun ComponentLookupViewModel.createActions(
    onNext: () -> Unit
): ComponentLookupFormActions {
    return ComponentLookupFormActions(
        onNewComponentNameChange = ::updateNewComponentName,
        onSelectComponent = ::onComponentSelected,
        onNext = onNext,
        onReset = ::reset
    )
}
package com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup

import com.erdees.foodcostcalc.domain.model.Item

data class ComponentLookupFormActions(
    val onNewComponentNameChange: (String) -> Unit = {},
    val onSelectComponent: (Item) -> Unit = {},
    val onNext: () -> Unit = {},
    val onReset: () -> Unit = {}
)

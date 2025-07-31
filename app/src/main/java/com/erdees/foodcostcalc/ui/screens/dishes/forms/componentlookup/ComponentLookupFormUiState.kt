package com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup

import com.erdees.foodcostcalc.domain.model.Item

data class ComponentLookupFormUiState(
    val suggestedComponents: ComponentLookupResult = ComponentLookupResult(),
    val showSuggestedComponents: Boolean = false,
    val newComponentName: String = "",
    val selectedComponent: Item? = null
)

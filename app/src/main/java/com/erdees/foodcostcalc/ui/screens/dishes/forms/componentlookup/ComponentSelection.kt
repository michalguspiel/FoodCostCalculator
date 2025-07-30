package com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup

import com.erdees.foodcostcalc.domain.model.Item

/**
 * Represents the selection made by the user in the component lookup form.
 * It can be either an existing component (an item from the suggestions) or a new component.
 *
 * */
sealed class ComponentSelection {
    data class ExistingComponent(val item: Item) : ComponentSelection()
    data class NewComponent(val name: String) : ComponentSelection()
}

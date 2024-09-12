package com.erdees.foodcostcalc.ui.tools

import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.ItemPresentationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ListPresentationStateHandler(private val onResetScreenState: () -> Unit) {
    private val _itemsPresentationState: MutableStateFlow<Map<Long, ItemPresentationState>> =
        MutableStateFlow(emptyMap())
    val itemsPresentationState: StateFlow<Map<Long, ItemPresentationState>> =
        _itemsPresentationState

    fun updatePresentationState(items: Map<Long, ItemPresentationState>) {
        _itemsPresentationState.value = items
    }

    fun onExpandToggle(item: Item) {
        var itemPresentationState = itemsPresentationState.value[item.id] ?: ItemPresentationState()
        itemPresentationState =
            itemPresentationState.copy(isExpanded = !itemPresentationState.isExpanded)
        val mutableMap = _itemsPresentationState.value.toMutableMap()
        mutableMap[item.id] = itemPresentationState
        _itemsPresentationState.value = mutableMap
    }

    fun updatePresentationQuantity(itemId: Long, newQuantity: String) {
        val quantity = newQuantity.toDoubleOrNull() ?: return
        var itemPresentationState = itemsPresentationState.value[itemId] ?: ItemPresentationState()
        itemPresentationState = itemPresentationState.copy(quantity = quantity)
        val mutableMap = _itemsPresentationState.value.toMutableMap()
        mutableMap[itemId] = itemPresentationState
        _itemsPresentationState.value = mutableMap

        // Close dialog after quantity is updated
        onResetScreenState()
    }
}
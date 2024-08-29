package com.erdees.foodcostcalc.domain.model



sealed class InteractionType {
    data object EditTax : InteractionType()
    data object EditMargin : InteractionType()
    data object EditTotalPrice : InteractionType()
    data object EditName : InteractionType()
    data class EditItem(val usedItem: UsedItem) : InteractionType()
}

sealed class ScreenState {
    data object Loading : ScreenState()
    data object Idle : ScreenState()
    data class Error(val error: kotlin.Error) : ScreenState()
    data object Success : ScreenState()
    data class Interaction(val interaction: InteractionType) : ScreenState()
}

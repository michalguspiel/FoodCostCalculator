package com.erdees.foodcostcalc.domain.model


sealed class InteractionType {
    data object EditTax : InteractionType()
    data object EditMargin : InteractionType()
    data object EditTotalPrice : InteractionType()
    data object EditName : InteractionType()
    data class EditItem(val usedItem: UsedItem) : InteractionType()
    data class EditQuantity(val itemId: Long) : InteractionType()
    data object ChangeServings : InteractionType()
    data object UnsavedChangesConfirmation : InteractionType()
    data class CopyDish(val prefilledName: String) : InteractionType()

    data object CalculateWaste : InteractionType()
    data object CalculatePiecePrice : InteractionType()
    data object CreateHalfProduct : InteractionType()
    data class DeleteConfirmation(val itemId: Long, val itemName: String) : InteractionType()
}

sealed class ScreenState {
    data class Loading<T>(val data: T? = null) : ScreenState()
    data object Idle : ScreenState()
    data class Error(val error: kotlin.Error) : ScreenState()
    data class Success<T>(val data: T? = null) : ScreenState()
    data class Interaction(val interaction: InteractionType) : ScreenState()
}
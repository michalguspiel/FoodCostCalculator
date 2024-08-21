package com.erdees.foodcostcalc.domain.model

interface InteractionType

sealed class ScreenState {
    data object Loading : ScreenState()
    data object Idle : ScreenState()
    data class Error(val error: kotlin.Error) : ScreenState()
    data object Success : ScreenState()
    data class Interaction(val interaction: InteractionType): ScreenState()
}

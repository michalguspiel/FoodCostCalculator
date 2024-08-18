package com.erdees.foodcostcalc.domain.model

sealed class ScreenState {
    data object Loading : ScreenState()
    data object Idle : ScreenState()
    data class Error(val error: kotlin.Error) : ScreenState()
    data object Success : ScreenState()
}

package com.erdees.foodcostcalc.ui.screens.paywall

sealed class RestoreState {
    object Idle : RestoreState()
    object Success : RestoreState()
    object NoPurchases : RestoreState()
    data class Error(val message: String) : RestoreState()
}
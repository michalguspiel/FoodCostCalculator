package com.erdees.foodcostcalc.ui.screens.featureRequest

data class FeatureRequestScreenCallbacks(
    val onBackClick: () -> Unit,
    val onSubmitClick: () -> Unit,
    val onDismiss: () -> Unit,
    val updateTitle: (String) -> Unit,
    val updateDescription: (String) -> Unit
)
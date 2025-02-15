package com.erdees.foodcostcalc.ui.screens.recipe

/**
 * Wrapper to ease passing down functions to composable.
 * */
data class RecipeUpdater(
    val updatePrepTime: (String) -> Unit,
    val updateCookTime: (String) -> Unit,
    val updateDescription: (String) -> Unit,
    val updateTips: (String) -> Unit,
    val updateStep: (Int, String) -> Unit
)
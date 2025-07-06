package com.erdees.foodcostcalc.ext

import androidx.compose.ui.Modifier

/**
 * Extension function to conditionally apply a modifier based on a boolean condition.
 *
 * @param condition The condition to check.
 * @param modifier The modifier to apply if the condition is true.
 * @return The original modifier if the condition is false, or the modified one if true.
 */
fun Modifier.conditionally(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        this.modifier()
    } else {
        this
    }
}
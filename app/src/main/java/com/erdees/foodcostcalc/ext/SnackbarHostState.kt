package com.erdees.foodcostcalc.ext

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

suspend fun SnackbarHostState.showUndoDeleteSnackbar(
    message: String,
    actionLabel: String,
    actionPerformed: () -> Unit,
    ignored: () -> Unit = { }
) {
    val result = showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = SnackbarDuration.Short,
        withDismissAction = true,
    )
    if (result == SnackbarResult.ActionPerformed) {
        actionPerformed()
    } else {
        ignored()
    }
}
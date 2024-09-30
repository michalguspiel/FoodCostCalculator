package com.erdees.foodcostcalc.ui.composables.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ErrorDialog(
  modifier: Modifier = Modifier,
  title: String = "Error",
  content: String = "Something went wrong",
  confirmButtonText: String = "OK",
  onDismiss: () -> Unit
) {
  AlertDialog(
    modifier = modifier,
    onDismissRequest = { onDismiss() },
    title = { Text(title) },
    text = { Text(content) },
    confirmButton = {
      Button(onClick = { onDismiss() }) {
        Text(confirmButtonText)
      }
    }
  )
}

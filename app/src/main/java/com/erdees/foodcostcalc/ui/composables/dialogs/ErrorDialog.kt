package com.erdees.foodcostcalc.ui.composables.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.erdees.foodcostcalc.R

@Composable
fun ErrorDialog(
  modifier: Modifier = Modifier,
  title: String = stringResource(id = R.string.error),
  content: String = stringResource(id = R.string.something_went_wrong),
  confirmButtonText: String = stringResource(id = R.string.okay),
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

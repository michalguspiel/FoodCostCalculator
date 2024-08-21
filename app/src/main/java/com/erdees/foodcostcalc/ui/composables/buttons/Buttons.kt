package com.erdees.foodcostcalc.ui.composables.buttons

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FCCPrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
  Button(modifier = modifier,
    onClick = { onClick() }) {
    Text(text = text,style = MaterialTheme.typography.labelLarge)
  }
}

@Composable
fun FCCTextButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
  TextButton(modifier = modifier,
    onClick = { onClick() }) {
    Text(text = text)
  }
}

@Composable
fun FCCOutlinedButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
  OutlinedButton(modifier = modifier,
    onClick = { onClick() }) {
    Text(text = text)
  }
}

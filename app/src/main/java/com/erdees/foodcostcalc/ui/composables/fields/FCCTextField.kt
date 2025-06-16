package com.erdees.foodcostcalc.ui.composables.fields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel

@Composable
fun FCCTextField(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    suffix: @Composable (() -> Unit)? = null,
    maxLines: Int = 1,
    onValueChange: (String) -> Unit,
) {
    val outlinedTextFieldModifier = if (singleLine && maxLines == 1) {
        Modifier.fillMaxWidth()
    } else {
        Modifier.fillMaxSize()
    }
    Column(modifier) {
        FieldLabel(
            text = title,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            modifier = outlinedTextFieldModifier,
            value = value,
            singleLine = singleLine,
            maxLines = maxLines,
            suffix = suffix,
            onValueChange = { onValueChange(it) },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}
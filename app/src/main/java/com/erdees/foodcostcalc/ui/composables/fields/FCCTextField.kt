package com.erdees.foodcostcalc.ui.composables.fields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
    singleLine: Boolean = true,
    maxLines: Int = 1,
    onValueChange: (String) -> Unit,
) {
    Column(modifier) {
        FieldLabel(
            text = title,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxSize(),
            value = value,
            singleLine = singleLine,
            maxLines = maxLines,
            onValueChange = { onValueChange(it) },
            keyboardOptions = keyboardOptions
        )
    }
}
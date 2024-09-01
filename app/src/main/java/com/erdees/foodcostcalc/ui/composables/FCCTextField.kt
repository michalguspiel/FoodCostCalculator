package com.erdees.foodcostcalc.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
    onValueChange: (String) -> Unit,
) {
    Column {
        FieldLabel(
            text = title,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            modifier = modifier.fillMaxWidth(),
            value = value,
            singleLine = true,
            maxLines = 1,
            onValueChange = { onValueChange(it) },
            keyboardOptions = keyboardOptions
        )
    }
}
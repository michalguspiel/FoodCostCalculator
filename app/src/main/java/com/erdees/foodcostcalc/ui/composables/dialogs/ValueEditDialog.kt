package com.erdees.foodcostcalc.ui.composables.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValueEditDialog(
    title: String,
    value: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier,
    updateValue: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = { onDismiss() },
    ) {
        Column(
            Modifier
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.size(16.dp))
            OutlinedTextField(
                value = value,
                onValueChange = { value ->
                    updateValue(value)
                },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
            )
            Spacer(modifier = Modifier.size(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FCCTextButton(text = "Save") {
                    onSave()
                }
            }
        }
    }
}

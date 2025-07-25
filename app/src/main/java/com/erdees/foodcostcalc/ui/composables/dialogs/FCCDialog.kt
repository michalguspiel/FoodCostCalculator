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
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FCCDialog(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    primaryActionButton: @Composable () -> Unit,
    secondaryActionButton: @Composable (() -> Unit)? = null
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
            Text(text = title, style = MaterialTheme.typography.titleLarge)

            subtitle?.let {
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.size(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                secondaryActionButton?.invoke()
                Spacer(modifier = Modifier.size(8.dp))
                primaryActionButton()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FCCDialog(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    buttonRow: @Composable () -> Unit
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
            Text(text = title, style = MaterialTheme.typography.titleLarge)

            subtitle?.let {
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.size(24.dp))

            buttonRow()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FCCDialog(
    title: String,
    modifier: Modifier = Modifier,
    primaryButtonText: String = stringResource(id = R.string.save),
    onDismiss: () -> Unit,
    onPrimaryButtonClick: () -> Unit,
    content: @Composable () -> Unit
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
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.size(16.dp))

            content()

            Spacer(modifier = Modifier.size(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FCCTextButton(text = primaryButtonText) {
                    onPrimaryButtonClick()
                }
            }
        }
    }
}

package com.erdees.foodcostcalc.ui.composables.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun FCCDeleteConfirmationDialog(
    itemName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onConfirmDelete: () -> Unit,
) {
    FCCDialog(
        modifier = modifier,
        title = stringResource(id = R.string.delete_item_title, itemName),
        subtitle = stringResource(id = R.string.delete_confirmation_message),
        primaryActionButton = {
            FCCTextButton(text = stringResource(R.string.delete)) {
                onConfirmDelete()
            }
        },
        secondaryActionButton = {
            FCCTextButton(stringResource(R.string.cancel)) {
                onDismiss()
            }
        },
        onDismiss = onDismiss
    )
}

@Preview
@PreviewLightDark
@Composable
private fun FCCDeleteConfirmationDialogPreview() {
    FCCTheme {
        Scaffold { padding ->
            FCCDeleteConfirmationDialog(
                modifier = Modifier.padding(padding),
                itemName = "Test Item",
                onDismiss = {},
                onConfirmDelete = {})
        }
    }
}

package com.erdees.foodcostcalc.ui.composables.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.theme.FCCTheme

/**
 * Dialog that shows when user attempts to navigate away with unsaved changes.
 *
 * @param onDismiss Called when the dialog is dismissed (e.g. by clicking outside or on Cancel)
 * @param onDiscard Called when user decides to discard changes
 * @param onSave Called when user decides to save changes
 * @param modifier Optional modifier for the dialog
 */
@Composable
fun FCCUnsavedChangesDialog(
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    FCCDialog(
        modifier = modifier,
        title = stringResource(id = R.string.save_changes_title),
        subtitle = stringResource(id = R.string.unsaved_changes_message),
        onDismiss = onDismiss,
        buttonRow = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FCCTextButton(text = stringResource(R.string.discard)) {
                    onDiscard()
                }

                Spacer(modifier = Modifier.size(8.dp))

                FCCTextButton(text = stringResource(R.string.cancel)) {
                    onDismiss()
                }

                Spacer(modifier = Modifier.size(8.dp))

                FCCTextButton(text = stringResource(R.string.save)) {
                    onSave()
                }
            }

        }
    )
}

@Preview
@PreviewLightDark
@Composable
private fun FCCUnsavedChangesDialogPreview() {
    FCCTheme {
        Scaffold { padding ->
            FCCUnsavedChangesDialog(
                modifier = Modifier.padding(padding),
                onDismiss = {},
                onDiscard = {},
                onSave = {}
            )
        }
    }
}

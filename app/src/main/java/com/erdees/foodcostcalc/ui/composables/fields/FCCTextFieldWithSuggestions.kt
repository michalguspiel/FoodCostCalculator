package com.erdees.foodcostcalc.ui.composables.fields

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

/**
 * A Composable function that provides a text field with suggestions displayed in a dropdown.
 *
 * This component combines a standard `FCCTextField` with a list of suggestions that appear
 * below the text field when `shouldShowSuggestions` is true and `suggestions` is not empty.
 * Users can type in the text field and select a suggestion from the list.
 *
 * @param T The type of the items in the suggestions list.
 * @param title The label displayed above the text field.
 * @param value The current text value of the text field.
 * @param onValueChange Callback invoked when the text value of the text field changes.
 *                      The new text value is provided as a parameter.
 * @param suggestions A list of items of type `T` to be displayed as suggestions.
 * @param onSuggestionSelected Callback invoked when a suggestion is selected from the dropdown.
 *                             The selected suggestion of type `T` is provided as a parameter.
 * @param modifier Optional [Modifier] to be applied to the root Box composable.
 * @param keyboardOptions Optional [KeyboardOptions] to configure the software keyboard.
 *                        Defaults to [KeyboardOptions.Default].
 * @param keyboardActions Optional [KeyboardActions] to define actions for the software keyboard.
 *                        Defaults to [KeyboardActions.Default].
 * @param suggestionItemContent A Composable lambda that defines how each suggestion item is rendered
 *                              in the dropdown. It receives a suggestion of type `T` as a parameter.
 * @param shouldShowSuggestions A boolean indicating whether the suggestions dropdown should be visible.
 * @param onDismissSuggestions Callback invoked when the suggestions dropdown is dismissed (e.g., by
 *                             clicking outside or when the text field loses focus and suggestions were showing).
 * @param focusRequester Optional [FocusRequester] to control the focus of the text field.
 *                       Defaults to a remembered `FocusRequester`.
 */
@Composable
fun <T> FCCTextFieldWithSuggestions(
    title: String?,
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<T>,
    shouldShowSuggestions: Boolean,
    onSuggestionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    placeholder: String? = null,
    suggestionItemContent: @Composable (T) -> Unit,

    onDismissSuggestions: () -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    var textFieldWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current

    Box(modifier = modifier) {
        FCCTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .onSizeChanged {
                    textFieldWidth = with(density) { it.width.toDp() }
                }
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && shouldShowSuggestions) {
                        onDismissSuggestions()
                    }
                },
            title = title,
            value = value,
            placeholder = placeholder,
            onValueChange = onValueChange,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            maxLines = 1
        )

        if (shouldShowSuggestions && suggestions.isNotEmpty()) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = {
                    onDismissSuggestions()
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.width(textFieldWidth),
                properties = PopupProperties(
                    focusable = false,
                )
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = {
                            suggestionItemContent(suggestion)
                        },
                        onClick = {
                            onSuggestionSelected(suggestion)
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }
    }
}
package com.erdees.foodcostcalc.ui.composables.fields

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchField(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    Row(
        modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(8.dp)
    ) {
        AnimatedVisibility(visible = isFocused) {
            IconButton(modifier = Modifier
                .size(36.dp)
                .alpha(0.6f), onClick = {
                focusManager.clearFocus()
                onValueChange("")
            }) {
                Icon(Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = stringResource(id = R.string.back))
            }
        }

        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            singleLine = true,
            interactionSource = interactionSource,
        ) { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_by_name),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        Icon(
                            modifier = Modifier.clickable { onValueChange("") },
                            imageVector = Icons.Default.Clear, contentDescription = stringResource(
                                id = R.string.cancel_search
                            )
                        )
                    }
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.alpha(0.6f),
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(id = R.string.search_bar)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(),
                contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                    top = 0.dp,
                    bottom = 0.dp,
                ),
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        shape = RoundedCornerShape(16.dp),
                        focusedBorderThickness = 12.dp,
                        unfocusedBorderThickness = 12.dp,
                    )
                },
            )
        }
    }
}


@Preview
@Composable
fun PreviewSearchFieldLight() {
    FCCTheme(darkTheme = false) {
        SearchField(value = "") {}
    }
}

@Preview
@Composable
fun PreviewSearchFieldLightFilled() {
    FCCTheme(darkTheme = false) {
        SearchField(value = "chicken") {}
    }
}

@Preview
@Composable
fun PreviewSearchFieldDark() {
    FCCTheme(darkTheme = true) {
        SearchField(value = "") {}
    }
}
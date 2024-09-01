package com.erdees.foodcostcalc.ui.composables.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun FCCPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(modifier = modifier,
        enabled = enabled,
        onClick = { onClick() }) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun FCCTextButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        enabled = enabled,
        onClick = { onClick() }) {
        Text(text = text)
    }
}

@Composable
fun FCCOutlinedButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(modifier = modifier,
        enabled = enabled,
        onClick = { onClick() }) {
        Text(text = text)
    }
}

@Composable
fun FCCTopAppBarNavIconButton(navController: NavController, modifier: Modifier = Modifier) {
    IconButton(onClick = { navController.popBackStack() }) {
        Icon(Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = "Back")
    }
}

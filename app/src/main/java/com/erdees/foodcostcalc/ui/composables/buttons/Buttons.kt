package com.erdees.foodcostcalc.ui.composables.buttons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R

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
    IconButton(modifier = modifier, onClick = { navController.popBackStack() }) {
        Icon(Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = stringResource(id = R.string.back))
    }
}

@Composable
fun FCCAnimatedFAB(isVisible: Boolean, contentDescription: String, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it * 2 }) + fadeIn(
            initialAlpha = 0.3f,
            animationSpec = tween(250, easing = LinearEasing)
        ),
        exit = slideOutHorizontally(targetOffsetX = { it * 2 }) + fadeOut(
            targetAlpha = 0.3f,
            animationSpec = tween(250, easing = LinearEasing)
        ),
    ) {
        FloatingActionButton(
            onClick = {
                onClick()
            },
            shape = CircleShape,
        ) {
            Icon(Icons.Filled.Add, contentDescription)
        }
    }
}
package com.erdees.foodcostcalc.ui.screens.dishes.dishdetails

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.erdees.foodcostcalc.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDishTopBar(
    dishName: String,
    onNameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onRecipeClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = {
        Text(
            text = dishName, modifier = Modifier.Companion.clickable { onNameClick() })
    }, actions = {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.more_options)
            )
        }
        DropdownMenu(
            expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.copy_dish)) }, onClick = {
                onCopyClick()
                showMenu = false
            }, leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.content_copy_24dp),
                    contentDescription = stringResource(R.string.copy_dish)
                )
            })
            DropdownMenuItem(text = { Text(stringResource(R.string.share)) }, onClick = {
                onShareClick()
                showMenu = false
            }, leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.share_24dp),
                    contentDescription = stringResource(R.string.share)
                )
            })
            DropdownMenuItem(
                text = { Text(stringResource(R.string.recipe_button_title)) },
                onClick = {
                    onRecipeClick()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.menu_book),
                        contentDescription = stringResource(R.string.recipe_button_title)
                    )
                })
            DropdownMenuItem(text = { Text(stringResource(R.string.remove_dish)) }, onClick = {
                onDeleteClick()
                showMenu = false
            }, leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.delete_24dp),
                    contentDescription = stringResource(R.string.remove_dish)
                )
            })
        }
    }, navigationIcon = {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Sharp.ArrowBack,
                contentDescription = stringResource(R.string.back)
            )
        }
    })
}
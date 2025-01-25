package com.erdees.foodcostcalc.ui.screens.recipe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.EditDishViewModel
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import timber.log.Timber

@Composable
fun RecipeScreen(navController: NavController, viewModel: EditDishViewModel) {

    Timber.i(viewModel.toString())
    RecipeScreenContent(modifier = Modifier, popBackStack = { navController.popBackStack() })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeScreenContent(
    modifier: Modifier = Modifier,
    popBackStack: () -> Unit = {}
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.recipe),
                        modifier = Modifier
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { popBackStack() }) {
                        Icon(Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier.padding(paddingValues)) {
            Text("Bar!")
        }
    }
}

@Preview
@Composable
private fun RecipeScreenContentPreview() {
    FCCTheme {
        RecipeScreenContent()
    }
}
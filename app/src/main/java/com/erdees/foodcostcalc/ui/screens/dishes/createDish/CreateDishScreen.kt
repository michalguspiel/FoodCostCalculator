package com.erdees.foodcostcalc.ui.screens.dishes.createDish

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erdees.foodcostcalc.utils.onNumericValueChange

@Composable
fun CreateDishScreen() {

  val viewModel: CreateDishScreenViewModel = viewModel()

  val addedDish by viewModel.addedDish.collectAsState()
  val dishName by viewModel.dishName.collectAsState()
  val margin by viewModel.margin.collectAsState()
  val tax by viewModel.tax.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }


  LaunchedEffect(addedDish) {
    addedDish?.let {
      snackbarHostState.showSnackbar("${it.name} added!")
      viewModel.resetAddedDish()
    }
  }

  // TODO, THIS WILL ACTUALLY BE A FULL SCREEN DIALOG
  // TODO String resources
  // TODO, For the time being this is okay, fix design before release
  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          viewModel.addDish()
                  },
        shape = CircleShape,
      ) {
        Icon(Icons.Filled.Add, "Large floating action button")
      }
    },
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    },
  ) { paddingValues ->
    Column(Modifier.padding(paddingValues)) {
      Spacer(modifier = Modifier.size(32.dp))
      TextField(
        value = dishName,
        onValueChange = { viewModel.dishName.value = it },
        label = { Text("Dish Name") },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Text,
          imeAction = ImeAction.Next
        )
      )

      TextField(
        value = margin,
        onValueChange = {
          onNumericValueChange(it, viewModel.margin)
        },
        label = { Text("Margin") },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Next
        )
      )

      TextField(
        value = tax,
        onValueChange = {
          onNumericValueChange(it, viewModel.tax)
        },
        label = { Text("Tax") },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Done
        )
      )
    }
  }
}

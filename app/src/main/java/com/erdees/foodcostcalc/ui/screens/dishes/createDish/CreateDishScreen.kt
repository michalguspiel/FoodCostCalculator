package com.erdees.foodcostcalc.ui.screens.dishes.createDish

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel
import com.erdees.foodcostcalc.utils.onNumericValueChange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDishScreen(navController: NavController) {

  val viewModel: CreateDishScreenViewModel = viewModel()

  val addedDish by viewModel.addedDish.collectAsState()
  val dishName by viewModel.dishName.collectAsState()
  val margin by viewModel.margin.collectAsState()
  val tax by viewModel.tax.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }

  LaunchedEffect(addedDish) {
    addedDish?.let {
      snackbarHostState.showSnackbar("${it.name} added!")
      viewModel.resetAddedDish()
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(title = { Text(text = "Create dish") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = "Back")
          }
        })
    },
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    },
  ) { paddingValues ->

    Column(
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(vertical = 24.dp)
        .padding(horizontal = 12.dp)
    ) {

      Column(
        Modifier
          .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {

        Column {
          FieldLabel(text = "Name", modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
          OutlinedTextField(
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(focusRequester),
            value = dishName,
            singleLine = true,
            maxLines = 1,
            onValueChange = { viewModel.dishName.value = it },
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Next
            )
          )
        }


        Column {
          FieldLabel(text = "Margin", modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
          OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = margin,
            singleLine = true,
            maxLines = 1,
            onValueChange = {
              onNumericValueChange(it, viewModel.margin)
            },
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Next
            )
          )
        }

        Column {
          FieldLabel(text = "Tax", modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
          OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = tax,
            singleLine = true,
            maxLines = 1,
            onValueChange = {
              onNumericValueChange(it, viewModel.tax)
            },
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Done
            )
          )
        }
      }
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        FCCPrimaryButton(
          onClick = {
            viewModel.addDish()
          },
          text = "Add"
        )
      }
    }
  }
}

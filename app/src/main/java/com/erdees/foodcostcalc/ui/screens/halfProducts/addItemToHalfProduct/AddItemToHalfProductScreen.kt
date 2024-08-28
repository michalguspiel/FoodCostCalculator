package com.erdees.foodcostcalc.ui.screens.halfProducts.addItemToHalfProduct

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.composables.AddItemFields
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel
import com.erdees.foodcostcalc.ui.screens.dishes.addItemToDish.SelectedTab
import com.erdees.foodcostcalc.utils.UnitsUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemToHalfProductScreen(
  navController: NavController,
  halfProductDomain: HalfProductDomain
) {

  val viewModel: AddItemToHalfProductViewModel = viewModel()
  val products by viewModel.products.collectAsState()
  val units by viewModel.units.collectAsState()
  val selectedUnit by viewModel.selectedUnit.collectAsState()
  val quantity by viewModel.quantity.collectAsState()
  val selectedProduct by viewModel.selectedProduct.collectAsState()
  val pieceWeight by viewModel.pieceWeight.collectAsState()
  val screenState by viewModel.screenState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(halfProductDomain) {
    viewModel.initializeWith(halfProductDomain)
  }

  LaunchedEffect(screenState) {
    when (screenState) {
      is ScreenState.Success -> {
        snackbarHostState.showSnackbar("Item added.", duration = SnackbarDuration.Short)
        viewModel.resetScreenState()
      }

      else -> {}
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      TopAppBar(title = {
        Text(text = halfProductDomain.name)
      }, navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
          Icon(Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = "Back")
        }
      })
    }) { paddingValues: PaddingValues ->
    Box(Modifier.padding(paddingValues), contentAlignment = Alignment.Center) {

      Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxSize()
          .padding(vertical = 24.dp)
          .padding(horizontal = 12.dp)
      ) {
        AddItemFields(
          items = products,
          units = units,
          quantity = quantity,
          selectedTab = SelectedTab.ADD_PRODUCT,
          selectedItem = selectedProduct,
          selectedUnit = selectedUnit,
          selectItem = { viewModel.selectProduct(it as ProductDomain) },
          selectUnit = viewModel::selectUnit,
          setQuantity = viewModel::setQuantity,
          extraField = {
            if (viewModel.pieceQuantityNeeded()) {
              PieceWeightField(
                modifier = Modifier.fillMaxWidth(),
                halfProductUnit = halfProductDomain.halfProductUnit,
                pieceWeight = pieceWeight,
                setPieceWeight = viewModel::setPieceWeight
              )
            }
          }
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          FCCPrimaryButton(onClick = { viewModel.addHalfProduct(halfProductDomain) }, text = "Add")
        }
      }

      when (screenState) {
        is ScreenState.Loading -> ScreenLoadingOverlay()
        is ScreenState.Error -> {
          AlertDialog(
            onDismissRequest = { viewModel.resetScreenState() },
            title = { Text("Error") },
            text = { Text("Something went wrong") },
            confirmButton = {
              Button(onClick = { viewModel.resetScreenState() }) {
                Text("OK")
              }
            }
          )
        }

        else -> {}
      }
    }
  }
}

@Composable
fun PieceWeightField(
  halfProductUnit: String,
  pieceWeight: String,
  modifier: Modifier = Modifier,
  setPieceWeight: (String) -> Unit,
) {
  Column(modifier) {
    FieldLabel(
      text = "Piece ${UnitsUtils.getPerUnitAsDescription(halfProductUnit)}",
      modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
    OutlinedTextField(
      value = pieceWeight,
      singleLine = true,
      onValueChange = { newValue ->
        setPieceWeight(newValue)
      },
      modifier = Modifier.fillMaxWidth(),
      textStyle = LocalTextStyle.current.copy(
        textAlign = TextAlign.End
      ),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
  }
}


package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.ui.composables.DetailItem
import com.erdees.foodcostcalc.ui.composables.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.onNumericValueChange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDishScreen(dishDomain: DishDomain, navController: NavController) {

  val viewModel: EditDishViewModel = viewModel()
  val screenState by viewModel.screenState.collectAsState()
  val usedItems: List<UsedItem> by viewModel.items.collectAsState()
  val modifiedDishDomain by viewModel.dish.collectAsState()

  LaunchedEffect(dishDomain) {
    viewModel.initializeWith(dishDomain)
  }

  LaunchedEffect(screenState) {
    when (screenState) {
      is ScreenState.Success -> {
        Log.i(
          "EditDishScreen",
          "Success, popping backstack \n" +
            "Previous backstack entry: ${navController.previousBackStackEntry?.destination?.route} \n"
        )
        navController.popBackStack()
      }

      else -> {}
    }

  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = dishDomain.name) },
        actions = {
          IconButton(onClick = { viewModel.deleteDish(dishDomain.dishId) }) {
            Icon(imageVector = Icons.Sharp.Delete, contentDescription = "Remove dish")
          }
        },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { paddingValues ->

    Box(modifier = Modifier.padding(paddingValues)) {
      Column {
        LazyColumn(Modifier.weight(fill = true, weight = 1f)) {
          items(usedItems, key = { item -> item.id }) { item ->
            UsedItem(
              usedItem = item,
              onRemove = viewModel::removeItem,
              onEdit = { viewModel.setInteraction(EditDishScreenInteraction.EditItem(it)) }
            )
            HorizontalDivider(
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
              thickness = 1.dp
            )
          }
        }

        Column(Modifier) {
          DishDetails(
            modifiedDishDomain ?: dishDomain,
            onTaxClick = {
              viewModel.setInteraction(EditDishScreenInteraction.EditTax)
            }, onMarginClick = {
              viewModel.setInteraction(EditDishScreenInteraction.EditMargin)
            }, onTotalPriceClick = {
              viewModel.setInteraction(EditDishScreenInteraction.EditTotalPrice)
            })


          Spacer(Modifier.size(16.dp))

          Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 16.dp, end = 16.dp)
          ) {
            FCCPrimaryButton(text = "Save") {
              viewModel.saveDish()
            }
          }
        }
      }


      when (screenState) {
        is ScreenState.Loading -> {
          ScreenLoadingOverlay()
        }

        is ScreenState.Success -> {
          // TODO
        }

        is ScreenState.Error -> {
          ErrorDialog {
            viewModel.resetScreenState()
          }
        }

        is ScreenState.Interaction -> {
          when ((screenState as ScreenState.Interaction).interaction) {
            EditDishScreenInteraction.EditTax -> {
              EditTaxDialog(
                tax = modifiedDishDomain?.taxPercent ?: dishDomain.taxPercent,
                onSave = viewModel::updateDishTax,
                onDismiss = viewModel::resetScreenState
              )
            }

            EditDishScreenInteraction.EditMargin -> {
              BasicAlertDialog(onDismissRequest = viewModel::resetScreenState) {
                EditMarginDialog(
                  margin = modifiedDishDomain?.marginPercent ?: dishDomain.marginPercent,
                  onSave = viewModel::updateDishMargin,
                  onDismiss = viewModel::resetScreenState
                )
              }
            }

            EditDishScreenInteraction.EditTotalPrice -> {
              // TODO
            }

            is EditDishScreenInteraction.EditItem -> {
              EditQuantityDialog(
                screenState = screenState,
                onSave = viewModel::updateItemQuantity,
                onDismiss = viewModel::resetScreenState
              )
            }
          }
        }

        is ScreenState.Idle -> {
          // TODO
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaxDialog(
  tax: Double,
  modifier: Modifier = Modifier,
  onSave: (Double?) -> Unit,
  onDismiss: () -> Unit
) {

  var editableTax by remember {
    mutableStateOf(tax.toString())
  }

  BasicAlertDialog(
    modifier = modifier,
    onDismissRequest = { onDismiss() },
  ) {
    Column(
      Modifier
        .background(
          MaterialTheme.colorScheme.surface,
          shape = RoundedCornerShape(28.dp)
        )
        .padding(24.dp)
    ) {
      Text(text = "Edit tax", style = MaterialTheme.typography.displaySmall)
      Spacer(modifier = Modifier.size(16.dp))
      OutlinedTextField(
        value = editableTax,
        onValueChange = { value ->
          editableTax = editableTax.onNumericValueChange(value)
        },
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
      )
      Spacer(modifier = Modifier.size(24.dp))
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        FCCTextButton(text = "Save") {
          onSave(editableTax.toDoubleOrNull())
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMarginDialog(
  margin: Double,
  modifier: Modifier = Modifier,
  onSave: (Double?) -> Unit,
  onDismiss: () -> Unit
) {

  var editableMargin by remember {
    mutableStateOf(margin.toString())
  }

  BasicAlertDialog(
    modifier = modifier,
    onDismissRequest = { onDismiss() },
  ) {
    Column(
      Modifier
        .background(
          MaterialTheme.colorScheme.surface,
          shape = RoundedCornerShape(28.dp)
        )
        .padding(24.dp)
    ) {
      Text(text = "Edit margin", style = MaterialTheme.typography.displaySmall)
      Spacer(modifier = Modifier.size(16.dp))
      OutlinedTextField(
        value = editableMargin,
        onValueChange = { value ->
          editableMargin = editableMargin.onNumericValueChange(value)
        },
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
      )
      Spacer(modifier = Modifier.size(24.dp))
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        FCCTextButton(text = "Save") {
          onSave(editableMargin.toDoubleOrNull())
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuantityDialog(
  screenState: ScreenState,
  modifier: Modifier = Modifier,
  onSave: (String, UsedItem) -> Unit,
  onDismiss: () -> Unit
) {
  val interaction = (screenState as ScreenState.Interaction).interaction
  val item = (interaction as EditDishScreenInteraction.EditItem).usedItem

  var editableQuantity by remember {
    mutableStateOf(item.quantity.toString())
  }

  BasicAlertDialog(
    modifier = modifier,
    onDismissRequest = { onDismiss() },
  ) {
    Column(
      Modifier
        .background(
          MaterialTheme.colorScheme.surface,
          shape = RoundedCornerShape(28.dp)
        )
        .padding(24.dp)
    ) {
      Text(text = "Edit quantity", style = MaterialTheme.typography.displaySmall)
      Spacer(modifier = Modifier.size(16.dp))
      OutlinedTextField(
        value = editableQuantity,
        onValueChange = { value ->
          editableQuantity = editableQuantity.onNumericValueChange(value)
        },
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
      )
      Spacer(modifier = Modifier.size(24.dp))
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        FCCTextButton(text = "Save") {
          onSave(editableQuantity, item)
        }
      }
    }
  }
}

@Composable
fun DishDetails(
  dishDomain: DishDomain,
  modifier: Modifier = Modifier,
  onTaxClick: () -> Unit,
  onMarginClick: () -> Unit,
  onTotalPriceClick: () -> Unit
) {
  val context = LocalContext.current
  Column(modifier) {
    Row {
      DetailItem(
        label = "Margin",
        value = "${dishDomain.marginPercent}%",
        modifier = Modifier
          .weight(1f)
          .clickable {
            onMarginClick()
          }
      )
      DetailItem(
        label = "Tax",
        value = "${dishDomain.taxPercent}%",
        modifier = Modifier
          .weight(1f)
          .clickable {
            onTaxClick()
          }
      )
    }

    Spacer(modifier = Modifier.size(8.dp))

    Row {
      DetailItem(
        label = "Food cost",
        value = Utils.formatPrice(dishDomain.foodCost, context),
        modifier = Modifier.weight(1f)
      )
      DetailItem(
        label = "Total cost",
        value = Utils.formatPrice(dishDomain.totalPrice, context),
        modifier = Modifier.weight(1f)
      )
    }
  }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsedItem(
  usedItem: UsedItem,
  modifier: Modifier = Modifier,
  onRemove: (UsedItem) -> Unit,
  onEdit: (UsedItem) -> Unit
) {
  val swipeState = rememberSwipeToDismissBoxState()

  SwipeToDismissBox(
    modifier = modifier.animateContentSize(),
    state = swipeState,
    backgroundContent = {
      Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
          .fillMaxSize()
          .background(color = MaterialTheme.colorScheme.error)
      ) {
        Icon(
          modifier = Modifier.minimumInteractiveComponentSize(),
          imageVector = Icons.Sharp.Delete, contentDescription = null
        )
      }
    },
    enableDismissFromStartToEnd = false,
    enableDismissFromEndToStart = true,
    content = {
      ListItem(
        colors = (
          ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
          ),
        headlineContent = {
          Text(text = usedItem.item.name)
        }, supportingContent = {
          Text(text = usedItem.quantity.toString() + " " + usedItem.quantityUnit)
        }, trailingContent = {
          IconButton(onClick = { onEdit(usedItem) }) {
            Icon(imageVector = Icons.Sharp.Edit, contentDescription = "Edit")
          }
        }
      )
    }
  )

  when (swipeState.currentValue) {
    SwipeToDismissBoxValue.EndToStart -> {
      LaunchedEffect(swipeState) {
        swipeState.reset()
      }
      onRemove(usedItem)
    }

    SwipeToDismissBoxValue.StartToEnd -> {}

    SwipeToDismissBoxValue.Settled -> {}
  }
}


@Preview
@Composable
fun UsedItemPreview() {
  FCCTheme {
    UsedItem(
      UsedProductDomain(
        id = 0,
        ownerId = 0,
        item = ProductDomain(
          id = 1,
          name = "Product",
          pricePerUnit = 10.0,
          unit = "kg",
          tax = 23.0,
          waste = 20.0
        ),
        quantity = 1.0,
        quantityUnit = "kg",
        weightPiece = 1.0
      ),
      modifier = Modifier,
      onEdit = {},
      onRemove = {},
    )
  }
}
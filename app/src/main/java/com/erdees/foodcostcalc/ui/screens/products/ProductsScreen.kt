package com.erdees.foodcostcalc.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.Ad
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.composables.Ad
import com.erdees.foodcostcalc.ui.composables.dividers.FCCPrimaryHorizontalDivider
import com.erdees.foodcostcalc.ui.composables.rows.PriceRow
import com.erdees.foodcostcalc.ui.composables.fields.SearchField
import com.erdees.foodcostcalc.ui.composables.animations.SearchFieldTransition
import com.erdees.foodcostcalc.ui.composables.buttons.FCCAnimatedFAB
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.rememberNestedScrollConnection
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Utils.formatPrice

@Composable
fun ProductsScreen(navController: NavController) {

    val viewModel: ProductsFragmentViewModel = viewModel()
    val searchKey by viewModel.searchKey.collectAsState()
    val adItems by viewModel.filteredProductsInjectedWithAds.collectAsState()
    val isVisible = rememberSaveable { mutableStateOf(true) }
    val nestedScrollConnection = rememberNestedScrollConnection(isVisible)

    Scaffold(
        modifier = Modifier,
        floatingActionButton = {
            FCCAnimatedFAB(isVisible = isVisible.value) {
                navController.navigate(FCCScreen.CreateProduct)
            }
        }
    ) { paddingValues ->

        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(paddingValues)
        ) {
            LazyColumn(
                Modifier
                    .nestedScroll(nestedScrollConnection)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = (36 + 8 + 8).dp)
            ) {
                items(adItems) { item ->
                    when (item) {
                        is Ad -> {
                            Ad(
                                modifier = Modifier.padding(vertical = 8.dp),
                                adUnitId = if (BuildConfig.DEBUG) Constants.Ads.ADMOB_TEST_AD_UNIT_ID
                                else Constants.Ads.ADMOB_PRODUCTS_AD_UNIT_ID
                            )
                        }

                        is ProductDomain -> {
                            ProductItem(
                                productDomain = item,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                navController.navigate(FCCScreen.EditProduct(item))
                            }
                        }
                    }
                }
            }

            SearchFieldTransition(isVisible = isVisible.value) {
                SearchField(
                    modifier = Modifier,
                    value = searchKey,
                    onValueChange = viewModel::updateSearchKey
                )
            }
        }
    }
}

@Composable
fun ProductItem(
    productDomain: ProductDomain,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Card(modifier.fillMaxWidth(), content = {
        Column(Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
            Text(text = productDomain.name, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(8.dp))

            PriceRow(
                description = stringResource(id = R.string.netto_price, productDomain.unit),
                price = formatPrice(productDomain.pricePerUnit, context)
            )
            Spacer(modifier = Modifier.height(4.dp))
            PriceRow(
                description = stringResource(id = R.string.total_price, productDomain.unit),
                price = formatPrice(productDomain.priceAfterWasteAndTax, context)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FCCPrimaryHorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FCCPrimaryButton(text = stringResource(id = R.string.edit)) {
                    onEditClick()
                }
            }
        }
    })
}

@Preview
@Composable
fun ProductItemPreview() {
    FCCTheme {
        ProductItem(
            productDomain = ProductDomain(
                id = 1,
                name = "Product",
                pricePerUnit = 12.23,
                tax = 0.23,
                waste = 0.1,
                unit = "kg"
            )
        )
    }
}
package com.erdees.foodcostcalc.ui.screens.products

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.ListProductBinding
import com.erdees.foodcostcalc.domain.model.Ad
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.composables.Ad
import com.erdees.foodcostcalc.ui.composables.SearchField
import com.erdees.foodcostcalc.ui.composables.animations.SearchFieldTransition
import com.erdees.foodcostcalc.ui.composables.buttons.FCCAnimatedFAB
import com.erdees.foodcostcalc.ui.composables.rememberNestedScrollConnection
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
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
                                adUnitId = if (BuildConfig.DEBUG) Constants.Ads.ADMOB_TEST_AD_UNIT_ID
                                else Constants.Ads.ADMOB_PRODUCTS_AD_UNIT_ID
                            )
                        }

                        is ProductDomain -> {
                            ProductItem(
                                productDomain = item,
                                navController = navController,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
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
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AndroidViewBinding(
        modifier = modifier,
        factory = ListProductBinding::inflate,
        onReset = {},
        onRelease = {},
        update = {
            this.productTitle.text = productDomain.name
            this.productNettoPrice.text = context.getString(
                R.string.product_netto_price,
                productDomain.unit,
                formatPrice(productDomain.pricePerUnit, context)
            )
            this.productFoodcostPrice.text = context.getString(
                R.string.product_foodcost_price,
                productDomain.unit,
                formatPrice(productDomain.priceAfterWasteAndTax, context)
            )
            this.editButton.setOnClickListener {
                navController.navigate(FCCScreen.EditProduct(productDomain))
            }
        })
}
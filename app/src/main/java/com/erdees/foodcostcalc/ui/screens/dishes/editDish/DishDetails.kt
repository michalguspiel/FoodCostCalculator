package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.icu.util.Currency
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.ui.composables.DetailItem
import com.erdees.foodcostcalc.utils.Utils

@Composable
fun DishDetails(
    dishDomain: DishDomain,
    currency: Currency?,
    onTaxClick: () -> Unit,
    onMarginClick: () -> Unit,
    onTotalPriceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row {
            DetailItem(
                label = stringResource(R.string.margin),
                value = "${dishDomain.marginPercent}%",
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
                    .clickable {
                        onMarginClick()
                    })
            DetailItem(
                label = stringResource(R.string.tax),
                value = "${dishDomain.taxPercent}%",
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
                    .clickable {
                        onTaxClick()
                    })
        }

        Spacer(modifier = Modifier.size(8.dp))

        Row {
            DetailItem(
                label = stringResource(R.string.food_cost),
                value = Utils.formatPrice(dishDomain.foodCost, currency),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            )
            DetailItem(
                label = stringResource(R.string.final_price),
                value = Utils.formatPrice(dishDomain.totalPrice, currency),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
                    .clickable(enabled = dishDomain.foodCost != 0.0) {
                        onTotalPriceClick()
                    },
                bolder = true
            )
        }
    }
}
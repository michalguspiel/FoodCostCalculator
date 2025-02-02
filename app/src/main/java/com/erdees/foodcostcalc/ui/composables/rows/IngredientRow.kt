package com.erdees.foodcostcalc.ui.composables.rows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun IngredientRow(
    description: String,
    quantity: String,
    price: String,
    modifier: Modifier = Modifier,
    showPrice: Boolean = true,
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = description,
            style = style,
            modifier = Modifier.weight(if (showPrice) 0.6f else 0.8f)
        )
        Text(
            text = quantity,
            style = style,
            modifier = Modifier.weight(0.2f),
            textAlign =
            if (showPrice) TextAlign.Start else TextAlign.End
        )
        if (showPrice){
            Text(
                text = price,
                style = style,
                modifier = Modifier.weight(0.2f),
                textAlign = TextAlign.End
            )
        }
    }
}


@Preview
@Composable
private fun IngredientRowPreview() {
    Column {
        IngredientRow(description = "Potato", quantity = "200g", price = "0,33€")
        IngredientRow(description = "Chicken", quantity = "150g", price = "2,45€")
        IngredientRow(description = "Cauliflower", quantity = "180g", price = "0,53€")
        IngredientRow(description = "Olive oil", quantity = "20ml", price = "0,16€")
    }
}
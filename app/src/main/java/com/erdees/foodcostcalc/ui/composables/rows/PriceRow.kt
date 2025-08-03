package com.erdees.foodcostcalc.ui.composables.rows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun PriceRow(
    primaryText: String,
    secondaryText: String,
    price: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = buildAnnotatedString {
                append("$primaryText ")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = style.fontSize.value.minus(3).sp
                    )
                ) {
                    append(
                        stringResource(
                            R.string.unit_per_format,
                            secondaryText.lowercase()
                        )
                    )
                }
            }
        )
        Text(text = price, style = style)
    }
}

@Composable
fun PriceRow(
    description: String,
    price: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = description, style = style)
        Text(text = price, style = style)
    }
}


@Preview
@Composable
private fun IngredientRowPreview() {
    Column {
        PriceRow(description = "Price per litre", price = "12,33€")
        PriceRow(description = "Price per recipe", price = "4,22€")
    }
}


@Preview
@Composable
private fun NewIngredientRowPreview() {
    FCCTheme {
        Surface {
            Column {
                PriceRow(
                    primaryText = "Price with Waste",
                    secondaryText = "kilogram",
                    price = "12,33€"
                )
                PriceRow(primaryText = "Net Price", secondaryText = "kilogram", price = "12,33€")
            }
        }
    }
}
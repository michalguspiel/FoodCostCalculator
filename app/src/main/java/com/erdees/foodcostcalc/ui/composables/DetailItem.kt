package com.erdees.foodcostcalc.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun DetailItem(
  modifier: Modifier = Modifier,
  label: String,
  value: String,
  divider: Boolean = true
) {
  Column(
    modifier = modifier
  ) {
    Text(text = label, style = MaterialTheme.typography.labelSmall)
    Spacer(modifier = Modifier.size(2.dp))
    Text(text = value, style = MaterialTheme.typography.bodyLarge)
    Spacer(modifier = Modifier.size(8.dp))
    if (divider) {
      HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 0.9.dp)
    }
  }
}


@Preview
@Composable
private fun DetailItemPreview() {
  FCCTheme {
    Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
      Row {
        DetailItem(label = "Total cost", value = "100€", modifier = Modifier.weight(1f))
        DetailItem(label = "Food cost", value = "20€", modifier = Modifier.weight(1f))
      }
      Spacer(modifier = Modifier.size(8.dp))
      Row {
        DetailItem(label = "Margin", value = "300%", modifier = Modifier.weight(1f))
        DetailItem(label = "Tax", value = "23%", modifier = Modifier.weight(1f))
      }
    }
  }
}

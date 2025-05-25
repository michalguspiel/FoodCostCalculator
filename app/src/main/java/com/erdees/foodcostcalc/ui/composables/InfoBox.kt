package com.erdees.foodcostcalc.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun InfoBox(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = Icons.Sharp.Info,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoBoxPreview() {
    FCCTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoBox(
                title = "Important Information",
                description = "This is a detailed description providing context and further details about the information presented. It can span multiple lines."
            )
            Spacer(modifier = Modifier.size(16.dp))
            InfoBox(
                title = "No Icon Example",
                description = "This info box does not have an icon.",
                icon = null
            )
            Spacer(modifier = Modifier.size(16.dp))
            InfoBox(
                title = "Custom Icon Tint",
                description = "This info box has a different icon tint.",
                iconTint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
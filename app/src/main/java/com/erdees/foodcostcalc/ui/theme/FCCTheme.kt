
package com.erdees.foodcostcalc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//TODO DESIGN COLOR SCHEME, LOW IMPORTANCE
val FCCLightColorScheme: ColorScheme = lightColorScheme().copy(
  // Primary color in App, Represents accent color
  primary = Color(0xFFFF9800),

  secondary = Color(0xFFFF5100),

  tertiary = Color(0xFF000BFF),

  // Main Background color in App
  background = Color(0xFFFAFAFC),

  // Color of Top App Bar and Bottom App Bar
  surface = Color(0xFFD5D4D4),
  onSurface = Color(0xFF131313),
  onSurfaceVariant = Color(0xCE282828),

  // Surface color of all cards in the app: products, half products, dishes
  surfaceBright = Color(0xFFE9E4E0),

  secondaryContainer = Color(0xFFA2A2A2),

  error = Color(0xFFC50808),

  outline = Color(0xFFAFAAAA)
)

//TODO DESIGN COLOR SCHEME, LOW IMPORTANCE
val FCCDarkColorScheme: ColorScheme = darkColorScheme().copy(
  // Primary color in App, Represents accent color
  primary = Color(0xFFFF9800),

  // Secondary color in App, Represents text color
  secondary = Color(0xFF050505),

  // Main Background color in App
  background = Color(0xFFD5D4D4),

  // Color of Top App Bar and Bottom App Bar
  surface = Color(0xFF302F2F),
  onSurface = Color(0xFFFAFAFF),

  // Surface color of all cards in the app: products, half products, dishes
  surfaceBright = Color(0xFFF1EFEF),

  secondaryContainer = Color(0xFFA2A2A2)
)

@Composable
fun FCCTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = if (darkTheme) FCCDarkColorScheme else FCCLightColorScheme,
    content = content,
    typography = FCCType,
  )
}

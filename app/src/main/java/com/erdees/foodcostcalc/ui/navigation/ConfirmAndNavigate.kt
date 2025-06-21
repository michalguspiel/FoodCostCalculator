package com.erdees.foodcostcalc.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.ext.vibrateForConfirmation
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin


@Suppress("MagicNumber")
@Composable
fun ConfirmAndNavigate(
    visible: Boolean,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    navigate: () -> Unit = {},
) {
    val context = LocalContext.current
    val configuration = LocalWindowInfo.current
    val screenWidth = configuration.containerSize.width.dp
    val screenHeight = configuration.containerSize.height.dp
    val checkmarkProgress = remember { Animatable(progress) }
    val animationDuration = 300
    val currentNavigate by rememberUpdatedState(newValue = navigate)

    val cornerRadius by animateIntAsState(
        targetValue = if (visible) 0 else 100,
        animationSpec = tween(durationMillis = animationDuration)
    )

    val width by animateDpAsState(
        targetValue = if (visible) screenWidth else 100.dp,
        animationSpec = tween(durationMillis = animationDuration * 2, easing = EaseIn)
    )

    val height by animateDpAsState(
        targetValue = if (visible) screenHeight else 100.dp,
        animationSpec = tween(durationMillis = animationDuration * 2, easing = EaseIn)
    )

    LaunchedEffect(visible) {
        if (visible) {
            context.vibrateForConfirmation()
            checkmarkProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = animationDuration,
                    easing = EaseIn
                )
            )
            delay(animationDuration * 2L)
            currentNavigate()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.2f),
        exit = fadeOut()
    ) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .clip(RoundedCornerShape(percent = cornerRadius))
                    .background(MaterialTheme.colorScheme.scrim.copy(0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CheckmarkAnimation(progress = checkmarkProgress.value)
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
fun CheckmarkAnimation(progress: Float, modifier: Modifier = Modifier) {
    val cookieColor = MaterialTheme.colorScheme.primaryContainer
    val checkmarkColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.size(120.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val center = Offset(canvasWidth / 2, canvasHeight / 2)
        val radius =
            size.minDimension / 2.5f // Radius for the main circle, adjusted for cookie shape

        // 1. Define the "sharp cookie" path (star-like or gear-like)
        val cookiePath = Path().apply {
            val numPoints = 24 // Number of points for the star/cookie
            val outerRadius = size.minDimension / 2 // Outer boundary of the cookie points
            val innerRadius = radius * 1.4f // Inner dips of the cookie shape

            for (i in 0 until numPoints * 2) {
                val currentRadius = if (i % 2 == 0) outerRadius else innerRadius
                // Ensure angle calculation is correct for starting point (e.g., top) and direction
                val angle = (Math.PI / numPoints * i).toFloat() - (Math.PI / 2).toFloat()
                val x = center.x + cos(angle) * currentRadius
                val y = center.y + sin(angle) * currentRadius
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

        // 2. Draw the cookie shape as the background
        drawPath(
            path = cookiePath,
            color = cookieColor
        )

        // 3. Draw the progress circle INSIDE the cookie shape
        val circleColor = checkmarkColor
        drawCircle(
            color = circleColor,
            radius = radius,
            center = center,
            style = Stroke(width = 14f)
        )

        // 4. Define and draw the checkmark path (relative to the center of the canvas)
        val checkmarkPath = Path().apply {
            // Adjust coordinates to be relative to the center and scaled by the inner circle's radius
            // These values define the checkmark's shape and position within the circle
            val startX = center.x - radius * 0.48f  // Adjusted for visual centering
            val startY = center.y + radius * 0.05f  // Adjusted for visual centering
            val midX = center.x - radius * 0.06f  // Adjusted for visual centering
            val midY = center.y + radius * 0.38f  // Adjusted for visual centering
            val endX = center.x + radius * 0.48f  // Adjusted for visual centering
            val endY = center.y - radius * 0.28f  // Adjusted for visual centering

            moveTo(startX, startY)
            lineTo(midX, midY)
            lineTo(endX, endY)
        }

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(checkmarkPath, false)
        val pathLength = pathMeasure.length
        val drawnPath = Path()
        if (pathLength > 0f) {
            pathMeasure.getSegment(0f, pathLength * progress.coerceIn(0f, 1f), drawnPath, true)
        }

        drawPath(
            path = drawnPath,
            color = checkmarkColor,
            style = Stroke(width = 16f, cap = StrokeCap.Round)
        )
    }
}

class ProgressPreviewParameterProvider : PreviewParameterProvider<Float> {
    override val values = sequenceOf(0f, 0.5f, 1f)
}

@PreviewLightDark
@Preview
@Composable
private fun ConfirmAndNavigate_ProgressPreview(
    @PreviewParameter(ProgressPreviewParameterProvider::class) progress: Float
) {
    FCCTheme {
        ConfirmAndNavigate(true, progress = progress, modifier = Modifier.size(200.dp))
    }
}

/**
 * Interactive preview to see the transition from button to animation.
 * Note: Full animation and navigation won't work perfectly in a typical static preview.
 * For full interaction, use an interactive preview mode or run on an emulator/device.
 */
@Preview(name = "Interactive ConfirmAndNavigate")
@Composable
private fun InteractiveConfirmAndNavigatePreview() {
    FCCTheme {
        ConfirmAndNavigate(true)
    }
}
package com.erdees.foodcostcalc.ui.spotlight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import kotlin.math.roundToInt

class SpotlightController {
    internal var targets by mutableStateOf(listOf<SpotlightTargetData>())
    internal var currentIndex by mutableIntStateOf(0)
    val isActive: Boolean get() = targets.isNotEmpty() && currentIndex < targets.size
    fun next() {
        if (currentIndex < targets.size - 1) currentIndex++ else currentIndex = targets.size
    }

    fun start() {
        currentIndex = 0
    }

    fun stop() {
        currentIndex = targets.size
    }
}

@Composable
fun rememberSpotlightController() = remember { SpotlightController() }

data class SpotlightTargetData(
    val order: Int,
    val info: String,
    val rect: Rect?,
    val onNext: (() -> Unit)? = null
)

fun Modifier.spotlightTarget(
    order: Int,
    info: String,
    controller: SpotlightController? = null,
    onNext: (() -> Unit)? = null
): Modifier = this.then(
    Modifier
        .onGloballyPositioned { coordinates ->
            controller?.let {
                val rect = coordinates.boundsInRoot()
                val existing = it.targets.find { t -> t.order == order }
                if (existing == null || existing.rect != rect) {
                    it.targets =
                        (it.targets - listOfNotNull(existing).toSet()) + SpotlightTargetData(
                            order,
                            info,
                            rect,
                            onNext
                        )
                    it.targets = it.targets.sortedBy { t -> t.order }
                }
            }
        }
        .spotlightClickable(order, controller)
)

private fun Modifier.spotlightClickable(order: Int, controller: SpotlightController?): Modifier =
    if (controller?.isActive == true && controller.targets.getOrNull(controller.currentIndex)?.order == order) {
        this.clickable { controller.next() }
    } else {
        this
    }

@Composable
fun SpotlightOverlay(
    controller: SpotlightController,
    dimColor: Color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f),
    highlightPadding: Dp = 8.dp,
    infoTextColor: Color = MaterialTheme.colorScheme.onBackground,
    infoBackground: Color = MaterialTheme.colorScheme.background,
    nextButtonText: String = "Next",
    content: @Composable BoxScope.() -> Unit
) {
    var boxSize by remember { mutableStateOf<Rect?>(null) }
    val subtleBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                boxSize = coordinates.boundsInRoot()
            }
    ) {
        content()
        val current = controller.targets.getOrNull(controller.currentIndex)
        if (controller.isActive && current?.rect != null) {
            val density = LocalDensity.current
            val padPx = with(density) { highlightPadding.toPx() }
            val rect = current.rect
            val highlightRect = Rect(
                rect.left - padPx,
                rect.top - padPx,
                rect.right + padPx,
                rect.bottom + padPx
            )

            var infoBoxPosition: IntOffset? = null

            Canvas(Modifier.fillMaxSize()) {
                // First create a path for the hole (the area we want to keep visible)
                val holePath = Path().apply {
                    addRoundRect(RoundRect(highlightRect, 16f, 16f))
                }

                // Then create a path for the entire canvas
                val fullScreenPath = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                }

                // Subtract the hole from the full screen path to get our dim area
                val dimPath = Path().apply {
                    addPath(fullScreenPath)
                    op(fullScreenPath, holePath, PathOperation.Difference)
                }

                // Draw the dim overlay using our calculated path
                drawPath(dimPath, dimColor)

                // Draw a subtle border around the highlight
                drawRoundRect(
                    color = subtleBorderColor,
                    topLeft = Offset(highlightRect.left, highlightRect.top),
                    size = highlightRect.size,
                    cornerRadius = CornerRadius(16f, 16f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            boxSize?.let { size ->
                // Calculate info box position using measured box size
                val infoBoxPadding = with(density) { 16.dp.toPx() }
                val infoBoxHeight = with(density) { 64.dp.toPx() }

                val showBelow = (highlightRect.bottom + infoBoxPadding + infoBoxHeight) < size.bottom
                val infoBoxY = if (showBelow) {
                    highlightRect.bottom + infoBoxPadding
                } else {
                    highlightRect.top - infoBoxHeight - infoBoxPadding
                }

                infoBoxPosition = IntOffset(
                    x = highlightRect.left.roundToInt(),
                    y = infoBoxY.roundToInt()
                )
            }

            // Use calculated position outside Canvas
            infoBoxPosition?.let { position ->
                Box(
                    Modifier
                        .offset { position }
                        .width(with(density) { highlightRect.width.toDp() })
                        .background(infoBackground, shape = MaterialTheme.shapes.medium)
                        .padding(16.dp)
                ) {
                    Text(current.info, color = infoTextColor)
                }
            }

            Button(
                onClick = { controller.next() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            ) {
                Text(nextButtonText)
            }
        }
    }
}

@Composable
@PreviewLightDark
fun SpotlightPreview() {
    FCCTheme {
        val controller = rememberSpotlightController()
        LaunchedEffect(Unit) { controller.start() }
        SpotlightOverlay(controller = controller) {
            Column(
                Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Title",
                    modifier = Modifier
                        .spotlightTarget(0, "This is the title!", controller)
                        .padding(32.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
                Button(
                    onClick = {},
                    modifier = Modifier
                        .spotlightTarget(1, "Tap here to do something!", controller)
                        .padding(32.dp)
                ) {
                    Text("Action")
                }
            }
        }
    }
}

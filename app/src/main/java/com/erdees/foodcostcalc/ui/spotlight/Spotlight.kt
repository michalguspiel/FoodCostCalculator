package com.erdees.foodcostcalc.ui.spotlight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import timber.log.Timber
import kotlin.math.min
import kotlin.math.roundToInt

enum class SpotlightShape {
    Rectangle,
    RoundedRectangle,
    Circle
}

enum class SpotlightStep(
    val info: String,
    val shape: SpotlightShape = SpotlightShape.RoundedRectangle,
    val cornerRadius: Dp = 16.dp
) {
    ExampleDishCard(
        info = "Here's your example dish! Tap it to see more details.",
        shape = SpotlightShape.RoundedRectangle,
        cornerRadius = 16.dp
    ),
    AddIngredientsButton(
        info = "Add ingredients to your dish by clicking here!",
        shape = SpotlightShape.Circle,
        cornerRadius = 16.dp
    );

    fun toSpotlightTarget(
        customAction: (() -> Unit)? = null,
        onNext: (() -> Unit)? = null
    ) = SpotlightTarget(
        order = ordinal,
        info = info,
        rect = null,
        shape = shape,
        cornerRadius = cornerRadius,
        customAction = customAction,
        onNext = onNext
    )
}

data class SpotlightTarget(
    val order: Int,
    val info: String,
    val rect: Rect? = null,
    val shape: SpotlightShape = SpotlightShape.RoundedRectangle,
    val cornerRadius: Dp = 16.dp,
    val customAction: (() -> Unit)? = null,
    val onNext: (() -> Unit)? = null
)

class Spotlight {
    private var targets by mutableStateOf(listOf<SpotlightTarget>())
    private var currentIndex by mutableIntStateOf(-1)
    private var onCompleteCallback: (() -> Unit)? = null

    val isActive: Boolean get() = currentIndex >= 0 && currentIndex < targets.size
    val currentTarget: SpotlightTarget? get() = targets.getOrNull(currentIndex)

    fun start(targets: List<SpotlightTarget>, onComplete: () -> Unit = {}) {
        Timber.i("Spotlight starting with ${targets.size} targets.")
        this.targets = targets
        this.onCompleteCallback = onComplete
        currentIndex = 0
    }

    fun next() {
        Timber.i("Spotlight next. Current index: $currentIndex, total targets: ${targets.size}")
        if (currentIndex < targets.size - 1) {
            currentIndex++
        } else {
            stop()
        }
    }

    fun stop() {
        Timber.i("Spotlight stopping.")
        onCompleteCallback?.invoke()
        currentIndex = -1
        targets = emptyList()
    }

    fun updateTarget(order: Int, rect: Rect) {
        val existing = targets.find { it.order == order }
        if (existing != null && existing.rect != rect) {
            Timber.i("Spotlight updating target $order with rect $rect")
            targets = targets.map {
                if (it.order == order) it.copy(rect = rect)
                else it
            }
        }
    }
}

@Composable
fun rememberSpotlight() = remember { Spotlight() }

fun Modifier.spotlightTarget(
    target: SpotlightTarget,
    spotlight: Spotlight? = null
): Modifier = this.then(
    Modifier
        .onGloballyPositioned { coordinates ->
            val rect = coordinates.boundsInRoot()
            Timber.i("Spotlight target ${target.order} positioned at $rect")
            spotlight?.updateTarget(target.order, rect)
        }
        .let { modifier ->
            if (spotlight?.currentTarget?.order == target.order) {
                modifier.clickable {
                    Timber.i("Spotlight target ${target.order} clicked.")
                    target.customAction?.invoke() ?: spotlight.next()
                }
            } else modifier
        }
)

@Composable
fun SpotlightOverlay(
    spotlight: Spotlight,
    dimColor: Color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.85f),
    highlightPadding: Dp = 8.dp,
    infoTextColor: Color = MaterialTheme.colorScheme.onBackground,
    infoBackground: Color = MaterialTheme.colorScheme.background,
    nextButtonText: String = "Next",
    content: @Composable BoxScope.() -> Unit
) {
    var boxSize by remember { mutableStateOf<Rect?>(null) }
    val insets = WindowInsets.safeDrawing
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                boxSize = coordinates.boundsInRoot()
            }
    ) {
        content()
        val current = spotlight.currentTarget
        Timber.i("SpotlightOverlay recomposing. isActive: ${spotlight.isActive}, currentTarget: $current")
        if (spotlight.isActive && current?.rect != null) {
            val topInsetPx = with(density) { insets.getTop(density).toDp().toPx() }
            val leftInsetPx = with(density) { insets.getLeft(density, layoutDirection).toDp().toPx() }
            val rightInsetPx = with(density) { insets.getRight(density, layoutDirection).toDp().toPx() }
            val padPx = with(density) { highlightPadding.toPx() }
            val rect = current.rect
            val cornerRadiusPx = with(density) { current.cornerRadius.toPx() }

            val highlightRect = when (current.shape) {
                SpotlightShape.Circle -> {
                    val size = min(rect.width, rect.height)
                    Rect(
                        rect.center.x - size/2 - padPx,
                        rect.center.y - size/2 - padPx,
                        rect.center.x + size/2 + padPx,
                        rect.center.y + size/2 + padPx
                    )
                }
                else -> Rect(
                    rect.left - padPx - leftInsetPx,
                    rect.top - padPx - topInsetPx,
                    rect.right + padPx + rightInsetPx,
                    rect.bottom + padPx
                )
            }

            var infoBoxPosition: IntOffset? = null

            Canvas(Modifier.fillMaxSize()) {
                val holePath = Path().apply {
                    when (current.shape) {
                        SpotlightShape.Rectangle -> addRect(highlightRect)
                        SpotlightShape.RoundedRectangle -> addRoundRect(RoundRect(highlightRect, cornerRadiusPx, cornerRadiusPx))
                        SpotlightShape.Circle -> addOval(highlightRect)
                    }
                }

                val fullScreenPath = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                }

                val dimPath = Path().apply {
                    addPath(fullScreenPath)
                    op(fullScreenPath, holePath, PathOperation.Difference)
                }

                drawPath(dimPath, dimColor)
            }

            boxSize?.let { size ->
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

            infoBoxPosition?.let { position ->
                Box(
                    Modifier
                        .offset { position }
                        .width(with(density) { highlightRect.width.toDp() })
                        .background(infoBackground, shape = MaterialTheme.shapes.medium)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(current.info, color = infoTextColor)
                        if (current.customAction == null) {
                            Button(
                                onClick = {
                                    current.onNext?.invoke()
                                    spotlight.next()
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(nextButtonText)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
fun SpotlightPreview() {
    FCCTheme {
        val spotlight = rememberSpotlight()
        LaunchedEffect(Unit) { spotlight.start(SpotlightStep.entries.map { it.toSpotlightTarget() }) }
        SpotlightOverlay(spotlight = spotlight) {
            Column(
                Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Title",
                    modifier = Modifier
                        .spotlightTarget(
                            SpotlightTarget(
                                order = 0,
                                info = "This is the title!"
                            ),
                            spotlight
                        )
                        .padding(32.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
                Button(
                    onClick = {},
                    modifier = Modifier
                        .spotlightTarget(
                            SpotlightTarget(
                                order = 1,
                                info = "Tap here to do something!"
                            ),
                            spotlight
                        )
                        .padding(32.dp)
                ) {
                    Text("Action")
                }
            }
        }
    }
}

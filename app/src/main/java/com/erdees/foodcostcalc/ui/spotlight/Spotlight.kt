package com.erdees.foodcostcalc.ui.spotlight

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
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
    val hasNextButton: Boolean = false
) {
    ExampleDishCard(
        info = "Here's your example dish! Tap it to see more details.",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = false
    ),
    AddIngredientsButton(
        info = "Add ingredients to your dish by clicking here!",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = false
    );

    fun toSpotlightTarget(onClickAction: (() -> Unit)? = null) = SpotlightTarget(
        order = ordinal,
        info = info,
        rect = null,
        shape = shape,
        hasNextButton = hasNextButton,
        onClickAction = onClickAction
    )
}

data class SpotlightTarget(
    val order: Int,
    val info: String,
    val rect: Rect? = null,
    val shape: SpotlightShape = SpotlightShape.RoundedRectangle,
    val cornerRadius: Dp = 16.dp,
    val hasNextButton: Boolean,
    val onClickAction: (() -> Unit)? = null
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

    fun updateTarget(target: SpotlightTarget) {
        val index = targets.indexOfFirst { it.order == target.order }
        if (index == -1) return

        val currentTarget = targets[index]
        // To prevent recomposition loops, we only update if the rect has changed,
        // or if a new action is being set
        if (currentTarget.rect != target.rect ||
            (currentTarget.onClickAction == null && target.onClickAction != null)) {
            Timber.i("Spotlight updating target ${target.order} with rect ${target.rect}")
            targets = targets.toMutableList().apply {
                this[index] = currentTarget.copy(
                    rect = target.rect,
                    hasNextButton = target.hasNextButton,
                    onClickAction = target.onClickAction ?: currentTarget.onClickAction
                )
            }
        }
    }

    // Helper method to trigger the action associated with the current target
    fun triggerCurrentTargetAction() {
        currentTarget?.onClickAction?.invoke()
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
            spotlight?.updateTarget(target.copy(rect = rect))
        }
        .let { modifier ->
            Timber.i("Spotlight target ${target.order} modifier created. Current target: ${spotlight?.currentTarget?.order}")
            if (spotlight?.currentTarget?.order == target.order) {
                modifier.clickable {
                    Timber.i("Spotlight target ${target.order} clicked.")
                    spotlight.next()
                }
            } else modifier
        }
)

@Composable
fun SpotlightOverlay(
    spotlight: Spotlight,
    dimColor: Color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
    highlightPadding: Dp = 8.dp,
    infoTextColor: Color = MaterialTheme.colorScheme.onBackground,
    infoBackground: Color = MaterialTheme.colorScheme.background,
    nextButtonText: String = "Next",
    content: @Composable BoxScope.() -> Unit
) {
    var boxSize by remember { mutableStateOf<Rect?>(null) }
    val density = LocalDensity.current

    // Remember the target rect to animate to
    var targetHighlightRect by remember { mutableStateOf<Rect?>(null) }

    // Animate the highlight rect
    val animatedHighlightRect by animateRectAsState(
        targetValue = targetHighlightRect ?: Rect(0f, 0f, 0f, 0f),
        animationSpec = SpringSpec(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "spotlight_rect_animation"
    )

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
            val padPx = with(density) { highlightPadding.toPx() }
            val rect = current.rect
            val cornerRadiusPx = with(density) { current.cornerRadius.toPx() }

            // Calculate the target highlight rect
            val newHighlightRect = when (current.shape) {
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
                    rect.left - padPx,
                    rect.top - padPx,
                    rect.right + padPx,
                    rect.bottom + padPx
                )
            }

            // Update the target rect for animation
            LaunchedEffect(newHighlightRect) {
                targetHighlightRect = newHighlightRect
            }

            // Draw the dimming overlay with a cutout for the spotlighted area
            Canvas(Modifier.fillMaxSize()) {
                val holePath = Path().apply {
                    when (current.shape) {
                        SpotlightShape.Rectangle -> addRect(animatedHighlightRect)
                        SpotlightShape.RoundedRectangle -> addRoundRect(RoundRect(animatedHighlightRect, cornerRadiusPx, cornerRadiusPx))
                        SpotlightShape.Circle -> addOval(animatedHighlightRect)
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

            // Add a clickable overlay that covers only the dimmed part (not the spotlight)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(current) { // re-compose on current target change
                        detectTapGestures { offset ->
                            // Only intercept taps outside the spotlight area
                            val isOutsideSpotlight = when (current.shape) {
                                SpotlightShape.Circle -> {
                                    val centerX = animatedHighlightRect.center.x
                                    val centerY = animatedHighlightRect.center.y
                                    val radius = min(animatedHighlightRect.width, animatedHighlightRect.height) / 2
                                    val dx = offset.x - centerX
                                    val dy = offset.y - centerY
                                    (dx * dx + dy * dy) > (radius * radius)  // Outside circle
                                }
                                else -> {
                                    offset.x < animatedHighlightRect.left ||
                                    offset.x > animatedHighlightRect.right ||
                                    offset.y < animatedHighlightRect.top ||
                                    offset.y > animatedHighlightRect.bottom  // Outside rectangle
                                }
                            }

                            if (isOutsideSpotlight) {
                                Timber.i("Clicked on dimmed area - intercepting click")
                                // Do nothing, just intercept the click
                            } else {
                                Timber.i("Clicked on spotlight area - triggering action and navigating to next target")
                                // Trigger the associated action first if available
                                current.onClickAction?.invoke()

                                // Then advance to the next spotlight step
                                spotlight.next()
                            }
                        }
                    }
            )

            // Calculate the info box position based on animated highlight rect
            boxSize?.let { size ->
                val infoBoxPadding = with(density) { 16.dp.toPx() }
                val infoBoxHeight = with(density) { 64.dp.toPx() }

                val showBelow = (animatedHighlightRect.bottom + infoBoxPadding + infoBoxHeight) < size.bottom
                val infoBoxY = if (showBelow) {
                    animatedHighlightRect.bottom + infoBoxPadding
                } else {
                    animatedHighlightRect.top - infoBoxHeight - infoBoxPadding
                }

                val infoBoxPosition = IntOffset(
                    x = animatedHighlightRect.left.roundToInt(),
                    y = infoBoxY.roundToInt()
                )

                // Info box with explanation text and optional next button
                Box(
                    Modifier
                        .offset { infoBoxPosition }
                        .width(with(density) { animatedHighlightRect.width.toDp() })
                        .background(infoBackground, shape = MaterialTheme.shapes.medium)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(current.info, color = infoTextColor)
                        if (current.hasNextButton) {
                            Button(
                                onClick = {
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
                                info = "This is the title!",
                                hasNextButton = true
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
                                info = "Tap here to do something!",
                                hasNextButton = false
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

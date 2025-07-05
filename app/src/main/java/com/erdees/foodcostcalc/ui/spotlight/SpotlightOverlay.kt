package com.erdees.foodcostcalc.ui.spotlight

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import kotlin.math.min
import kotlin.math.roundToInt

// TODO FIX INFO BOX VISUALLY
@Composable
fun SpotlightOverlay(
    spotlight: Spotlight,
    dimColor: Color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
    highlightPadding: Dp = 8.dp,
    infoTextColor: Color = MaterialTheme.colorScheme.onBackground,
    infoBackground: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    nextButtonText: String = "Next",
    content: @Composable BoxScope.() -> Unit
) {
    var boxSize by remember { mutableStateOf<Rect?>(null) }
    val density = LocalDensity.current
    var infoBoxHeight by remember { mutableFloatStateOf(0f) }

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

        if (spotlight.isActive && current?.rect != null) {
            val padPx = with(density) { highlightPadding.toPx() }
            val rect = current.rect
            val cornerRadiusPx = with(density) { current.cornerRadius.toPx() }

            // Calculate the target highlight rect
            val newHighlightRect = when (current.shape) {
                SpotlightShape.Circle -> {
                    val size = min(rect.width, rect.height)
                    Rect(
                        rect.center.x - size / 2 - padPx,
                        rect.center.y - size / 2 - padPx,
                        rect.center.x + size / 2 + padPx,
                        rect.center.y + size / 2 + padPx
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
                        SpotlightShape.RoundedRectangle -> addRoundRect(
                            RoundRect(
                                animatedHighlightRect,
                                cornerRadiusPx,
                                cornerRadiusPx
                            )
                        )

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
                                    val radius = min(
                                        animatedHighlightRect.width,
                                        animatedHighlightRect.height
                                    ) / 2
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
                                spotlight.next()
                            }
                        }
                    }
            )

            // Calculate the info box position based on animated highlight rect
            boxSize?.let { size ->
                val infoBoxPadding = with(density) { 16.dp.toPx() }

                val safeInsets = WindowInsets.safeDrawing.asPaddingValues(density)
                val topInset = with(density) { safeInsets.calculateTopPadding().toPx() }
                val bottomInset = with(density) { safeInsets.calculateBottomPadding().toPx() }

                val spaceBelow =
                    size.bottom - bottomInset - animatedHighlightRect.bottom - infoBoxPadding
                val spaceAbove = animatedHighlightRect.top - topInset - infoBoxPadding

                val showBelow = (spaceBelow >= infoBoxHeight) || (spaceBelow > spaceAbove)

                val infoBoxY = if (showBelow) {
                    animatedHighlightRect.bottom + infoBoxPadding
                } else {
                    animatedHighlightRect.top - infoBoxHeight - infoBoxPadding
                }

                val finalInfoBoxY = infoBoxY.coerceIn(
                    topInset,
                    size.height - bottomInset - infoBoxHeight
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.safeDrawing.asPaddingValues())
                        .padding(horizontal = 16.dp)
                        .offset { IntOffset(x = 0, y = (finalInfoBoxY - topInset).roundToInt()) }
                        .onGloballyPositioned { layoutCoordinates ->
                            infoBoxHeight = layoutCoordinates.size.height.toFloat()
                        }
                        .background(infoBackground, shape = MaterialTheme.shapes.medium)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(current.info),
                        color = infoTextColor,
                        textAlign = TextAlign.Center
                    )
                    if (current.hasNextButton) {
                        FCCPrimaryButton(
                            nextButtonText,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            spotlight.next()
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
        val scope = rememberCoroutineScope()
        val spotlight = rememberSpotlight(scope)
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
                                info = R.string.products,
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
                                info = R.string.spotlight_details_button,
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

@Composable
fun rememberSpotlight(scope: CoroutineScope = rememberCoroutineScope()) = remember { Spotlight(scope) }

fun Modifier.spotlightTarget(
    target: SpotlightTarget,
    spotlight: Spotlight? = null
): Modifier = this.then(
    Modifier
        .onGloballyPositioned { coordinates ->
            val rect = coordinates.boundsInRoot()
            Timber.v("Spotlight target ${target.order} positioned at $rect")
            spotlight?.updateTarget(target.copy(rect = rect))
        }
)
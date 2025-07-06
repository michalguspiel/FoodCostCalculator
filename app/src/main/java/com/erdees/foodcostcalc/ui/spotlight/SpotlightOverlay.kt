package com.erdees.foodcostcalc.ui.spotlight

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.math.min
import kotlin.math.roundToInt

private const val FirstSpotlightIndex = 0
private const val FirstPromptPopUpDelayMs = 500L

@Composable
fun SpotlightOverlay(
    spotlight: Spotlight,
    modifier: Modifier = Modifier,
    dimColor: Color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
    highlightPadding: Dp = 8.dp,
    nextButtonText: String = stringResource(R.string.spotlight_next),
    content: @Composable (BoxScope.() -> Unit)
) {
    var boxSize by remember { mutableStateOf<Rect?>(null) }
    var infoBoxHeight by remember { mutableFloatStateOf(0f) }
    var showInfoBox by remember { mutableStateOf(false) }
    var targetHighlightRect by remember { mutableStateOf<Rect?>(null) }

    val animatedHighlightRect by animateRectAsState(
        targetValue = targetHighlightRect ?: Rect.Zero,
        animationSpec = SpringSpec(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "spotlight_rect_animation"
    )

    val currentTarget = spotlight.currentTarget
    val density = LocalDensity.current

    LaunchedEffect(currentTarget) {
        if (currentTarget != null) {
            if (spotlight.currentIndex == FirstSpotlightIndex) {
                delay(FirstPromptPopUpDelayMs)
            }
            showInfoBox = true
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                boxSize = coordinates.boundsInRoot()
            }
    ) {
        content()

        if (spotlight.isActive && currentTarget != null) {
            val padPx = with(density) { highlightPadding.toPx() }
            val newHighlightRect = calculateHighlightRect(currentTarget, padPx)

            LaunchedEffect(newHighlightRect) {
                targetHighlightRect = newHighlightRect
            }

            DimOverlay(
                dimColor = dimColor,
                animatedHighlightRect = animatedHighlightRect,
                currentTarget = currentTarget
            )

            ClickableOverlay(
                animatedHighlightRect = animatedHighlightRect,
                currentTarget = currentTarget,
                spotlight = spotlight
            )

            boxSize?.let { size ->
                InfoBox(
                    boxSize = size,
                    animatedHighlightRect = animatedHighlightRect,
                    infoBoxHeight = infoBoxHeight,
                    setInfoBoxHeight = { infoBoxHeight = it },
                    showInfoBox = showInfoBox,
                    currentTarget = currentTarget,
                    nextButtonText = nextButtonText,
                    onNext = { spotlight.next() }
                )
            }
        }
    }
}

@Composable
private fun DimOverlay(
    dimColor: Color,
    animatedHighlightRect: Rect,
    currentTarget: SpotlightTarget
) {
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { currentTarget.cornerRadius.toPx() }

    Canvas(Modifier.fillMaxSize()) {
        val holePath = Path().apply {
            when (currentTarget.shape) {
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
}

@Composable
private fun ClickableOverlay(
    animatedHighlightRect: Rect,
    currentTarget: SpotlightTarget,
    spotlight: Spotlight
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(currentTarget) {
                detectTapGestures { offset ->
                    val isOutsideSpotlight = when (currentTarget.shape) {
                        SpotlightShape.Circle -> {
                            val centerX = animatedHighlightRect.center.x
                            val centerY = animatedHighlightRect.center.y
                            val radius = min(
                                animatedHighlightRect.width,
                                animatedHighlightRect.height
                            ) / 2
                            val dx = offset.x - centerX
                            val dy = offset.y - centerY
                            (dx * dx + dy * dy) > (radius * radius)
                        }
                        else -> {
                            offset.x < animatedHighlightRect.left ||
                                    offset.x > animatedHighlightRect.right ||
                                    offset.y < animatedHighlightRect.top ||
                                    offset.y > animatedHighlightRect.bottom
                        }
                    }

                    if (isOutsideSpotlight) {
                        Timber.i("Clicked on dimmed area - intercepting click")
                        spotlight.clickedOutsideSpotlightArea()
                    } else {
                        Timber.i("Clicked on spotlight area - triggering action and navigating to next target")
                        spotlight.next()
                    }
                }
            }
    )
}

@Composable
private fun InfoBox(
    boxSize: Rect,
    animatedHighlightRect: Rect,
    infoBoxHeight: Float,
    setInfoBoxHeight: (Float) -> Unit,
    showInfoBox: Boolean,
    currentTarget: SpotlightTarget,
    nextButtonText: String,
    onNext: () -> Unit
) {
    val density = LocalDensity.current
    val infoBoxPadding = with(density) { 16.dp.toPx() }

    val safeInsets = WindowInsets.safeDrawing.asPaddingValues(density)
    val topInset = with(density) { safeInsets.calculateTopPadding().toPx() }
    val bottomInset = with(density) { safeInsets.calculateBottomPadding().toPx() }

    val spaceBelow =
        boxSize.bottom - bottomInset - animatedHighlightRect.bottom - infoBoxPadding
    val spaceAbove = animatedHighlightRect.top - topInset - infoBoxPadding

    val showBelow = (spaceBelow >= infoBoxHeight) || (spaceBelow > spaceAbove)

    val infoBoxY = if (showBelow) {
        animatedHighlightRect.bottom + infoBoxPadding
    } else {
        animatedHighlightRect.top - infoBoxHeight - infoBoxPadding
    }

    val finalInfoBoxY = infoBoxY.coerceIn(
        topInset,
        boxSize.height - bottomInset - infoBoxHeight
    )

    AnimatedVisibility(showInfoBox) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(horizontal = 16.dp)
                .offset { IntOffset(x = 0, y = (finalInfoBoxY - topInset).roundToInt()) }
                .onGloballyPositioned { layoutCoordinates ->
                    setInfoBoxHeight(layoutCoordinates.size.height.toFloat())
                }
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.medium)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(currentTarget.info),
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                textAlign = TextAlign.Center
            )
            if (currentTarget.hasNextButton) {
                FCCPrimaryButton(
                    nextButtonText,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    onNext()
                }
            }
        }
    }
}

private fun calculateHighlightRect(currentTarget: SpotlightTarget, padPx: Float): Rect {
    val rect = currentTarget.rect ?: return Rect.Zero
    return when (currentTarget.shape) {
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
}

@Composable
@Preview
@PreviewLightDark
private fun SpotlightPreview() {
    FCCTheme {
        val spotlight = Spotlight(rememberCoroutineScope())
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
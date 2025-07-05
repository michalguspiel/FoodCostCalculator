package com.erdees.foodcostcalc.ui.spotlight

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages the state and flow of a spotlight tour.
 * This class is responsible for orchestrating the display of spotlight targets in a sequential manner.
 *
 * @param scope The coroutine scope to launch animations and other async operations.
 */
class Spotlight(private val scope: CoroutineScope) {

    /** The list of targets to be displayed in the spotlight tour. */
    internal var targets by mutableStateOf<List<SpotlightTarget>>(emptyList())
        private set

    /** The index of the currently active target. */
    var currentIndex by mutableIntStateOf(-1)
        private set

    /** Indicates whether the spotlight tour is currently active. */
    val isActive: Boolean
        get() = currentIndex in targets.indices

    /** The currently active spotlight target. */
    val currentTarget: SpotlightTarget?
        get() = targets.getOrNull(currentIndex)

    /**
     * Starts the spotlight tour with a given list of targets.
     * If the list is empty, the spotlight will not be activated.
     *
     * @param targets The list of [SpotlightTarget] to display.
     */
    fun start(targets: List<SpotlightTarget>) {
        Timber.i("Spotlight starting with ${targets.size} targets.")
        this.targets = targets
        if (targets.isNotEmpty()) {
            currentIndex = 0
            scrollToCurrentTarget()
        }
    }

    /**
     * Moves to the next target in the tour.
     * If the current target is the last one, the tour is stopped.
     */
    fun next() {
        if (!isActive) return
        Timber.i("Spotlight next. Current target: $currentIndex: ${currentTarget}, total targets: ${targets.size}")

        currentTarget?.onClickAction?.invoke()

        if (currentIndex < targets.size - 1) {
            currentIndex++
            scrollToCurrentTarget()
        } else {
            stop()
        }
    }

    /** Stops the spotlight tour and cleans up the state. */
    private fun stop() {
        Timber.i("Spotlight stopping.")
        currentIndex = -1
        targets = emptyList()
    }

    /**
     * Updates a target in the list with new properties.
     * This is useful for updating a target's position or actions dynamically.
     *
     * @param newTarget The target containing the updated properties.
     */
    fun updateTarget(newTarget: SpotlightTarget) {
        val index = targets.indexOfFirst { it.order == newTarget.order }
        if (index == -1) return

        val oldTarget = targets[index]

        val needsUpdate = oldTarget.rect != newTarget.rect ||
                (oldTarget.onClickAction == null && newTarget.onClickAction != null) ||
                (oldTarget.scrollToElement == null && newTarget.scrollToElement != null)

        if (needsUpdate) {
            Timber.i("Spotlight updating target ${newTarget.order} with rect ${newTarget.rect}")
            targets = targets.toMutableList().apply {
                this[index] = oldTarget.copy(
                    rect = newTarget.rect,
                    hasNextButton = newTarget.hasNextButton,
                    onClickAction = newTarget.onClickAction ?: oldTarget.onClickAction,
                    scrollToElement = newTarget.scrollToElement ?: oldTarget.scrollToElement
                )
            }
        }
    }

    fun clickedOutsideSpotlightArea(){
        when(currentTarget?.order) {
            SpotlightStep.CreateDishFAB.ordinal -> {
                stop()
            }
        }
    }

    private fun scrollToCurrentTarget() {
        scope.launch {
            currentTarget?.scrollToElement?.invoke()
        }
    }
}
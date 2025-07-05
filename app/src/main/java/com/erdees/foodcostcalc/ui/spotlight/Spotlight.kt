package com.erdees.foodcostcalc.ui.spotlight

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * TODO:
 *  - Test Recompositions
 *  - Test Why Expanding Dish does flicker the UI
 */
/**
 * Manages the state and flow of a spotlight tour.
 * This class is responsible for orchestrating the display of spotlight targets in a sequential manner.
 */
class Spotlight(private val scope: CoroutineScope) : KoinComponent {

    private val analyticsRepository by inject<AnalyticsRepository>()

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
            logStepShown()
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
            logStepShown()
            scrollToCurrentTarget()
        } else {
            logCompleted()
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

        val needsUpdate = oldTarget.rect != newTarget.rect && (oldTarget.order == currentTarget?.order || newTarget.order == currentTarget?.order) ||
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
        logClickedOutsideSpotlightArea()
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

    private fun logStepShown() {
        currentTarget?.let {
            val bundle = Bundle().apply {
                putInt(Constants.Analytics.Onboarding.STEP_INDEX, currentIndex)
                putString(
                    Constants.Analytics.Onboarding.STEP_NAME,
                    SpotlightStep.entries.getOrNull(currentIndex)?.name ?: Constants.Analytics.Onboarding.UNKNOWN_STEP
                )
            }
            analyticsRepository.logEvent(Constants.Analytics.Onboarding.STEP_SHOWN, bundle)
        }
    }

    private fun logCompleted() {
        analyticsRepository.logEvent(Constants.Analytics.Onboarding.COMPLETED, null)
    }

    private fun logClickedOutsideSpotlightArea() {
        val bundle = Bundle().apply {
            putInt(Constants.Analytics.Onboarding.STEP_INDEX, currentIndex)
            putString(
                Constants.Analytics.Onboarding.STEP_NAME,
                SpotlightStep.entries.getOrNull(currentIndex)?.name ?: Constants.Analytics.Onboarding.UNKNOWN_STEP
            )
        }
        analyticsRepository.logEvent(
            Constants.Analytics.Onboarding.OUTSIDE_SPOTLIGHT_AREA_CLICKED, bundle
        )
    }
}
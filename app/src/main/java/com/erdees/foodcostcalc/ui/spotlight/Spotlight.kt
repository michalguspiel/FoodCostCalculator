package com.erdees.foodcostcalc.ui.spotlight

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class Spotlight(
    private val scope: CoroutineScope
) {
    private var targets by mutableStateOf(listOf<SpotlightTarget>())
    private var currentIndex by mutableIntStateOf(-1)

    val isActive: Boolean get() = currentIndex >= 0 && currentIndex < targets.size && targets.isNotEmpty()
    val currentTarget: SpotlightTarget? get() = targets.getOrNull(currentIndex)

    fun start(targets: List<SpotlightTarget>) {
        Timber.i("Spotlight starting with ${targets.size} targets.")
        this.targets = targets
        currentIndex = 0
        scope.launch { currentTarget?.scrollToElement?.invoke() }
    }

    fun next() {
        Timber.i("Spotlight next. Current target: $currentIndex: ${targets.getOrNull(currentIndex)}, total targets: ${targets.size}")
        // call the action of target before incrementing the index
        currentTarget?.onClickAction?.invoke()
        if (currentIndex < targets.size - 1) {
            // Increment the index to move to the next target
            currentIndex++
            // scroll to the next target if it has a scroll action
            scope.launch {
                currentTarget?.scrollToElement?.invoke()
            }
        } else {
            stop()
        }
    }

    private fun stop() {
        Timber.i("Spotlight stopping.")
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
            (currentTarget.onClickAction == null && target.onClickAction != null) ||
            (currentTarget.scrollToElement == null && target.scrollToElement != null)
            ) {
            Timber.i("Spotlight updating target ${target.order} with rect ${target.rect}")
            targets = targets.toMutableList().apply {
                this[index] = currentTarget.copy(
                    rect = target.rect,
                    hasNextButton = target.hasNextButton,
                    onClickAction = target.onClickAction ?: currentTarget.onClickAction,
                    scrollToElement = target.scrollToElement ?: currentTarget.scrollToElement
                )
            }
        }
    }
}
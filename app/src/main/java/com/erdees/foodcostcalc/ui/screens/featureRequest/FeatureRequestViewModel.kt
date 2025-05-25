package com.erdees.foodcostcalc.ui.screens.featureRequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.domain.usecase.SubmitFeatureRequestUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FeatureRequestViewModel : ViewModel(), KoinComponent {

    private val submitFeatureRequestUseCase: SubmitFeatureRequestUseCase by inject()

    private val _screenState = MutableStateFlow(FeatureRequestScreenState.IDLE)
    val screenState: StateFlow<FeatureRequestScreenState> = _screenState

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    val isSubmitButtonEnabled: StateFlow<Boolean> =
        combine(title, description, screenState) { title, description, screenState ->
            title.isNotBlank() && description.isNotBlank() && screenState != FeatureRequestScreenState.LOADING
        }.stateIn(viewModelScope, SharingStarted.Lazily, false)


    fun updateTitle(value: String) {
        _title.value = value
    }

    fun updateDescription(value: String) {
        _description.value = value
    }

    fun submitFeatureRequest() {
        viewModelScope.launch {
            _screenState.value = FeatureRequestScreenState.LOADING
            val result = submitFeatureRequestUseCase.invoke(title.value, description.value)
            when {
                result.isSuccess -> _screenState.value = FeatureRequestScreenState.SUCCESS
                result.isFailure -> _screenState.value = FeatureRequestScreenState.ERROR
            }
        }
    }

    fun resetScreenState() {
        _screenState.value = FeatureRequestScreenState.IDLE
    }
}
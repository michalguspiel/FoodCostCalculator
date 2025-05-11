package com.erdees.foodcostcalc.ui.viewModel

import androidx.lifecycle.ViewModel
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.utils.Constants
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce

open class FCCBaseViewModel : ViewModel() {
    private var _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> = _screenState

    private var _searchKey: MutableStateFlow<String> = MutableStateFlow("")
    val searchKey: StateFlow<String> = _searchKey

    @OptIn(FlowPreview::class)
    val debouncedSearch = _searchKey.debounce(Constants.UI.SEARCH_DEBOUNCE_MS)

    fun updateSearchKey(searchKey: String) {
        _searchKey.value = searchKey
    }

    fun updateScreenState(screenState: ScreenState) {
        _screenState.value = screenState
    }

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }
}
package com.erdees.foodcostcalc.ui.screens.featureRequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.model.remote.FeatureRequest
import com.erdees.foodcostcalc.data.model.remote.FirestoreResult
import com.erdees.foodcostcalc.data.remote.FeatureRequestService
import com.erdees.foodcostcalc.data.repository.FeatureRequestRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toEntity
import com.erdees.foodcostcalc.utils.MyDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

class FeatureRequestViewModel : ViewModel(), KoinComponent {

    private val dispatchers: MyDispatchers by inject()
    private val featureRequestService: FeatureRequestService by inject()
    private val featureRequestRepository: FeatureRequestRepository by inject()

    private val _screenState = MutableStateFlow(FeatureRequestScreenState.IDLE)
    val screenState: StateFlow<FeatureRequestScreenState> = _screenState

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    fun updateTitle(value: String) {
        _title.value = value
    }

    fun updateDescription(value: String) {
        _description.value = value
    }

    fun submitFeatureRequest(title: String, description: String) {
        viewModelScope.launch(dispatchers.ioDispatcher) {
            _screenState.value = FeatureRequestScreenState.LOADING
            try {
                val now = ZonedDateTime.now(ZoneId.systemDefault())
                val timestamp = Date.from(now.toInstant())
                val featureRequest = FeatureRequest(
                    title = title,
                    description = description,
                )
                when (val response = featureRequestService.submitFeatureRequest(featureRequest)) {
                    is FirestoreResult.Success -> {
                        Timber.d("Success: ${response.data}")
                        featureRequestRepository.insertFeatureRequest(
                            featureRequest.toEntity(
                                response.data,
                                timestamp
                            )
                        )
                        Timber.d("Feature request inserted to local DB")
                        _screenState.value = FeatureRequestScreenState.SUCCESS
                    }

                    is FirestoreResult.Error -> {
                        Timber.e("Error: ${response.exception.message}", response.exception)
                        _screenState.value = FeatureRequestScreenState.ERROR
                    }
                }
            } catch (e: Exception) {
                Timber.e("Exception: ${e.message}", e)
                _screenState.value = FeatureRequestScreenState.ERROR
            }
        }
    }

    fun resetScreenState() {
        _screenState.value = FeatureRequestScreenState.IDLE
    }
}
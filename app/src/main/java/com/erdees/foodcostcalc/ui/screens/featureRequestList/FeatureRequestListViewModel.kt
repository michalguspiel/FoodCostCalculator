package com.erdees.foodcostcalc.ui.screens.featureRequestList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.model.local.FeatureRequestEntity
import com.erdees.foodcostcalc.data.model.remote.FirestoreResult
import com.erdees.foodcostcalc.data.remote.FeatureRequestService
import com.erdees.foodcostcalc.data.repository.FeatureRequestRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toFeatureRequestDomain
import com.erdees.foodcostcalc.domain.model.FeatureRequestDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class FeatureRequestListViewModel : ViewModel(), KoinComponent {

    private val featureRequestRepository: FeatureRequestRepository by inject()
    private val featureRequestService: FeatureRequestService by inject()

    private val localFeatureRequestsFlow: Flow<List<FeatureRequestEntity>> =
        featureRequestRepository.getFeatureRequests()
            .catch { e ->
                Timber.e(e, "Error fetching local feature requests flow")
                emit(emptyList()) // Emit empty list on error or handle appropriately
            }

    val featureRequestList: StateFlow<List<FeatureRequestDomain>?> =
        combine(
            featureRequestService.getApprovedFeatureRequestsFlow(),
            localFeatureRequestsFlow
        ) { remoteResult, localRequests ->
            Timber.d("Combined feature requests flow called $remoteResult $localRequests")
            when (remoteResult) {
                is FirestoreResult.Success -> {
                    val remoteApprovedRequests = remoteResult.data
                    val remoteApprovedIds = remoteApprovedRequests.mapNotNull { it.id }.toSet()

                    val unapprovedLocalFeatureRequestsToDisplay = localRequests
                        .filter { localRequest -> localRequest.id !in remoteApprovedIds }
                        .map { it.toFeatureRequestDomain() }

                    // Combine remote approved with local unapproved
                    remoteApprovedRequests.mapNotNull { it.toFeatureRequestDomain() } + unapprovedLocalFeatureRequestsToDisplay
                }

                is FirestoreResult.Error -> {
                    Timber.e(
                        remoteResult.exception,
                        "Error from remote feature requests flow, showing only local data."
                    )
                    localRequests.map { it.toFeatureRequestDomain() }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
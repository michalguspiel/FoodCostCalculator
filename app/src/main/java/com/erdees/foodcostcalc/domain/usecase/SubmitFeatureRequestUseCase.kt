package com.erdees.foodcostcalc.domain.usecase

import com.erdees.foodcostcalc.data.model.remote.FeatureRequest
import com.erdees.foodcostcalc.data.model.remote.FirestoreResult
import com.erdees.foodcostcalc.data.remote.FeatureRequestService
import com.erdees.foodcostcalc.data.repository.FeatureRequestRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toEntity
import com.erdees.foodcostcalc.utils.MyDispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

/**
 * Submits FeatureRequest to firebase and creates a local copy in ROOM DB.
 * */
class SubmitFeatureRequestUseCase(
    private val featureRequestService: FeatureRequestService,
    private val featureRequestRepository: FeatureRequestRepository,
    private val dispatchers: MyDispatchers,
) {
    suspend operator fun invoke(title: String, description: String): Result<Unit> {
        return withContext(dispatchers.ioDispatcher) {
            try {
                val now = ZonedDateTime.now(ZoneId.systemDefault())
                val timestamp = Date.from(now.toInstant())
                val featureRequest = FeatureRequest(title = title, description = description)

                when (val response = featureRequestService.submitFeatureRequest(featureRequest)) {
                    is FirestoreResult.Success -> {
                        featureRequestRepository.insertFeatureRequest(
                            featureRequest.toEntity(response.data, timestamp)
                        )
                        Result.success(Unit)
                    }

                    is FirestoreResult.Error -> {
                        Timber.e("Submit failed: ${response.exception.message}", response.exception)
                        Result.failure(response.exception)
                    }
                }
            } catch (e: Exception) {
                Timber.e("Unexpected error: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}

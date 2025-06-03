package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.featurerequest.FeatureRequestDao
import com.erdees.foodcostcalc.data.model.local.FeatureRequestEntity
import com.erdees.foodcostcalc.data.model.local.UpvotedFeatureRequest
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


interface FeatureRequestRepository {
    suspend fun insertFeatureRequest(featureRequest: FeatureRequestEntity)
    fun getFeatureRequests(): Flow<List<FeatureRequestEntity>>
    fun getUpvotedFeatureRequests(): Flow<List<UpvotedFeatureRequest>>
    suspend fun upvoteFeatureRequest(id: String)
    suspend fun removeUpvote(id: String)
}

class FeatureRequestRepositoryImpl : FeatureRequestRepository, KoinComponent {
    private val featureRequestDao: FeatureRequestDao by inject()

    override fun getFeatureRequests(): Flow<List<FeatureRequestEntity>> =
        featureRequestDao.getFeatureRequests()

    override fun getUpvotedFeatureRequests(): Flow<List<UpvotedFeatureRequest>> =
        featureRequestDao.getUpvotedFeatureRequests()

    override suspend fun insertFeatureRequest(featureRequest: FeatureRequestEntity) {
        featureRequestDao.insertFeatureRequest(featureRequest)
    }

    override suspend fun upvoteFeatureRequest(id: String) {
        featureRequestDao.upvoteFeatureRequest(UpvotedFeatureRequest(id))
    }

    override suspend fun removeUpvote(id: String) {
        featureRequestDao.removeUpvote(id)
    }
}
package com.erdees.foodcostcalc.data.db.dao.featurerequest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.erdees.foodcostcalc.data.model.local.FeatureRequestEntity
import com.erdees.foodcostcalc.data.model.local.UpvotedFeatureRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface FeatureRequestDao {

    @Insert
    suspend fun insertFeatureRequest(featureRequest: FeatureRequestEntity)

    @Query("SELECT * FROM feature_requests ORDER BY timestamp DESC")
    fun getFeatureRequests(): Flow<List<FeatureRequestEntity>>

    @Query("SELECT * FROM upvoted_feature_requests")
    fun getUpvotedFeatureRequests(): Flow<List<UpvotedFeatureRequest>>

    @Insert
    suspend fun upvoteFeatureRequest(upvotedFeatureRequest: UpvotedFeatureRequest)

    @Query("DELETE FROM upvoted_feature_requests WHERE id = :id")
    suspend fun removeUpvote(id: String)
}
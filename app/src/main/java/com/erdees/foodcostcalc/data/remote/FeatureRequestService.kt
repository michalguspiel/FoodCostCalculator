package com.erdees.foodcostcalc.data.remote

import com.erdees.foodcostcalc.data.model.remote.FeatureRequest
import com.erdees.foodcostcalc.data.model.remote.FirestoreResult
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FeatureRequestService {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val featureRequestsCollection: CollectionReference = db.collection("feature-requests")

    /**
     * Submits a new feature request to Firestore.
     * The 'approved' field will be false by default as per the data class.
     * The 'timestamp' field will be set by the server.
     */
    suspend fun submitFeatureRequest(featureRequest: FeatureRequest): FirestoreResult<String> {
        return try {
            val documentReference = featureRequestsCollection.add(featureRequest).await()
            Timber.d("Success submitting feature request")
            FirestoreResult.Success(documentReference.id)
        } catch (e: Exception) {
            Timber.e(e, "Error submitting feature request")
            FirestoreResult.Error(e)
        }
    }

    /**
     * Fetches all feature requests that are marked as 'approved'.
     * Returns a Flow that emits a list of requests whenever the approved requests change.
     */
    fun getApprovedFeatureRequestsFlow(): Flow<FirestoreResult<List<FeatureRequest>>> =
        callbackFlow {
            // Query for documents where 'approved' is true, order by timestamp descending
            val query = featureRequestsCollection
                .whereEqualTo("approved", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)

            // Listen for real-time updates
            val listenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening for approved feature requests")
                    trySend(FirestoreResult.Error(error))
                    close(error) // Close the flow on error
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val requests = snapshot.toObjects(FeatureRequest::class.java)
                    trySend(FirestoreResult.Success(requests))
                } else {
                    trySend(FirestoreResult.Success(emptyList())) // Or handle as an error if snapshot is unexpectedly null
                }
            }
            // When the Flow is cancelled, remove the listener
            awaitClose {
                Timber.d("Cancelling approved feature requests listener")
                listenerRegistration.remove()
            }
        }
}
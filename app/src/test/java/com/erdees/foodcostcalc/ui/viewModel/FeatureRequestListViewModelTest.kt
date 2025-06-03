package com.erdees.foodcostcalc.ui.viewModel

import app.cash.turbine.test
import com.erdees.foodcostcalc.data.model.local.FeatureRequestEntity
import com.erdees.foodcostcalc.data.model.remote.FeatureRequest
import com.erdees.foodcostcalc.data.model.remote.FeatureRequestStatus
import com.erdees.foodcostcalc.data.model.remote.FirestoreResult
import com.erdees.foodcostcalc.data.remote.FeatureRequestService
import com.erdees.foodcostcalc.data.repository.FeatureRequestRepository
import com.erdees.foodcostcalc.domain.model.FeatureRequestDomain
import com.erdees.foodcostcalc.ui.screens.featureRequestList.FeatureRequestListViewModel
import com.erdees.foodcostcalc.utils.Formatter
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.util.Date

@ExperimentalCoroutinesApi
class FeatureRequestListViewModelTest : KoinTest {

    private lateinit var viewModel: FeatureRequestListViewModel
    private lateinit var featureRequestRepository: FeatureRequestRepository
    private lateinit var featureRequestService: FeatureRequestService

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        featureRequestRepository = mockk()
        featureRequestService = mockk()

        startKoin {
            modules(module {
                single { featureRequestRepository }
                single { featureRequestService }
            })
        }
        // Must be instantiated after Koin setup for inject() to work
        viewModel = FeatureRequestListViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `initial featureRequestList should be null`() = runTest {
        viewModel.featureRequestList.value.shouldBeNull()
    }

    @Test
    fun `featureRequestList should combine remote approved and local unapproved requests`() = runTest {
        val date = Date()
        val formattedDate = Formatter.formatTimeStamp(date)

        val remoteApprovedRequest = FeatureRequest(id = "remote1", title = "Remote Approved", description = "Desc1", status = FeatureRequestStatus.APPROVED.name, timestamp = date)
        val localPendingRequestEntity = FeatureRequestEntity(id = "local1", title = "Local Pending", description = "Desc2", status = FeatureRequestStatus.PENDING.name, timestamp = date, isShown = true)
        // This local request is also in remote, so it should not be duplicated if its status is also approved or similar.
        // However, the viewmodel logic is: remote approved + local *unapproved*.
        // So if a local one is also remote AND approved, it's just taken from remote.
        val localAlsoRemoteButApprovedEntity = FeatureRequestEntity(id = "remote1", title = "Local but Remote Approved", description = "Desc1", status = FeatureRequestStatus.APPROVED.name, timestamp = date, isShown = true)

        every { featureRequestService.getApprovedFeatureRequestsFlow() } returns flowOf(FirestoreResult.Success(listOf(remoteApprovedRequest)))
        every { featureRequestRepository.getFeatureRequests() } returns flowOf(listOf(localPendingRequestEntity, localAlsoRemoteButApprovedEntity))

        viewModel.featureRequestList.test {
            val items = awaitItem() // This will be the initial null, then the combined list
            items.shouldBeNull() // initial value

            val actualItems = awaitItem()
            val expectedItems = listOf(
                FeatureRequestDomain("remote1", "Remote Approved", "Desc1", FeatureRequestStatus.APPROVED, formattedDate, 0),
                FeatureRequestDomain("local1", "Local Pending", "Desc2", FeatureRequestStatus.PENDING, formattedDate, 0)
            )
            actualItems shouldContainExactlyInAnyOrder expectedItems
        }
    }

    @Test
    fun `featureRequestList should show only local requests if remote fails`() = runTest {
        val date1 = Date()
        val date2 = Date(System.currentTimeMillis() - 100000)
        val formattedDate1 = Formatter.formatTimeStamp(date1)
        val formattedDate2 = Formatter.formatTimeStamp(date2)


        val localRequest1 = FeatureRequestEntity("local1", "Local One", "Desc1", FeatureRequestStatus.PENDING.name, date1, true)
        val localRequest2 = FeatureRequestEntity("local2", "Local Two", "Desc2", FeatureRequestStatus.IN_PROGRESS.name, date2, true)

        every { featureRequestService.getApprovedFeatureRequestsFlow() } returns flowOf(FirestoreResult.Error(Exception("Remote error")))
        every { featureRequestRepository.getFeatureRequests() } returns flowOf(listOf(localRequest1, localRequest2))

        viewModel.featureRequestList.test {
            awaitItem().shouldBeNull() // initial value

            val actualItems = awaitItem()
            val expectedItems = listOf(
                FeatureRequestDomain("local1", "Local One", "Desc1", FeatureRequestStatus.PENDING, formattedDate1, 0),
                FeatureRequestDomain("local2", "Local Two", "Desc2", FeatureRequestStatus.IN_PROGRESS, formattedDate2, 0)
            )
            actualItems shouldContainExactlyInAnyOrder expectedItems
        }
    }

    @Test
    fun `featureRequestList should be empty if both remote and local are empty`() = runTest {
        every { featureRequestService.getApprovedFeatureRequestsFlow() } returns flowOf(FirestoreResult.Success(emptyList()))
        every { featureRequestRepository.getFeatureRequests() } returns flowOf(emptyList())

        viewModel.featureRequestList.test {
            awaitItem().shouldBeNull() // initial value
            awaitItem()!!.shouldBeEmpty()
        }
    }

    @Test
    fun `featureRequestList should handle remote success but local empty`() = runTest {
         val date = Date()
         val formattedDate = Formatter.formatTimeStamp(date)
        val remoteApprovedRequest = FeatureRequest(id = "remote1", title = "Remote Approved", description = "Desc1", status = FeatureRequestStatus.APPROVED.name, timestamp = date)

        every { featureRequestService.getApprovedFeatureRequestsFlow() } returns flowOf(FirestoreResult.Success(listOf(remoteApprovedRequest)))
        every { featureRequestRepository.getFeatureRequests() } returns flowOf(emptyList())

        viewModel.featureRequestList.test {
            awaitItem().shouldBeNull() // initial value
            val actualItems = awaitItem()
            val expectedItems = listOf(
                FeatureRequestDomain("remote1", "Remote Approved", "Desc1", FeatureRequestStatus.APPROVED, formattedDate, 0)
            )
            actualItems shouldContainExactlyInAnyOrder expectedItems
        }
    }

     @Test
    fun `featureRequestList should handle local results when remote returns success but empty list`() = runTest {
        val date = Date()
        val formattedDate = Formatter.formatTimeStamp(date)
        val localRequestEntity = FeatureRequestEntity(id = "local1", title = "Local Pending", description = "Desc2", status = FeatureRequestStatus.PENDING.name, timestamp = date, isShown = true)

        every { featureRequestService.getApprovedFeatureRequestsFlow() } returns flowOf(FirestoreResult.Success(emptyList()))
        every { featureRequestRepository.getFeatureRequests() } returns flowOf(listOf(localRequestEntity))

        viewModel.featureRequestList.test {
            awaitItem().shouldBeNull() // initial value
            val items = awaitItem()
             val expectedItems = listOf(
                FeatureRequestDomain("local1", "Local Pending", "Desc2", FeatureRequestStatus.PENDING, formattedDate, 0)
            )
            items shouldContainExactlyInAnyOrder expectedItems
        }
    }
}

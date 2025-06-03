package com.erdees.foodcostcalc.domain.usecase

import com.erdees.foodcostcalc.data.model.remote.FeatureRequest
import com.erdees.foodcostcalc.data.model.remote.FeatureRequestStatus
import com.erdees.foodcostcalc.data.model.remote.FirestoreResult
import com.erdees.foodcostcalc.data.remote.FeatureRequestService
import com.erdees.foodcostcalc.data.repository.FeatureRequestRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toEntity
import com.erdees.foodcostcalc.utils.MyDispatchers
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

@ExperimentalCoroutinesApi
class SubmitFeatureRequestUseCaseTest {

    private lateinit var useCase: SubmitFeatureRequestUseCase
    private lateinit var featureRequestService: FeatureRequestService
    private lateinit var featureRequestRepository: FeatureRequestRepository
    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = MyDispatchers(testDispatcher, testDispatcher, testDispatcher)


    @Before
    fun setUp() {
        featureRequestService = mockk()
        featureRequestRepository = mockk(relaxUnitFun = true) // relaxUnitFun for insert
        useCase = SubmitFeatureRequestUseCase(featureRequestService, featureRequestRepository, dispatchers)
    }

    @Test
    fun `invoke should return success when service and repository succeed`() = runTest {
        val title = "Test Title"
        val description = "Test Description"
        val featureRequestSlot = slot<FeatureRequest>()
        val documentId = "docId123"

        coEvery { featureRequestService.submitFeatureRequest(capture(featureRequestSlot)) } returns FirestoreResult.Success(documentId)

        val result = useCase.invoke(title, description)

        result.shouldBeSuccess()
        featureRequestSlot.captured.title shouldBe title
        featureRequestSlot.captured.description shouldBe description
        featureRequestSlot.captured.status shouldBe FeatureRequestStatus.PENDING.name

        coVerify { featureRequestRepository.insertFeatureRequest(featureRequestSlot.captured.toEntity(documentId, any<Date>())) }
    }

    @Test
    fun `invoke should return failure when service returns error`() = runTest {
        val title = "Test Title"
        val description = "Test Description"
        val exception = RuntimeException("Service error")

        coEvery { featureRequestService.submitFeatureRequest(any()) } returns FirestoreResult.Error(exception)

        val result = useCase.invoke(title, description)

        result.shouldBeFailure { it shouldBe exception }
        coVerify(exactly = 0) { featureRequestRepository.insertFeatureRequest(any()) }
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        val title = "Test Title"
        val description = "Test Description"
        val exception = RuntimeException("Repository error")
        val documentId = "docId123"

        coEvery { featureRequestService.submitFeatureRequest(any()) } returns FirestoreResult.Success(documentId)
        coEvery { featureRequestRepository.insertFeatureRequest(any()) } throws exception

        val result = useCase.invoke(title, description)

        result.shouldBeFailure { it shouldBe exception }
    }

    @Test
    fun `invoke should return failure for any other unexpected exception`() = runTest {
        val title = "Test Title"
        val description = "Test Description"
        val exception = RuntimeException("Unexpected error")

        coEvery { featureRequestService.submitFeatureRequest(any()) } throws exception

        val result = useCase.invoke(title, description)

        result.shouldBeFailure { it shouldBe exception }
        coVerify(exactly = 0) { featureRequestRepository.insertFeatureRequest(any()) }
    }
}

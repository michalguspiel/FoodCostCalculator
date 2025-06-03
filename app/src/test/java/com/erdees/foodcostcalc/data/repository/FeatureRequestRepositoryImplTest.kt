package com.erdees.foodcostcalc.data.repository

import app.cash.turbine.test
import com.erdees.foodcostcalc.data.db.dao.featurerequest.FeatureRequestDao
import com.erdees.foodcostcalc.data.model.local.FeatureRequestEntity
import com.erdees.foodcostcalc.data.model.local.UpvotedFeatureRequest
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.Date

@ExperimentalCoroutinesApi
class FeatureRequestRepositoryImplTest : KoinTest {

    private val featureRequestDao: FeatureRequestDao = mockk()
    private val repository: FeatureRequestRepository by inject()

    @Before
    fun setUp() {
        startKoin {
            modules(module {
                single { featureRequestDao }
                single<FeatureRequestRepository> { FeatureRequestRepositoryImpl() }
            })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `getFeatureRequests should call DAO and return its flow`() = runTest {
        val mockFlow = flowOf(listOf(FeatureRequestEntity("id1", "title", "desc", "PENDING", Date(), false)))
        every { featureRequestDao.getFeatureRequests() } returns mockFlow

        val resultFlow = repository.getFeatureRequests()

        resultFlow.test {
            awaitItem() shouldBe mockFlow.expectMostRecentItem()
            awaitComplete()
        }
        verify { featureRequestDao.getFeatureRequests() }
    }

    @Test
    fun `getUpvotedFeatureRequests should call DAO and return its flow`() = runTest {
        val mockFlow = flowOf(listOf(UpvotedFeatureRequest("id1")))
        every { featureRequestDao.getUpvotedFeatureRequests() } returns mockFlow

        val resultFlow = repository.getUpvotedFeatureRequests()

        resultFlow.test {
            awaitItem() shouldBe mockFlow.expectMostRecentItem()
            awaitComplete()
        }
        verify { featureRequestDao.getUpvotedFeatureRequests() }
    }

    @Test
    fun `insertFeatureRequest should call DAO's insert method`() = runTest {
        val entity = FeatureRequestEntity("id1", "title", "desc", "PENDING", Date(), false)
        coEvery { featureRequestDao.insertFeatureRequest(any()) } returns Unit // DAO insert is suspend

        repository.insertFeatureRequest(entity)

        coVerify { featureRequestDao.insertFeatureRequest(entity) }
    }

    @Test
    fun `upvoteFeatureRequest should call DAO's upvote method`() = runTest {
        val requestId = "id1"
        coEvery { featureRequestDao.upvoteFeatureRequest(any()) } returns Unit // DAO upvote is suspend

        repository.upvoteFeatureRequest(requestId)

        coVerify { featureRequestDao.upvoteFeatureRequest(UpvotedFeatureRequest(requestId)) }
    }

    @Test
    fun `removeUpvote should call DAO's removeUpvote method`() = runTest {
        val requestId = "id1"
        coEvery { featureRequestDao.removeUpvote(any()) } returns Unit // DAO removeUpvote is suspend

        repository.removeUpvote(requestId)

        coVerify { featureRequestDao.removeUpvote(requestId) }
    }
}

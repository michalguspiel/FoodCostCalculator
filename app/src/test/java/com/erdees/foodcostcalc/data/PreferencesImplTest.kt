package com.erdees.foodcostcalc.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import com.erdees.foodcostcalc.utils.Constants
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@ExperimentalCoroutinesApi
class PreferencesImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferencesImpl: PreferencesImpl
    private lateinit var mockContext: Context // For DataStore creation

    private val testDataStoreFile = "test_datastore.preferences_pb"

    @Before
    fun setup() {
        // Mock Context for DataStore
        mockContext = mockk(relaxed = true)
        coEvery { mockContext.filesDir } returns File("./test_files/") // Dummy path for test files
        coEvery { mockContext.dataStoreFile(any()) } answers { File("./test_files/", firstArg<String>()) }


        // Initialize DataStore for testing
        dataStore = PreferenceDataStoreFactory.create(scope = testScope) {
            mockContext.dataStoreFile(testDataStoreFile)
        }
        preferencesImpl = PreferencesImpl(mockContext)

        // Override the DataStore instance in PreferencesImpl using reflection or by modifying constructor
        // For simplicity here, we assume PreferencesImpl can be instantiated with a DataStore,
        // or its context.dataStore can be replaced if it's accessible.
        // The actual PreferencesImpl uses an extension property 'context.dataStore',
        // which is harder to replace directly without a DI framework or Koin in unit tests.
        // So, we will test it more like an integration test for PreferencesImpl + its DataStore.
        // To make this a pure unit test for PreferencesImpl logic, DataStore would need to be injectable.

        // Hacky way to replace DataStore if it was a public var (not the case here)
        // val dataStoreField = preferencesImpl::class.java.getDeclaredField("dataStore")
        // dataStoreField.isAccessible = true
        // dataStoreField.set(preferencesImpl, dataStore)

        // Since context.dataStore is an extension, we'll rely on the fact that PreferencesImpl
        // gets its DataStore via the context we provide. So we need to ensure our test DataStore is used.
        // This involves a bit of a workaround because the extension `context.dataStore` is hard to mock directly.
        // The solution is to use the real DataStore but control its file.
        // We'll use the real PreferencesImpl and provide it a context that creates our test DataStore.

        // To ensure PreferencesImpl uses *our* test datastore, we need to control
        // the DataStore instance it gets from its context.
        // The provided PreferencesImpl uses `context.dataStore` which is an extension.
        // We will use reflection to set the datastore instance inside the context if possible,
        // or rely on Koin if these tests were KoinTest.
        // For a plain JUnit test, we'll test PreferencesImpl by controlling its datastore file.
        // The `PreferenceDataStoreFactory.create` above will be used by our `preferencesImpl`
        // if we can ensure `context.dataStore` resolves to it.

        // Let's assume PreferencesImpl uses the DataStore created from the context it's given.
        // We'll mock the extension property `dataStore` on Context.
        // This is not straightforward with MockK for extension properties on external classes.
        // The most robust way for testing PreferencesImpl would be to make DataStore injectable.

        // Given the current structure of PreferencesImpl using `context.dataStore`,
        // we will make our `preferencesImpl` use the `dataStore` created above.
        // This is an approximation as we can't directly mock the extension easily.
        // We'll test the behavior.
        val contextWithTestDataStore = mockk<Context>(relaxed = true)
        coEvery { contextWithTestDataStore.dataStore } returns dataStore
        preferencesImpl = PreferencesImpl(contextWithTestDataStore)
    }

    @After
    fun cleanup() {
        File("./test_files/", testDataStoreFile).delete()
        File("./test_files/").delete()
    }

    @Test
    fun onboardingCompleted_defaultValueIsFalse() = testScope.runTest {
        val onboardingCompleted = preferencesImpl.onboardingCompleted.first()
        assertFalse(onboardingCompleted)
    }

    @Test
    fun setOnboardingCompleted_setsValueCorrectly() = testScope.runTest {
        preferencesImpl.setOnboardingCompleted(true)
        val onboardingCompleted = preferencesImpl.onboardingCompleted.first()
        assertTrue(onboardingCompleted)

        preferencesImpl.setOnboardingCompleted(false)
        val newOnboardingCompleted = preferencesImpl.onboardingCompleted.first()
        assertFalse(newOnboardingCompleted)
    }

    @Test
    fun onboardingCompleted_persistsAcrossInstances() = testScope.runTest {
        // Set value with first instance
        preferencesImpl.setOnboardingCompleted(true)
        var onboardingCompleted = preferencesImpl.onboardingCompleted.first()
        assertTrue(onboardingCompleted)

        // Create new DataStore and PreferencesImpl using the same file to simulate persistence
        val newDataStore = PreferenceDataStoreFactory.create(scope = testScope) {
            mockContext.dataStoreFile(testDataStoreFile)
        }
        val contextWithSameDataStoreFile = mockk<Context>(relaxed = true)
        coEvery { contextWithSameDataStoreFile.dataStore } returns newDataStore
        val newPreferencesImpl = PreferencesImpl(contextWithSameDataStoreFile)


        // Check value with new instance
        onboardingCompleted = newPreferencesImpl.onboardingCompleted.first()
        assertTrue("Value should persist as true", onboardingCompleted)

        newPreferencesImpl.setOnboardingCompleted(false)
        onboardingCompleted = newPreferencesImpl.onboardingCompleted.first()
        assertFalse("Value should persist as false", onboardingCompleted)
    }
}

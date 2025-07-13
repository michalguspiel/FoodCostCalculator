package com.erdees.foodcostcalc.utils

import io.kotest.matchers.shouldBe
import org.junit.Test

class UnsavedChangesValidatorTest {

    @Test
    fun `hasUnsavedChanges should return false when both objects are null`() {
        // Given
        val original: TestData? = null
        val current: TestData? = null

        // When
        val result = UnsavedChangesValidator.hasUnsavedChanges(original, current)

        // Then
        result shouldBe false
    }

    @Test
    fun `hasUnsavedChanges should return true when original is null but current is not`() {
        // Given
        val original: TestData? = null
        val current = TestData("Test", 42)

        // When
        val result = UnsavedChangesValidator.hasUnsavedChanges(original, current)

        // Then
        result shouldBe true
    }

    @Test
    fun `hasUnsavedChanges should return true when current is null but original is not`() {
        // Given
        val original = TestData("Test", 42)
        val current: TestData? = null

        // When
        val result = UnsavedChangesValidator.hasUnsavedChanges(original, current)

        // Then
        result shouldBe true
    }

    @Test
    fun `hasUnsavedChanges should return false when objects are equal`() {
        // Given
        val original = TestData("Test", 42)
        val current = TestData("Test", 42)

        // When
        val result = UnsavedChangesValidator.hasUnsavedChanges(original, current)

        // Then
        result shouldBe false
    }

    @Test
    fun `hasUnsavedChanges should return true when objects are different`() {
        // Given
        val original = TestData("Test", 42)
        val current = TestData("Changed", 42)

        // When
        val result = UnsavedChangesValidator.hasUnsavedChanges(original, current)

        // Then
        result shouldBe true
    }

    @Test
    fun `hasListChanges should return false when both lists are null`() {
        // Given
        val originalList: List<TestData>? = null
        val currentList: List<TestData>? = null

        // When
        val result = UnsavedChangesValidator.hasListChanges(originalList, currentList)

        // Then
        result shouldBe false
    }

    @Test
    fun `hasListChanges should return true when original list is null but current is not`() {
        // Given
        val originalList: List<TestData>? = null
        val currentList = listOf(TestData("Test", 42))

        // When
        val result = UnsavedChangesValidator.hasListChanges(originalList, currentList)

        // Then
        result shouldBe true
    }

    @Test
    fun `hasListChanges should return true when current list is null but original is not`() {
        // Given
        val originalList = listOf(TestData("Test", 42))
        val currentList: List<TestData>? = null

        // When
        val result = UnsavedChangesValidator.hasListChanges(originalList, currentList)

        // Then
        result shouldBe true
    }

    @Test
    fun `hasListChanges should return false when lists contain the same elements`() {
        // Given
        val originalList = listOf(TestData("Test1", 42), TestData("Test2", 24))
        val currentList = listOf(TestData("Test1", 42), TestData("Test2", 24))

        // When
        val result = UnsavedChangesValidator.hasListChanges(originalList, currentList)

        // Then
        result shouldBe false
    }

    @Test
    fun `hasListChanges should return true when lists have different elements`() {
        // Given
        val originalList = listOf(TestData("Test1", 42), TestData("Test2", 24))
        val currentList = listOf(TestData("Test1", 42), TestData("Changed", 24))

        // When
        val result = UnsavedChangesValidator.hasListChanges(originalList, currentList)

        // Then
        result shouldBe true
    }

    @Test
    fun `hasListChanges should return true when lists have different sizes`() {
        // Given
        val originalList = listOf(TestData("Test1", 42), TestData("Test2", 24))
        val currentList = listOf(TestData("Test1", 42), TestData("Test2", 24), TestData("Test3", 10))

        // When
        val result = UnsavedChangesValidator.hasListChanges(originalList, currentList)

        // Then
        result shouldBe true
    }

    @Test
    fun `hasListChanges should return true when lists have same elements in different order`() {
        // Given
        val originalList = listOf(TestData("Test1", 42), TestData("Test2", 24))
        val currentList = listOf(TestData("Test2", 24), TestData("Test1", 42))

        // When
        val result = UnsavedChangesValidator.hasListChanges(originalList, currentList)

        // Then
        // Lists are compared with equals which checks both elements and order
        result shouldBe true
    }

    // Simple data class for testing
    data class TestData(val name: String, val value: Int)
}

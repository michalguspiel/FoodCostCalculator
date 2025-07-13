package com.erdees.foodcostcalc.utils

/**
 * A utility class for detecting unsaved changes across different data models.
 * This helps identify whether the user has made modifications to data that haven't been saved yet.
 */
object UnsavedChangesValidator {

    /**
     * Generic function to compare two objects of the same type to determine if changes have been made.
     *
     * @param original The original state of the object
     * @param current The current state of the object that may have changes
     * @return true if changes are detected, false otherwise
     */
    fun <T> hasUnsavedChanges(original: T?, current: T?): Boolean {
        // If both are null, no changes
        if (original == null && current == null) return false

        // If one is null and the other isn't, there are changes
        if (original == null || current == null) return true

        // Otherwise compare the objects
        return original != current
    }

    /**
     * Compares lists to determine if there have been additions, removals or modifications.
     *
     * @param originalList The original list
     * @param currentList The current list that may have changes
     * @return true if changes are detected, false otherwise
     */
    fun <T> hasListChanges(originalList: List<T>?, currentList: List<T>?): Boolean {
        return when {
            originalList == null && currentList == null -> false
            originalList == null || currentList == null -> true
            originalList.size != currentList.size -> true
            else -> originalList != currentList
        }
    }
}

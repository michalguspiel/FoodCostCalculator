package com.erdees.foodcostcalc.domain.model

/**
 * Represents a [ItemUsageEntry] that has been persisted and has an identity.
 */
interface UsedItem : ItemUsageEntry {
    val id: Long
    val ownerId: Long
}
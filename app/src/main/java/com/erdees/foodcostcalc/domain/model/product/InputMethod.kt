package com.erdees.foodcostcalc.domain.model.product

/**
 * Enum representing the method used to input product pricing information
 */
enum class InputMethod {
    /** Product was entered with unit-based pricing (price per unit) */
    UNIT,

    /** Product was entered with package-based pricing (price per package) */
    PACKAGE;

    companion object {
        /**
         * Safely converts a string to InputMethod with fallback
         * @param value The string value to convert
         * @param defaultValue The default value to use if conversion fails
         * @return The corresponding InputMethod or defaultValue if not found
         */
        fun fromStringOrDefault(value: String?, defaultValue: InputMethod = UNIT): InputMethod {
            return if (value == null) {
                defaultValue
            } else {
                try {
                    valueOf(value.uppercase())
                } catch (_: IllegalArgumentException) {
                    defaultValue
                }
            }
        }
    }
}

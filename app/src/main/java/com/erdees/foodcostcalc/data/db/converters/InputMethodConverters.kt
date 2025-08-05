package com.erdees.foodcostcalc.data.db.converters

import androidx.room.TypeConverter
import com.erdees.foodcostcalc.domain.model.product.InputMethod

/**
 * Room type converters for InputMethod enum
 * Handles safe serialization/deserialization with fallback for unknown values
 */
class InputMethodConverters {

    @TypeConverter
    fun fromInputMethod(inputMethod: InputMethod): String {
        return inputMethod.name
    }

    @TypeConverter
    fun toInputMethod(inputMethodString: String): InputMethod {
        return InputMethod.fromStringOrDefault(inputMethodString, InputMethod.UNIT)
    }
}
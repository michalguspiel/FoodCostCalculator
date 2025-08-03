package com.erdees.foodcostcalc.data.db.converters

import androidx.room.TypeConverter
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

/**
 * Room type converters for MeasurementUnit enum
 * Handles safe serialization/deserialization with fallback for unknown values
 */
class UnitConverters {
    
    @TypeConverter
    fun fromMeasurementUnit(unit: MeasurementUnit): String {
        return unit.name
    }
    
    @TypeConverter
    fun toMeasurementUnit(unitString: String): MeasurementUnit {
        return MeasurementUnit.fromStringOrDefault(unitString, MeasurementUnit.GRAM)
    }
}
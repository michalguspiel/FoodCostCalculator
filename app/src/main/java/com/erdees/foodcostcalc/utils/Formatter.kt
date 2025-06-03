package com.erdees.foodcostcalc.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date

object Formatter {
    fun formatTimeStamp(date: Date): String {
        val localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        return localDateTime.format(formatter)
    }
}

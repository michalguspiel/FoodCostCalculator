package com.erdees.foodcostcalc.utils

import kotlinx.coroutines.flow.MutableStateFlow

fun onNumericValueChange(newValue: String, stateFlow: MutableStateFlow<String>) {
  if (newValue.isEmpty()) {
    stateFlow.value = newValue
  } else {
    stateFlow.value = when (newValue.toDoubleOrNull()) {
      null -> stateFlow.value //old value
      else -> newValue   //new value
    }
  }
}

fun onNumericValueChange(oldValue: String, newValue: String): String {
  return if (newValue.isEmpty()) newValue
  else when (newValue.toDoubleOrNull()) {
    null -> oldValue
    else -> newValue
  }
}

fun onIntegerValueChange(oldValue: String, newValue: String): String {
  return if (newValue.isEmpty()) newValue
  else when (newValue.toIntOrNull()) {
    null -> oldValue
    else -> newValue
  }
}

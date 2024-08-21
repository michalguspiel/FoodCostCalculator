package com.erdees.foodcostcalc.domain.model.dish

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DishDomainNavType : NavType<DishDomain>(isNullableAllowed = false) {
  override fun get(bundle: Bundle, key: String): DishDomain? {
    return Json.decodeFromString(bundle.getString(key) ?: return null)
  }

  override fun parseValue(value: String): DishDomain {
    return Json.decodeFromString(Uri.decode(value))
  }

  override fun serializeAsValue(value: DishDomain): String {
    return Uri.encode(Json.encodeToString(value))
  }

  override fun put(bundle: Bundle, key: String, value: DishDomain) {
    bundle.putString(key, Json.encodeToString(value))
  }
}

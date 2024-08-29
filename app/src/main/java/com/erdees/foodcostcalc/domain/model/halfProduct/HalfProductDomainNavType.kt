package com.erdees.foodcostcalc.domain.model.halfProduct

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object HalfProductDomainNavType : NavType<HalfProductDomain>(isNullableAllowed = false) {
  override fun get(bundle: Bundle, key: String): HalfProductDomain? {
    return Json.decodeFromString(bundle.getString(key) ?: return null)
  }

  override fun parseValue(value: String): HalfProductDomain {
    return Json.decodeFromString(Uri.decode(value))
  }

  override fun serializeAsValue(value: HalfProductDomain): String {
    return Uri.encode(Json.encodeToString(value))
  }

  override fun put(bundle: Bundle, key: String, value: HalfProductDomain) {
    bundle.putString(key, Json.encodeToString(value))
  }
}

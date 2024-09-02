package com.erdees.foodcostcalc.domain.model.product

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ProductDomainNavType : NavType<ProductDomain>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): ProductDomain? {
        return Json.decodeFromString(bundle.getString(key) ?: return null)
    }

    override fun parseValue(value: String): ProductDomain {
        return Json.decodeFromString(Uri.decode(value))
    }

    override fun serializeAsValue(value: ProductDomain): String {
        return Uri.encode(Json.encodeToString(value))
    }

    override fun put(bundle: Bundle, key: String, value: ProductDomain) {
        bundle.putString(key, Json.encodeToString(value))
    }
}

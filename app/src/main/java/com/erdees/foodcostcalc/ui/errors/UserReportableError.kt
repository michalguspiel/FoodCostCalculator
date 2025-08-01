package com.erdees.foodcostcalc.ui.errors

import androidx.annotation.StringRes
import com.erdees.foodcostcalc.R

interface UserReportableError {
    @get:StringRes
    val errorRes: Int

    val message: String?
}

class InvalidMarginFormatException(
    message: String,
    @StringRes override val errorRes: Int = R.string.invalid_margin_format_error
) : IllegalArgumentException(message), UserReportableError

class InvalidTaxFormatException(
    message: String,
    @StringRes override val errorRes: Int = R.string.invalid_tax_format_error
) : IllegalArgumentException(message), UserReportableError

class InvalidProductPriceException(
    message: String,
    @StringRes override val errorRes: Int = R.string.invalid_product_price_error
) : IllegalArgumentException(message), UserReportableError

class FailedToAddComponent(
    @StringRes override val errorRes: Int = R.string.error_failed_to_add_component
) : IllegalStateException(), UserReportableError
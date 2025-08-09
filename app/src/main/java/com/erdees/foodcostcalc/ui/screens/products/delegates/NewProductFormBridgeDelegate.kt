package com.erdees.foodcostcalc.ui.screens.products.delegates

import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormData
import com.erdees.foodcostcalc.ui.screens.products.EditableProductUiState
import com.erdees.foodcostcalc.ui.screens.products.PackagePriceState
import com.erdees.foodcostcalc.ui.screens.products.UnitPriceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Atomic state containers for each logical grouping
 */
private data class ProductFormState(
    val inputMethod: InputMethod,
    val name: String,
    val tax: String,
    val waste: String
)

private data class PackagePricingState(
    val packagePrice: String,
    val packageQuantity: String,
    val packageUnit: MeasurementUnit,
    val canonicalPriceAndUnit: Pair<Double?, MeasurementUnit?>
)

private data class UnitPricingState(
    val unitPrice: String,
    val unitPriceUnit: MeasurementUnit
)

/**
 * Final combined state container
 */
private data class DelegateStateSnapshot(
    val inputMethod: InputMethod,
    val name: String,
    val packagePrice: String,
    val packageQuantity: String,
    val packageUnit: MeasurementUnit,
    val unitPrice: String,
    val unitPriceUnit: MeasurementUnit,
    val canonicalPriceAndUnit: Pair<Double?, MeasurementUnit?>
)

/**
 * Bridge delegate that allows NewProductFormViewModel to use the common delegates
 * while maintaining its existing NewProductFormData state structure.
 */
class NewProductFormBridgeDelegate(
    private val productFormDelegate: ProductFormDelegate,
    private val packagePricingDelegate: PackagePricingDelegate,
    private val unitPricingDelegate: UnitPricingDelegate,
    private val scope: CoroutineScope
) {

    private val productFormState: StateFlow<ProductFormState> = combine(
        productFormDelegate.inputMethod,
        productFormDelegate.name,
        productFormDelegate.tax,
        productFormDelegate.waste
    ) { inputMethod, name, tax, waste ->
        ProductFormState(inputMethod, name, tax, waste)
    }.stateIn(scope, SharingStarted.Eagerly,
        ProductFormState(
            inputMethod = InputMethod.PACKAGE,
            name = "",
            tax = "",
            waste = ""
        )
    )

    private val packagePricingState: StateFlow<PackagePricingState> = combine(
        packagePricingDelegate.packagePrice,
        packagePricingDelegate.packageQuantity,
        packagePricingDelegate.packageUnit,
        packagePricingDelegate.canonicalPriceAndUnit
    ) { price, quantity, unit, canonical ->
        PackagePricingState(price, quantity, unit, canonical)
    }.stateIn(scope, SharingStarted.Eagerly,
        PackagePricingState(
            packagePrice = "",
            packageQuantity = "",
            packageUnit = MeasurementUnit.KILOGRAM,
            canonicalPriceAndUnit = null to null
        )
    )

    private val unitPricingState: StateFlow<UnitPricingState> = combine(
        unitPricingDelegate.unitPrice,
        unitPricingDelegate.unitPriceUnit
    ) { price, unit ->
        UnitPricingState(price, unit)
    }.stateIn(scope, SharingStarted.Eagerly,
        UnitPricingState(
            unitPrice = "",
            unitPriceUnit = MeasurementUnit.KILOGRAM
        )
    )

    private val delegateSnapshot: StateFlow<DelegateStateSnapshot> = combine(
        productFormState,
        packagePricingState,
        unitPricingState
    ) { productForm, packagePricing, unitPricing ->
        DelegateStateSnapshot(
            inputMethod = productForm.inputMethod,
            name = productForm.name,
            packagePrice = packagePricing.packagePrice,
            packageQuantity = packagePricing.packageQuantity,
            packageUnit = packagePricing.packageUnit,
            unitPrice = unitPricing.unitPrice,
            unitPriceUnit = unitPricing.unitPriceUnit,
            canonicalPriceAndUnit = packagePricing.canonicalPriceAndUnit
        )
    }.stateIn(scope, SharingStarted.Eagerly,
        DelegateStateSnapshot(
            inputMethod = InputMethod.PACKAGE,
            name = "",
            packagePrice = "",
            packageQuantity = "",
            packageUnit = MeasurementUnit.KILOGRAM,
            unitPrice = "",
            unitPriceUnit = MeasurementUnit.KILOGRAM,
            canonicalPriceAndUnit = null to null
        )
    )

    /**
     * Converts delegate states to NewProductFormData
     */
    fun createFormDataState(
        quantityAddedToDish: String,
        quantityAddedToDishUnit: MeasurementUnit?
    ): StateFlow<NewProductFormData> {
        return delegateSnapshot.map { snapshot ->
            snapshot.toNewProductFormData(quantityAddedToDish, quantityAddedToDishUnit)
        }.stateIn(scope, SharingStarted.Eagerly, NewProductFormData())
    }

    /**
     * Syncs a NewProductFormData update back to the delegates
     */
    fun syncFromFormData(formData: NewProductFormData) {
        // Update waste field
        if (productFormDelegate.waste.value != formData.wastePercent) {
            productFormDelegate.updateWaste(formData.wastePercent)
        }
        if (productFormDelegate.inputMethod.value != formData.inputMethod) {
            productFormDelegate.setInputMethod(formData.inputMethod)
        }

        // Sync based on input method
        when (formData.inputMethod) {
            InputMethod.PACKAGE -> syncPackageFields(formData)
            InputMethod.UNIT -> syncUnitFields(formData)
        }
    }

    private fun syncPackageFields(formData: NewProductFormData) {
        with(packagePricingDelegate) {
            if (packagePrice.value != formData.packagePrice) {
                updatePackagePrice(formData.packagePrice)
            }
            if (packageQuantity.value != formData.packageQuantity) {
                updatePackageQuantity(formData.packageQuantity)
            }
            formData.packageUnit?.let { unit ->
                if (packageUnit.value != unit) {
                    updatePackageUnit(unit)
                }
            }
        }
    }

    private fun syncUnitFields(formData: NewProductFormData) {
        with(unitPricingDelegate) {
            if (unitPrice.value != formData.unitPrice) {
                updateUnitPrice(formData.unitPrice)
            }
            formData.unitPriceUnit?.let { unit ->
                if (unitPriceUnit.value != unit) {
                    updateUnitPriceUnit(unit)
                }
            }
        }
    }

    /**
     * Creates validation state for NewProductForm
     */
    fun createValidation(): StateFlow<Boolean> = combine(
        productFormDelegate.inputMethod,
        packagePricingDelegate.createValidation(),
        unitPricingDelegate.createValidation()
    ) { inputMethod, packageValid, unitValid ->
        when (inputMethod) {
            InputMethod.PACKAGE -> packageValid
            InputMethod.UNIT -> unitValid
        }
    }.stateIn(scope, SharingStarted.Lazily, false)

    /**
     * Converts current delegate state to EditableProductUiState for compatibility
     */
    fun toEditableProductUiState(): StateFlow<EditableProductUiState> = combine(
        delegateSnapshot,
        productFormDelegate.tax,
        productFormDelegate.waste
    ) { snapshot, tax, waste ->
        snapshot.toEditableProductUiState(tax, waste)
    }.stateIn(scope, SharingStarted.Eagerly, PackagePriceState(name = ""))
}

/**
 * Extension functions for cleaner state conversion
 */
private fun DelegateStateSnapshot.toNewProductFormData(
    quantityAddedToDish: String,
    quantityAddedToDishUnit: MeasurementUnit?
): NewProductFormData {
    return NewProductFormData(
        inputMethod = inputMethod,
        packagePrice = packagePrice,
        packageQuantity = packageQuantity,
        packageUnit = packageUnit,
        unitPrice = unitPrice,
        unitPriceUnit = unitPriceUnit,
        wastePercent = "", // This will be filled by the ViewModel
        quantityAddedToDish = quantityAddedToDish,
        quantityAddedToDishUnit = quantityAddedToDishUnit
    )
}

private fun DelegateStateSnapshot.toEditableProductUiState(
    tax: String,
    waste: String
): EditableProductUiState {
    return when (inputMethod) {
        InputMethod.PACKAGE -> PackagePriceState(
            name = name, // Now using name directly from snapshot
            tax = tax,
            waste = waste,
            packagePrice = packagePrice,
            packageQuantity = packageQuantity,
            packageUnit = packageUnit,
            canonicalPrice = canonicalPriceAndUnit.first,
            canonicalUnit = canonicalPriceAndUnit.second
        )
        InputMethod.UNIT -> UnitPriceState(
            name = name, // Now using name directly from snapshot
            tax = tax,
            waste = waste,
            unitPrice = unitPrice,
            unitPriceUnit = unitPriceUnit
        )
    }
}

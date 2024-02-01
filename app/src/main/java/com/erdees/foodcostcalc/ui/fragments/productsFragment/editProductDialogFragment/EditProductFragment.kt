package com.erdees.foodcostcalc.ui.fragments.productsFragment.editProductDialogFragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.FragmentEditProductBinding
import com.erdees.foodcostcalc.entities.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.entities.ProductIncluded
import com.erdees.foodcostcalc.entities.Product
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.UnitsUtils.filterVol
import com.erdees.foodcostcalc.utils.UnitsUtils.filterWeight
import com.erdees.foodcostcalc.utils.Utils.getUnits
import com.erdees.foodcostcalc.utils.ViewUtils.hideKeyboard

class EditProductFragment : DialogFragment(), AdapterView.OnItemClickListener {

  private var _binding: FragmentEditProductBinding? = null
  private val binding get() = _binding!!

  private var unitPosition: Int? = null
  private var productId: Long? = null
  private var unitList: MutableList<String> = mutableListOf()
  private val spinnerId = 1
  private lateinit var fragmentViewModel: EditProductFragmentViewModel
  private lateinit var sharedPreferences: SharedPreferences

  override fun onResume() {
    unitList = getUnits(resources, sharedPreferences)
    unitList = when (productPassedFromAdapter.unit) {
      "per piece" -> mutableListOf("per piece")
      "per kilogram" -> unitList.filterWeight()
      "per liter" -> unitList.filterVol()
      "per pound" -> unitList.filterWeight()
      "per gallon" -> unitList.filterVol()
      else -> mutableListOf("error!")
    }

    val unitsAdapter = ArrayAdapter(
      requireActivity(), R.layout.dropdown_item, unitList
    )
    with(binding.spinnerEditProduct) {
      setAdapter(unitsAdapter)
      onItemClickListener = this@EditProductFragment
      gravity = Gravity.CENTER
      id = spinnerId
    }
    super.onResume()
  }

  @SuppressLint("ResourceType")
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = FragmentEditProductBinding.inflate(inflater, container, false)
    val view = binding.root
    fragmentViewModel = ViewModelProvider(this).get(EditProductFragmentViewModel::class.java)
    sharedPreferences = SharedPreferences(requireContext())

    binding.spinnerEditProduct.setOnFocusChangeListener { _, hasFocus ->
      if (hasFocus) view.hideKeyboard()
    }

    /** empty lists which gets populated by every 'ProductIncluded' and
     * 'ProductIncludedInHalfProductModel' that has the same ID as edited product. */
    var productIncludedList = listOf<ProductIncluded>()
    var productIncludedInHalfProductList = listOf<ProductIncludedInHalfProduct>()
    setProductIdOfEditedProduct(productPassedFromAdapter)
    setDialogFieldsAccordinglyToProductEdited(productPassedFromAdapter)
    if (productId != null) {
      fragmentViewModel.getCertainProductsIncluded(productId!!).observe(
        viewLifecycleOwner
      ) { listOfProducts ->
        productIncludedList = listOfProducts
      }
      fragmentViewModel.getCertainProductsIncludedInHalfProduct(productId!!).observe(
          viewLifecycleOwner
        ) { listOfProducts ->
          productIncludedInHalfProductList = listOfProducts
        }
    }

    /** BUTTON LOGIC*/
    binding.saveChangesButton.setOnClickListener {
      if (allFieldsAreLegit()) {
        if (unitPosition == null) unitPosition =
          unitList.indexOf(productPassedFromAdapter.unit)
        val unit = unitList.getOrNull(unitPosition ?: 0) ?: productPassedFromAdapter.unit
        val productToChange = Product(
          productId!!,
          binding.editProductName.text.toString(),
          binding.editProductPrice.text.toString().toDouble(),
          binding.editProductTax.text.toString().toDouble(),
          binding.editProductWaste.text.toString().toDouble(),
          unit
        )
        fragmentViewModel.editProduct(productToChange)
        changeEveryProductIncluded(productToChange, productIncludedList)
        changeEveryProductIncludedInHalfProduct(
          productToChange, productIncludedInHalfProductList
        )
        this.dismiss()
      } else Toast.makeText(requireContext(), "Fields must not be empty!", Toast.LENGTH_SHORT)
        .show()
    }

    binding.deleteProductButton.setOnClickListener {
      fragmentViewModel.deleteProduct(productPassedFromAdapter)
      this.dismiss()
    }

    dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    return view
  }

  companion object {
    const val TAG = "EditProductFragment"
    lateinit var productPassedFromAdapter: Product
  }

  private fun allFieldsAreLegit(): Boolean {
    return (!binding.editProductName.text.isNullOrBlank() && !binding.editProductPrice.text.isNullOrBlank() && binding.editProductPrice.text.toString() != "." && !binding.editProductTax.text.isNullOrBlank() && binding.editProductTax.text.toString() != "." && !binding.editProductWaste.text.isNullOrBlank() && binding.editProductWaste.text.toString() != ".")
  }

  private fun changeEveryProductIncluded(
    productToChange: Product, listOfProductsToChange: List<ProductIncluded>
  ) {
    listOfProductsToChange.forEach {
      fragmentViewModel.editProductsIncluded(
        ProductIncluded(
          it.productIncludedId,
          productToChange,
          it.dishOwnerId,
          it.dish,
          it.productOwnerId,
          it.weight,
          it.weightUnit
        )
      )
    }
  }

  private fun changeEveryProductIncludedInHalfProduct(
    productToChange: Product, listToChange: List<ProductIncludedInHalfProduct>
  ) {
    listToChange.forEach {
      fragmentViewModel.editProductIncludedInHalfProduct(
        ProductIncludedInHalfProduct(
          it.productIncludedInHalfProductId,
          productToChange,
          it.halfProduct,
          it.halfProductHostId,
          it.weight,
          it.weightUnit,
          it.weightOfPiece
        )
      )
    }
  }

  private fun setProductIdOfEditedProduct(productEdited: Product) {
    productId = productEdited.productId
  }

  private fun setDialogFieldsAccordinglyToProductEdited(productPassedFromAdapter: Product) {
    binding.editProductName.setText(productPassedFromAdapter.name)
    binding.editProductPrice.setText(productPassedFromAdapter.pricePerUnit.toString())
    binding.editProductTax.setText(productPassedFromAdapter.tax.toString())
    binding.editProductWaste.setText(productPassedFromAdapter.waste.toString())
    binding.spinnerEditProduct.setText(productPassedFromAdapter.unit, false)
  }

  override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    unitPosition = when (parent?.id) {
      1 -> position
      else -> position
    }
  }
}

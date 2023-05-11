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
import com.erdees.foodcostcalc.domain.model.halfProduct.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.domain.model.product.ProductIncluded
import com.erdees.foodcostcalc.domain.model.product.ProductModel
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
    unitList = when (productModelPassedFromAdapter.unit) {
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
    setProductIdOfEditedProduct(productModelPassedFromAdapter)
    setDialogFieldsAccordinglyToProductEdited(productModelPassedFromAdapter)
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
          unitList.indexOf(productModelPassedFromAdapter.unit)
        val productToChange = ProductModel(
          productId!!,
          binding.editProductName.text.toString(),
          binding.editProductPrice.text.toString().toDouble(),
          binding.editProductTax.text.toString().toDouble(),
          binding.editProductWaste.text.toString().toDouble(),
          unitList[unitPosition!!]
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
      fragmentViewModel.deleteProduct(productModelPassedFromAdapter)
      this.dismiss()
    }

    dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    return view
  }

  companion object {
    const val TAG = "EditProductFragment"
    lateinit var productModelPassedFromAdapter: ProductModel
  }

  private fun allFieldsAreLegit(): Boolean {
    return (!binding.editProductName.text.isNullOrBlank() && !binding.editProductPrice.text.isNullOrBlank() && binding.editProductPrice.text.toString() != "." && !binding.editProductTax.text.isNullOrBlank() && binding.editProductTax.text.toString() != "." && !binding.editProductWaste.text.isNullOrBlank() && binding.editProductWaste.text.toString() != ".")
  }

  private fun changeEveryProductIncluded(
    productModelToChange: ProductModel, listOfProductsToChange: List<ProductIncluded>
  ) {
    listOfProductsToChange.forEach {
      fragmentViewModel.editProductsIncluded(
        ProductIncluded(
          it.productIncludedId,
          productModelToChange,
          it.dishOwnerId,
          it.dishModel,
          it.productOwnerId,
          it.weight,
          it.weightUnit
        )
      )
    }
  }

  private fun changeEveryProductIncludedInHalfProduct(
    productModelToChange: ProductModel, listToChange: List<ProductIncludedInHalfProduct>
  ) {
    listToChange.forEach {
      fragmentViewModel.editProductIncludedInHalfProduct(
        ProductIncludedInHalfProduct(
          it.productIncludedInHalfProductId,
          productModelToChange,
          it.halfProductModel,
          it.halfProductHostId,
          it.weight,
          it.weightUnit,
          it.weightOfPiece
        )
      )
    }
  }

  private fun setProductIdOfEditedProduct(productModelEdited: ProductModel) {
    productId = productModelEdited.productId
  }

  private fun setDialogFieldsAccordinglyToProductEdited(productModelPassedFromAdapter: ProductModel) {
    binding.editProductName.setText(productModelPassedFromAdapter.name)
    binding.editProductPrice.setText(productModelPassedFromAdapter.pricePerUnit.toString())
    binding.editProductTax.setText(productModelPassedFromAdapter.tax.toString())
    binding.editProductWaste.setText(productModelPassedFromAdapter.waste.toString())
    binding.spinnerEditProduct.setText(productModelPassedFromAdapter.unit, false)
  }

  override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    unitPosition = when (parent?.id) {
      1 -> position
      else -> position
    }
  }
}

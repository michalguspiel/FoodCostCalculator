package com.erdees.foodcostcalc.ui.screens.halfProducts.halfProductsFragment.addProductToHalfProductDialogFragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.AddProductToHalfProductBinding
import com.erdees.foodcostcalc.data.model.HalfProductBase
import com.erdees.foodcostcalc.ui.dialogFragments.informationDialogFragment.InformationDialogFragment
import com.erdees.foodcostcalc.utils.Constants.HALFPRODUCT_SPINNER_ID
import com.erdees.foodcostcalc.utils.Constants.PRODUCT_SPINNER_ID
import com.erdees.foodcostcalc.utils.Constants.UNIT_SPINNER_ID
import com.erdees.foodcostcalc.utils.UnitsUtils.getPerUnitAsDescription
import com.erdees.foodcostcalc.utils.Utils.changeUnitList
import com.erdees.foodcostcalc.utils.ViewUtils.hideKeyboard
import com.erdees.foodcostcalc.utils.ViewUtils.makeGone
import com.erdees.foodcostcalc.utils.ViewUtils.makeVisible
import com.erdees.foodcostcalc.utils.ViewUtils.showShortToast

class AddProductToHalfProductFragment : DialogFragment(), AdapterView.OnItemSelectedListener {

  private var _binding: AddProductToHalfProductBinding? = null
  private val binding get() = _binding!!

  private val unitList = arrayListOf<String>()
  private lateinit var halfProductAdapter: ArrayAdapter<String>
  private lateinit var productsAdapter: ArrayAdapter<String>
  private lateinit var unitAdapter: ArrayAdapter<String>

  lateinit var viewModel: AddProductToHalfProductFragmentViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = AddProductToHalfProductBinding.inflate(inflater, container, false)
    val view = binding.root
    viewModel =
      ViewModelProvider(this)[AddProductToHalfProductFragmentViewModel::class.java]

    binding.productWeightInHalfProduct.setOnFocusChangeListener { _, hasFocus ->
      if (!hasFocus) view.hideKeyboard()
    }
    binding.weightForPiece.makeGone()

    viewModel.halfProducts.observe(viewLifecycleOwner) { halfProducts ->
      halfProductAdapter =
        ArrayAdapter(
          requireActivity(),
          R.layout.spinner_layout,
          halfProducts.map { it.halfProductBase.name })
      setHalfProductsSpinner()
      halfProductAdapter.notifyDataSetChanged()
      pickHalfProductIfPresent()
    }

    viewModel.products.observe(viewLifecycleOwner) { products ->
      Log.i(TAG, "readAllProductModelData $products")
      productsAdapter =
        ArrayAdapter(requireActivity(), R.layout.spinner_layout, products.map { it.name })
      setProductsSpinner()
      productsAdapter.notifyDataSetChanged()
    }

    binding.calculateWasteInfoButton.setOnClickListener {
      InformationDialogFragment().show(this.parentFragmentManager, TAG)
    }

    binding.addProductToHalfproductBtn.setOnClickListener {
      if (eitherOfSpinnersIsEmpty()) {
        showShortToast(
          requireContext(),
          message = "You must pick half product and product."
        )
        return@setOnClickListener
      } else if (binding.productWeightInHalfProduct.text.isNullOrEmpty() || binding.productWeightInHalfProduct.text.toString() == ".") {
        showShortToast(requireContext(), message = "You can't add product without weight.")
        return@setOnClickListener
      } else if (!viewModel.isHalfProductPiece && viewModel.isProductPiece && binding.weightForPiece.text.isEmpty()) {
        showShortToast(
          requireContext(),
          message = "You need to provide ${viewModel.getHalfProductUnitType()} of this product!"
        )
      } else {

        val weight = binding.productWeightInHalfProduct.text.toString().toDouble()
        val pieceWeight =
          if (!viewModel.isHalfProductPiece && viewModel.isProductPiece) binding.weightForPiece.text.toString()
            .toDouble() else 1.0
        viewModel.addProductToHalfProduct(weight, pieceWeight)
        binding.productWeightInHalfProduct.text.clear()
        showShortToast(
          requireContext(),
          message = "${
            viewModel.getChosenProductName()
          } added."
        )
      }
    }

    dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    return view
  }

  private fun pickHalfProductIfPresent() {
    val halfProductToSelect = passedHalfProductBase ?: return
    val positionToSelect = halfProductAdapter.getPosition(halfProductToSelect.name)
    binding.halfProductSpinner.setSelection(positionToSelect)
  }

  private fun setUnitsSpinner() {
    unitAdapter =
      ArrayAdapter(requireActivity(), R.layout.support_simple_spinner_dropdown_item, unitList)
    with(binding.unitSpinner) {
      adapter = unitAdapter
      onItemSelectedListener = this@AddProductToHalfProductFragment
      prompt = getString(R.string.select_unit)
      gravity = Gravity.CENTER
      id = UNIT_SPINNER_ID
      setSelection(0, false)
    }
  }

  private fun setProductsSpinner() {
    with(binding.productSpinner)
    {
      adapter = productsAdapter
      onItemSelectedListener = this@AddProductToHalfProductFragment
      prompt = getString(R.string.select_product)
      gravity = Gravity.CENTER
      id = PRODUCT_SPINNER_ID
      setSelection(0, false)
    }
  }

  private fun setHalfProductsSpinner() {
    with(binding.halfProductSpinner) {
      adapter = halfProductAdapter
      id = HALFPRODUCT_SPINNER_ID
      onItemSelectedListener = this@AddProductToHalfProductFragment
      prompt = "choose half product"
      gravity = Gravity.CENTER
    }
  }

  companion object {
    fun newInstance(): AddProductToHalfProductFragment =
      AddProductToHalfProductFragment()

    var passedHalfProductBase: HalfProductBase? = null
    const val TAG = "AddProductToHalfProductFragment"
  }

  private fun eitherOfSpinnersIsEmpty(): Boolean {
    return (viewModel.products.value.isNullOrEmpty() || viewModel.halfProducts.value.isNullOrEmpty())
  }

  private fun setTextField() {
    if (viewModel.isProductPiece && !viewModel.isHalfProductPiece) {
      binding.weightForPiece.makeVisible()
      binding.weightForPiece.hint =
        "${viewModel.getChosenProductName()} ${getPerUnitAsDescription(viewModel.getHalfProductUnit())}."
    } else binding.weightForPiece.makeGone()
  }

  override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    when (parent?.id) {
      1 -> {
        viewModel.updateChosenProductData(position)
        unitList.changeUnitList(
          viewModel.getUnitType() ?: "",
          viewModel.metricCondition,
          viewModel.imperialCondition
        )
        setUnitsSpinner()
        unitAdapter.notifyDataSetChanged()
        viewModel.setUnit(unitList.first())
        setTextField()
      }

      2 -> {
        viewModel.updateChosenHalfProductData(position)
        setTextField()
      }

      else -> {
        viewModel.setUnit(unitList[position])
      }
    }
  }

  override fun onNothingSelected(parent: AdapterView<*>?) {}
}

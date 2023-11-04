package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.addProductToHalfProductDialogFragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.SearchableListDialog
import com.erdees.foodcostcalc.databinding.AddProductToHalfProductBinding
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductModel
import com.erdees.foodcostcalc.ui.dialogFragments.informationDialogFragment.InformationDialogFragment
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
  private lateinit var productAdapter: ArrayAdapter<String>
  private lateinit var unitAdapter: ArrayAdapter<String>
  private lateinit var halfProductsList: MutableList<String>
  private lateinit var productList: MutableList<String>

  lateinit var viewModel: AddProductToHalfProductFragmentViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = AddProductToHalfProductBinding.inflate(inflater, container, false)
    val view = binding.root
    viewModel =
      ViewModelProvider(this).get(AddProductToHalfProductFragmentViewModel::class.java)

    binding.productWeightInHalfProduct.setOnFocusChangeListener { _, hasFocus ->
      if (!hasFocus) view.hideKeyboard()
    }
    binding.weightForPiece.makeGone()

    viewModel.updateUnitsConditions()
    setAdapters()
    setHalfProductsSpinner()
    setProductsSpinner()
    setUnitsSpinner()

    viewModel.readAllHalfProductModelData.observe(
      viewLifecycleOwner
    ) {
      it.forEach { halfProduct -> halfProductsList.add(halfProduct.name) }
      pickHalfProductIfPresent()
    }

    viewModel.readAllProductModelData.observe(
      viewLifecycleOwner
    ) { it.forEach { product -> productList.add(product.name) } }

    viewModel.readAllProductModelData.observe(
      viewLifecycleOwner
    ) { products ->
      productAdapter.clear()
      products.forEach { product ->
        productAdapter.add(product.name)
        productAdapter.notifyDataSetChanged()
      }
    }

    viewModel.readAllHalfProductModelData.observe(
      viewLifecycleOwner
    ) { halfProducts ->
      halfProductAdapter.clear()
      halfProducts.forEach { halfProduct ->
        halfProductAdapter.add(halfProduct.name)
        halfProductAdapter.notifyDataSetChanged()
      }
    }

    binding.halfProductSpinner.setOnClickListener {
      Log.i(TAG, "CLICKED IT, I COULD HAVE OPEN SOMETHING ELSE NOW HM...")
      val spinner = SearchableListDialog.newInstance(halfProductsList)
      spinner.setOnSearchableItemClickListener { item, position ->
        viewModel.updateChosenHalfProductData(position)
        setTextField()
        binding.halfProductSpinner.setText(item.toString())
      }
      spinner.show(
        this.parentFragmentManager,
        TAG
      )
    }
    binding.productSpinner.setOnClickListener {
      val spinner = SearchableListDialog.newInstance(productList)
      spinner.setOnSearchableItemClickListener { item, position ->
        viewModel.updateChosenProductData(position)
        unitList.changeUnitList(
          viewModel.getUnitType(),
          viewModel.metricCondition,
          viewModel.usaCondition
        )
        unitAdapter.notifyDataSetChanged()
        viewModel.setUnit(unitList.first())
        binding.unitSpinner.setText(viewModel.chosenUnit) // when the product is chosen first units got chosen immediately
        setTextField()
        binding.productSpinner.setText(item.toString())
      }
      spinner.show(
        this.parentFragmentManager,
        TAG
      )
    }

    binding.calculateWasteInfoButton.setOnClickListener {
      InformationDialogFragment().show(
        this.parentFragmentManager,
        TAG
      )
    }

    binding.addProductToHalfproductButton.setOnClickListener {
      if (eitherOfSpinnersIsEmpty()) {
        showShortToast(
          requireContext(),
          message = "You must pick half product and product."
        )
        return@setOnClickListener
      } else if (binding.productWeightInHalfProduct.text.isNullOrEmpty() || binding.productWeightInHalfProduct.text.toString() == ".") {
        showShortToast(requireContext(), message = "You can't add product without weight.")
        return@setOnClickListener
      } else if (!viewModel.isHalfProductPiece && viewModel.isProductPiece && binding.weightForPiece.text?.isEmpty() == true) {
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
        binding.productWeightInHalfProduct.text?.clear()
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
    val halfProductToSelect = passedHalfProduct ?: return
    val positionToSelect = halfProductsList.indexOf(halfProductToSelect.name)
    binding.halfProductSpinner.setSelection(positionToSelect)
  }

  private fun setAdapters() {
    halfProductsList = mutableListOf()
    halfProductAdapter =
      ArrayAdapter(requireActivity(), R.layout.spinner_layout, halfProductsList)
    productList = mutableListOf()
    productAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_layout, productList)
    unitAdapter =
      ArrayAdapter(requireActivity(), R.layout.support_simple_spinner_dropdown_item, unitList)
    unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
  }

  private fun setUnitsSpinner() {
    with(binding.unitSpinner) {
//      adapter = unitAdapter
//      setSelection(0, false)
//      onItemSelectedListener = this@AddProductToHalfProductFragment
//      prompt = "Select unit"
//      gravity = Gravity.CENTER
//      id = UNIT_SPINNER_ID
    }
  }

  private fun setProductsSpinner() {
    with(binding.productSpinner)
    {
//      adapter = productAdapter
//      setSelection(0, false)
//      onItemSelectedListener = this@AddProductToHalfProductFragment
//      prompt = "Select product"
//      gravity = Gravity.CENTER
//      id = PRODUCT_SPINNER_ID
    }
    productAdapter.notifyDataSetChanged()
  }

  private fun setHalfProductsSpinner() {
    with(binding.halfProductSpinner) {
//      adapter = halfProductAdapter
//      id = HALFPRODUCT_SPINNER_ID
//      onItemSelectedListener = this@AddProductToHalfProductFragment
    }
    halfProductAdapter.notifyDataSetChanged()
  }

  companion object {
    fun newInstance(): AddProductToHalfProductFragment =
      AddProductToHalfProductFragment()

    var passedHalfProduct: HalfProductModel? = null
    const val TAG = "AddProductToHalfProductFragment"
  }

  private fun eitherOfSpinnersIsEmpty(): Boolean {
    return (viewModel.readAllProductModelData.value.isNullOrEmpty() || viewModel.readAllHalfProductModelData.value.isNullOrEmpty())
  }

  private fun setTextField() {
    if (viewModel.isProductPiece && !viewModel.isHalfProductPiece) {
      binding.weightForPiece.makeVisible()
      binding.weightForPiece.hint =
        "${viewModel.getChosenProductName()} ${getPerUnitAsDescription(viewModel.getHalfProductUnit())}."
    } else binding.weightForPiece.makeGone()
  }

  override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    viewModel.setUnit(unitList[position])
  }

  override fun onNothingSelected(parent: AdapterView<*>?) {
    this.dismiss()
  }
}

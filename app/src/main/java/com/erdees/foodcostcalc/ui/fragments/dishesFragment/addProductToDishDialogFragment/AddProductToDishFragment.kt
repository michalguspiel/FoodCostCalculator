package com.erdees.foodcostcalc.ui.fragments.dishesFragment.addProductToDishDialogFragment

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
import android.widget.CompoundButton
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.AddProductsToDishBinding
import com.erdees.foodcostcalc.data.model.Dish
import com.erdees.foodcostcalc.utils.Constants.DISH_SPINNER_ID
import com.erdees.foodcostcalc.utils.Constants.PRODUCT_SPINNER_ID
import com.erdees.foodcostcalc.utils.Constants.UNIT_SPINNER_ID
import com.erdees.foodcostcalc.utils.ViewUtils.showShortToast

class AddProductToDishFragment : DialogFragment(), AdapterView.OnItemSelectedListener {

  private var _binding: AddProductsToDishBinding? = null
  private val binding get() = _binding!!

  lateinit var viewModel: AddProductToDishFragmentViewModel
  private lateinit var unitAdapter: ArrayAdapter<String>
  private lateinit var dishesAdapter: ArrayAdapter<String>
  private lateinit var productsAdapter: ArrayAdapter<String>
  private lateinit var halfProductAdapter: ArrayAdapter<String>

  override fun onNothingSelected(parent: AdapterView<*>?) {}

  override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    when (parent?.id) {
      PRODUCT_SPINNER_ID -> {
        viewModel.productPosition = position
        if (!binding.productHalfproductSwitch.isChecked) viewModel.setProductUnitType(
          position
        )
        else viewModel.setHalfProductUnitType(position)
        viewModel.updateUnitList()
        setUnitsSpinner()
        unitAdapter.notifyDataSetChanged()
        binding.unitSpinner.setSelection(0) // when the product is chosen first units got chosen immediately
      }

      DISH_SPINNER_ID -> {
        viewModel.dishPosition = position
      }

      UNIT_SPINNER_ID -> {
        viewModel.chooseUnit(position)
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = AddProductsToDishBinding.inflate(inflater, container, false)
    val view = binding.root

    viewModel = ViewModelProvider(this)[AddProductToDishFragmentViewModel::class.java]
    viewModel.updateUnitsConditions()

    binding.productHalfproductSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
      if (isChecked) setUiToHalfProductsSpinner()
      else setUiToProductsSpinner()
    }

    viewModel.readAllProductData.observe(viewLifecycleOwner) { products ->
      productsAdapter =
        ArrayAdapter(requireActivity(), R.layout.spinner_layout, products.map { it.name })
      setProductsSpinner()
      productsAdapter.notifyDataSetChanged()
    }

    viewModel.readAllDishData.observe(viewLifecycleOwner) { dishes ->
      dishesAdapter =
        ArrayAdapter(requireActivity(), R.layout.spinner_layout, dishes.map { it.name })
      setDishesSpinner()
      dishesAdapter.notifyDataSetChanged()
      selectChosenDish()
    }

    viewModel.readAllHalfProductData.observe(viewLifecycleOwner) { halfProducts ->
      halfProductAdapter =
        ArrayAdapter(requireActivity(), R.layout.spinner_layout, halfProducts.map { it.name })
      halfProductAdapter.notifyDataSetChanged()
    }

    binding.addProductToDishBtn.setOnClickListener {
      val result = viewModel.addToDish(
        binding.productWeight.text.toString(), binding.productHalfproductSwitch.isChecked
      )
      when (result) {
        AddProductToDishFragmentViewModel.Result.FailureProduct -> showShortToast(
          requireContext(), getString(R.string.you_must_pick_product)
        )

        AddProductToDishFragmentViewModel.Result.FailureDish -> showShortToast(
          requireContext(), getString(R.string.you_must_pick_dish)
        )

        AddProductToDishFragmentViewModel.Result.FailureWeight -> showShortToast(
          requireContext(), getString(R.string.cant_add_product_without_weight)
        )

        is AddProductToDishFragmentViewModel.Result.SuccessProduct -> {
          binding.productWeight.text.clear()
          showShortToast(
            requireContext(), getString(R.string.product_added, result.name)
          )
        }

        is AddProductToDishFragmentViewModel.Result.SuccessHalfProduct -> {
          binding.productWeight.text.clear()
          showShortToast(
            requireContext(), getString(R.string.product_added, result.name)
          )
        }
      }
    }
    dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    return view
  }

  companion object {
    fun newInstance(): AddProductToDishFragment = AddProductToDishFragment()

    const val TAG = "AddProductToDishFragment"
    var dishPassedFromAdapter: Dish? = null
  }

  private fun setUnitsSpinner() {
    unitAdapter = ArrayAdapter(
      requireActivity(), R.layout.support_simple_spinner_dropdown_item, viewModel.getUnitList()
    )
    with(binding.unitSpinner) {
      adapter = unitAdapter
      onItemSelectedListener = this@AddProductToDishFragment
      prompt = getString(R.string.select_unit)
      gravity = Gravity.CENTER
      id = UNIT_SPINNER_ID
      setSelection(0, false)
    }
  }

  private fun setDishesSpinner() {
    with(binding.dishSpinner) {
      adapter = dishesAdapter
      onItemSelectedListener = this@AddProductToDishFragment
      prompt = getString(R.string.select_dish)
      gravity = Gravity.CENTER
      id = DISH_SPINNER_ID
    }
  }

  private fun setProductsSpinner() {
    with(binding.productSpinner) {
      adapter = productsAdapter
      onItemSelectedListener = this@AddProductToDishFragment
      prompt = getString(R.string.select_product)
      gravity = Gravity.CENTER
      id = PRODUCT_SPINNER_ID
      setSelection(0)
    }
  }

  private fun setUiToProductsSpinner() {
    binding.chooseProductOrHalfProductTv.text = resources.getString(R.string.choose_product)
    binding.productHalfproductSwitch.text = getString(R.string.switch_to_half_products)
    binding.productSpinner.adapter = productsAdapter
  }

  private fun setUiToHalfProductsSpinner() {
    binding.chooseProductOrHalfProductTv.text = resources.getString(R.string.choose_half_product)
    binding.productHalfproductSwitch.text = getString(R.string.switch_to_products)
    binding.productSpinner.adapter = halfProductAdapter
  }

  private fun selectChosenDish() {
    Log.i(this.tag, "selectChosenDish ${dishPassedFromAdapter?.name}")
    val dishToSelect = dishPassedFromAdapter ?: return
    val positionToSelect = dishesAdapter.getPosition(dishToSelect.name)
    Log.i(this.tag, "position to select $positionToSelect")
    binding.dishSpinner.setSelection(positionToSelect)
  }
}

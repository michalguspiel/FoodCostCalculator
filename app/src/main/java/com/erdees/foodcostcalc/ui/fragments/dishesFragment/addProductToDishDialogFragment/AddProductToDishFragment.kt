package com.erdees.foodcostcalc.ui.fragments.dishesFragment.addProductToDishDialogFragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.*
import com.erdees.foodcostcalc.databinding.AddProductsToDishBinding
import com.erdees.foodcostcalc.domain.model.dish.DishModel
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.DishesFragmentRecyclerAdapter
import com.erdees.foodcostcalc.utils.Constants.DISH_SPINNER_ID
import com.erdees.foodcostcalc.utils.Constants.PRODUCT_SPINNER_ID
import com.erdees.foodcostcalc.utils.Constants.UNIT_SPINNER_ID
import com.erdees.foodcostcalc.utils.ViewUtils.showShortToast

class AddProductToDishFragment : DialogFragment(), AdapterView.OnItemSelectedListener {

    private var _binding: AddProductsToDishBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: AddProductToDishFragmentViewModel
    private lateinit var unitAdapter: ArrayAdapter<*>
    private lateinit var dishesAdapter: ArrayAdapter<String>
    private lateinit var productsAdapter: ArrayAdapter<String>
    private lateinit var halfProductAdapter: ArrayAdapter<String>

    override fun onNothingSelected(parent: AdapterView<*>?) {
        this.dismiss()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            PRODUCT_SPINNER_ID -> {
                viewModel.productPosition = position
                if (!binding.productHalfproductSwitch.isChecked) viewModel.setProductUnitType(
                    position
                )
                else viewModel.setHalfProductUnitType(position)
                viewModel.updateUnitList()
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddProductsToDishBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel =
            ViewModelProvider(this).get(AddProductToDishFragmentViewModel::class.java)
        viewModel.updateUnitsConditions()
        setAdapters()
        setProductsSpinner() //because initially dialog start with products
        setDishesSpinner()
        setUnitsSpinner()

        binding.productHalfproductSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) setUiToHalfProductsSpinner()
            else setUiToProductsSpinner()
        }

        viewModel.readAllProductModelData.observe(viewLifecycleOwner) { products ->
          productsAdapter.addAll(products.map { it.name })
          productsAdapter.notifyDataSetChanged()
        }

      viewModel.readAllDishModelData.observe(viewLifecycleOwner) { dishes ->
          dishesAdapter.addAll(dishes.map { it.name })
          dishesAdapter.notifyDataSetChanged()
          selectChosenDish()
        }

      viewModel.readAllHalfProductModelData.observe(viewLifecycleOwner) { halfProducts ->
          halfProductAdapter.addAll(halfProducts.map { it.name })
          halfProductAdapter.notifyDataSetChanged()
        }

      binding.addProductToDishBtn.setOnClickListener {
            if (isProductWeightInvalid()) {
                showShortToast(
                    requireContext(),
                    getString(R.string.cant_add_product_without_weight)
                )
            } else if (viewModel.readAllDishModelData.value.isNullOrEmpty()) {
                showShortToast(requireContext(), getString(R.string.you_must_pick_dish))
            } else if (!binding.productHalfproductSwitch.isChecked) {
                if (viewModel.readAllProductModelData.value.isNullOrEmpty()) {
                    showShortToast(requireContext(), getString(R.string.you_must_pick_product))
                    return@setOnClickListener
                }
                addChosenProductToDish()
            } else {
                if (viewModel.getHalfProducts().value.isNullOrEmpty()) {
                    showShortToast(requireContext(), getString(R.string.you_must_pick_product))
                    return@setOnClickListener
                }
                addChosenHalfProductToDish()
            }
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    companion object {
        fun newInstance(): AddProductToDishFragment =
            AddProductToDishFragment()

        const val TAG = "AddProductToDishFragment"
        lateinit var dishModelPassedFromAdapter: DishModel
    }

    private fun addChosenHalfProductToDish() {
        val halfProductAdded = viewModel.addHalfProductIncludedInDish(
            binding.productWeight.text.toString().toDouble()
        )
        binding.productWeight.text.clear()
        showShortToast(
            requireContext(),
            getString(R.string.product_added, halfProductAdded.name)
        )
    }

    private fun addChosenProductToDish() {
        val productAdded = viewModel.addProductToDish(
            binding.productWeight.text.toString().toDouble(),
        )
        binding.productWeight.text.clear()
        showShortToast(
            requireContext(), getString(
                R.string.product_added, productAdded.name
            )
        )
    }

    private fun setAdapters() {
        halfProductAdapter =
            ArrayAdapter(requireActivity(), R.layout.spinner_layout, mutableListOf<String>())
        dishesAdapter =
            ArrayAdapter(requireActivity(), R.layout.spinner_layout, mutableListOf<String>())
        productsAdapter =
            ArrayAdapter(requireActivity(), R.layout.spinner_layout, mutableListOf<String>())
        unitAdapter =
            ArrayAdapter(
                requireActivity(),
                R.layout.support_simple_spinner_dropdown_item,
                viewModel.getUnitList()
            )
    }

    private fun setUnitsSpinner() {
        with(binding.unitSpinner) {
            adapter = unitAdapter
            setSelection(0, false)
            onItemSelectedListener = this@AddProductToDishFragment
            prompt = getString(R.string.select_unit)
            gravity = Gravity.CENTER
            id = UNIT_SPINNER_ID
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
        with(binding.productSpinner)
        {
            adapter = productsAdapter
            setSelection(0, false)
            onItemSelectedListener = this@AddProductToDishFragment
            prompt = getString(R.string.select_product)
            gravity = Gravity.CENTER
            id = PRODUCT_SPINNER_ID
        }
    }

    private fun setUiToProductsSpinner() {
        binding.chooseProductOrHalfProductTv.text =
            resources.getString(R.string.choose_product)
        binding.productHalfproductSwitch.text = getString(R.string.switch_to_half_products)
        binding.productSpinner.adapter = productsAdapter
    }

    private fun setUiToHalfProductsSpinner() {
        binding.chooseProductOrHalfProductTv.text =
            resources.getString(R.string.choose_half_product)
        binding.productHalfproductSwitch.text = getString(R.string.switch_to_products)
        binding.productSpinner.adapter = halfProductAdapter
    }

    private fun selectChosenDish() {
      Log.i(this.tag,"selectChosenDish ${dishModelPassedFromAdapter.name}")
        if (this.isOpenedFromDishAdapter()) {
            val dishToSelect = dishModelPassedFromAdapter
            val positionToSelect = dishesAdapter.getPosition(dishToSelect.name)
          Log.i(this.tag,"position to select $positionToSelect")
            binding.dishSpinner.setSelection(positionToSelect)
        }
    }

    private fun isProductWeightInvalid(): Boolean {
        return (binding.productWeight.text.isNullOrEmpty() || binding.productWeight.text.toString() == ".")
    }

    private fun isOpenedFromDishAdapter(): Boolean {
        return this.tag == DishesFragmentRecyclerAdapter.TAG
    }
}

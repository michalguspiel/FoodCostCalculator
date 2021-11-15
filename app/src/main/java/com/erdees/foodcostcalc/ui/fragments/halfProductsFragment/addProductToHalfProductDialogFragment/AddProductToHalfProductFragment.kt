package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.addProductToHalfProductDialogFragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.*
import com.erdees.foodcostcalc.databinding.AddProductToHalfProductBinding
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
    private lateinit var productAdapter: ArrayAdapter<String>
    private lateinit var unitAdapter: ArrayAdapter<String>
    private lateinit var halfProductsList: MutableList<String>
    private lateinit var productList: MutableList<String>

    lateinit var viewModel: AddProductToHalfProductFragmentViewModel


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
            viewLifecycleOwner,
            {
                it.forEach { halfProduct -> halfProductsList.add(halfProduct.name) }
                if (this.isOpenFromHalfProductAdapter()) {
                    pickHalfProduct()
                }
            })

        viewModel.readAllProductModelData.observe(
            viewLifecycleOwner,
            { it.forEach { product -> productList.add(product.name) } })

        viewModel.readAllProductModelData.observe(
            viewLifecycleOwner,
            { products ->
                productAdapter.clear()
                products.forEach { product ->
                    productAdapter.add(product.name)
                    productAdapter.notifyDataSetChanged()
                }
            })

        viewModel.readAllHalfProductModelData.observe(
            viewLifecycleOwner,
            { halfProducts ->
                halfProductAdapter.clear()
                halfProducts.forEach { halfProduct ->
                    halfProductAdapter.add(halfProduct.name)
                    halfProductAdapter.notifyDataSetChanged()
                }
            })

        binding.calculateWasteInfoButton.setOnClickListener {
            InformationDialogFragment().show(
                this.parentFragmentManager,
                TAG
            )
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

    private fun pickHalfProduct() {
        val halfProductToSelect = viewModel.getHalfProductToDialog().value
        val positionToSelect = halfProductsList.indexOf(halfProductToSelect!!.name)
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
            adapter = unitAdapter
            setSelection(0, false)
            onItemSelectedListener = this@AddProductToHalfProductFragment
            prompt = "Select unit"
            gravity = Gravity.CENTER
            id = UNIT_SPINNER_ID
        }
    }

    private fun setProductsSpinner() {
        with(binding.productSpinner)
        {
            adapter = productAdapter
            setSelection(0, false)
            onItemSelectedListener = this@AddProductToHalfProductFragment
            prompt = "Select product"
            gravity = Gravity.CENTER
            id = PRODUCT_SPINNER_ID
        }
        productAdapter.notifyDataSetChanged()
    }

    private fun setHalfProductsSpinner() {
        with(binding.halfProductSpinner) {
            adapter = halfProductAdapter
            id = HALFPRODUCT_SPINNER_ID
            onItemSelectedListener = this@AddProductToHalfProductFragment
            prompt = "choose half product"
            gravity = Gravity.CENTER
        }
        halfProductAdapter.notifyDataSetChanged()
    }

    companion object {
        fun newInstance(): AddProductToHalfProductFragment =
            AddProductToHalfProductFragment()

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
        when (parent?.id) {
            1 -> {
                viewModel.updateChosenProductData(position)
                unitList.changeUnitList(
                    viewModel.getUnitType(),
                    viewModel.metricCondition,
                    viewModel.usaCondition
                )
                unitAdapter.notifyDataSetChanged()
                binding.unitSpinner.setSelection(0, false)
                viewModel.setUnit(unitList.first())
                binding.unitSpinner.setSelection(0) // when the product is chosen first units got chosen immediately
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

    override fun onNothingSelected(parent: AdapterView<*>?) {
        this.dismiss()
    }

    private fun isOpenFromHalfProductAdapter(): Boolean {
        return this.tag == "HalfProductFragmentRecyclerAdapter"
    }
}
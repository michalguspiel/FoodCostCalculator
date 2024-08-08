package com.erdees.foodcostcalc.ui.fragments.addFragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.FragmentAddBinding
import com.erdees.foodcostcalc.ui.activities.mainActivity.MainActivity
import com.erdees.foodcostcalc.ui.dialogFragments.informationDialogFragment.InformationDialogFragment
import com.erdees.foodcostcalc.data.SharedPreferences
import com.erdees.foodcostcalc.utils.Constants.ADD_FRAGMENT_SPINNER_ID
import com.erdees.foodcostcalc.utils.ViewUtils.hideKeyboard
import com.erdees.foodcostcalc.utils.ViewUtils.isNotEmptyNorJustDot
import com.erdees.foodcostcalc.utils.ViewUtils.makeCreationConfirmationSnackBar
import com.erdees.foodcostcalc.utils.ViewUtils.scrollUp
import com.erdees.foodcostcalc.utils.ViewUtils.showShortToast
import com.google.firebase.analytics.FirebaseAnalytics

class AddFragment : Fragment(), AdapterView.OnItemClickListener {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewModel: AddFragmentViewModel

    private val informationDialog = InformationDialogFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddFragmentViewModel::class.java)
        viewModel.firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
    }

    override fun onResume() {
        val unitsAdapter =
            ArrayAdapter(requireActivity(), R.layout.dropdown_item, viewModel.unitList)
        with(binding.unitsSpinner) {
            setSelection(0)
            setAdapter(unitsAdapter)
            onItemClickListener = this@AddFragment
            gravity = Gravity.CENTER
            id = ADD_FRAGMENT_SPINNER_ID
        }
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root
        setToolbar()
        sharedPreferences = SharedPreferences(requireContext())
        viewModel.getUnits(resources, sharedPreferences)

        binding.unitsSpinner.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) view.hideKeyboard()
        }

        binding.addProductBtn.setOnClickListener {
            when {
                oneOfTheFieldsInputIsWrong() -> showShortToast(
                    requireContext(),
                    getString(R.string.fill_all_data)
                )
                unitIsNotChosen() -> showShortToast(
                    requireContext(),
                    getString(R.string.choose_unit)
                )
                else -> {
                    viewModel.addProduct(
                        binding.productNameEditText.text.toString(),
                        binding.productPriceEditText.text.toString().toDouble(),
                        binding.productTaxEditText.text.toString().toDouble(),
                        binding.productWasteEditText.text.toString().toDouble(),
                        binding.unitsSpinner.text.toString()
                    )
                    binding.root.makeCreationConfirmationSnackBar(
                        binding.productNameEditText.text.toString(),
                        requireContext()
                    )
                    clearInputFields()
                }
            }
        }

        binding.countWastePercentBtn.setOnClickListener {
            if (wasteCalculatorFieldsAreNotEmpty()) {
                val productWeight = binding.wasteCalcProductWeightEditText.text
                val productWaste = binding.wasteCalcWasteWeightEditText.text
                val result = viewModel.calculateWaste(
                    productWeight.toString().toDouble(),
                    productWaste.toString().toDouble()
                )
                binding.productWasteEditText.setText(result)
                productWaste?.clear()
                productWeight?.clear()
                binding.addScrollView.scrollUp()
            }
        }

        binding.countPricePerPieceBtn.setOnClickListener {
            if (priceCalculatorFieldsAreNotEmpty()) {
                val boxPrice = binding.calcPricePerPieceBoxPriceEditText.text
                val quantity = binding.calcPricePerPieceQuantityEditText.text
                val result = viewModel.calculatePricePerPiece(
                    boxPrice.toString().toDouble(),
                    quantity.toString().toDouble()
                )
                binding.productPriceEditText.setText(result)
                boxPrice?.clear()
                quantity?.clear()
                binding.addScrollView.scrollUp()
            }
        }

        binding.infoButton.setOnClickListener {
            informationDialog.show(parentFragmentManager, TAG)
        }
        binding.calculateWasteInfoButton.setOnClickListener {
            informationDialog.show(parentFragmentManager, "WasteCalculatorInfo")
        }
        binding.calculatePricePerPieceInfoBtn.setOnClickListener {
            informationDialog.show(parentFragmentManager, "BoxPriceCalculatorInfo")
        }
        return view
    }

    private fun setToolbar(){
        (activity as MainActivity).setToolBarTitle(getString(R.string.add_product))
    }

    companion object {
        fun newInstance(): AddFragment = AddFragment()
        const val TAG = "AddFragment"
    }

    private fun clearInputFields() {
        binding.productNameEditText.text!!.clear()
        binding.productPriceEditText.text!!.clear()
        binding.productTaxEditText.text!!.clear()
        binding.productWasteEditText.text!!.clear()
    }

    private fun unitIsNotChosen(): Boolean {
        return (binding.unitsSpinner.text.toString() == getString(R.string.choose_unit) || binding.unitsSpinner.text.isNullOrBlank())
    }

    private fun oneOfTheFieldsInputIsWrong(): Boolean {
        return (binding.productNameEditText.text.isNullOrEmpty() ||
                !binding.productPriceEditText.isNotEmptyNorJustDot() ||
                !binding.productTaxEditText.isNotEmptyNorJustDot() ||
                !binding.productWasteEditText.isNotEmptyNorJustDot())
    }

    private fun wasteCalculatorFieldsAreNotEmpty(): Boolean {
        return binding.wasteCalcProductWeightEditText.isNotEmptyNorJustDot() && binding.wasteCalcWasteWeightEditText.isNotEmptyNorJustDot()
    }

    private fun priceCalculatorFieldsAreNotEmpty(): Boolean {
        return binding.calcPricePerPieceQuantityEditText.isNotEmptyNorJustDot() && binding.calcPricePerPieceBoxPriceEditText.isNotEmptyNorJustDot()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel.chosenUnit = when (parent?.id) {
            ADD_FRAGMENT_SPINNER_ID -> {
                viewModel.unitList[position]
            }
            else -> {
                viewModel.unitList[position]
            }
        }
    }
}

package com.erdees.foodcostcalc.ui.fragments.addFragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.*
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.FragmentAddBinding
import com.erdees.foodcostcalc.ui.dialogFragments.informationDialogFragment.InformationDialogFragment
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.SharedFunctions.getUnits
import com.erdees.foodcostcalc.utils.SharedFunctions.hideKeyboard
import com.erdees.foodcostcalc.utils.SharedFunctions.isNotEmptyNorJustDot
import com.erdees.foodcostcalc.utils.SharedFunctions.makeSnackBar
import com.google.firebase.analytics.FirebaseAnalytics

class AddFragment : Fragment(), AdapterView.OnItemClickListener {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AddFragmentViewModel
    private var chosenUnit: String = ""
    private var unitList: MutableList<String> = mutableListOf()
    private lateinit var sharedPreferences: SharedPreferences
    private val spinnerId = 1
    private val informationDialog = InformationDialogFragment()

    private lateinit var thisView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddFragmentViewModel::class.java)
        viewModel.firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
    }

    override fun onResume() {
        /**Spinner adapter*/
        unitList = getUnits(resources, sharedPreferences)
        val unitsAdapter = ArrayAdapter(requireActivity(), R.layout.dropdown_item, unitList)
        with(binding.unitsSpinner) {
            setSelection(0)
            setAdapter(unitsAdapter)
            onItemClickListener = this@AddFragment
            gravity = Gravity.CENTER
            id = spinnerId
        }
        super.onResume()
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        thisView = binding.root

        sharedPreferences = SharedPreferences(requireContext())
        unitList = getUnits(resources, sharedPreferences)
        binding.unitsSpinner.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) thisView.hideKeyboard()
        }

        /** BUTTONS FUNCTIONALITY */

        binding.addProductBtn.setOnClickListener {
            when {
                oneOfTheFieldsInputIsWrong() -> showToast(message = "Fill all data!")
                unitIsNotChosen() -> showToast(message = "Choose unit!")
                else -> {
                    viewModel.addProduct(
                        binding.productNameEditText.text.toString(),
                        binding.productPriceEditText.text.toString().toDouble(),
                        binding.productTaxEditText.text.toString().toDouble(),
                        binding.productWasteEditText.text.toString().toDouble(),
                        chosenUnit
                    )
                    thisView.makeSnackBar(
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
                scrollUp()
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
                scrollUp()
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
        return thisView
    }

    companion object {
        fun newInstance(): AddFragment = AddFragment()
        const val TAG = "AddFragment"
    }


    private fun showToast(
        context: FragmentActivity? = activity,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(context, message, duration).show()
    }

    private fun clearInputFields() {
        binding.productNameEditText.text!!.clear()
        binding.productPriceEditText.text!!.clear()
        binding.productTaxEditText.text!!.clear()
        binding.productWasteEditText.text!!.clear()
    }

    private fun unitIsNotChosen(): Boolean {
        return (binding.unitsSpinner.text.toString() == "Choose unit" || binding.unitsSpinner.text.isNullOrBlank())
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

    private fun scrollUp() {
        binding.addScrollView.fullScroll(ScrollView.FOCUS_UP)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        chosenUnit = when (parent?.id) {
            spinnerId -> {
                unitList[position]
            }
            else -> {
                unitList[position]
            }
        }
    }
}
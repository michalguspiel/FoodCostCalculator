package com.erdees.foodcostcalc.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.*
import androidx.lifecycle.ViewModelProvider
import com.erdees.Constants
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.SharedFunctions.getUnits
import com.erdees.foodcostcalc.SharedPreferences
import com.erdees.foodcostcalc.fragments.dialogs.InformationDialog
import com.erdees.foodcostcalc.model.Product
import com.erdees.foodcostcalc.viewmodel.AddViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.analytics.FirebaseAnalytics
import java.math.RoundingMode
import java.text.DecimalFormat


class Add : Fragment(), AdapterView.OnItemClickListener {

    private lateinit var firebaseAnalytics: FirebaseAnalytics


    private var chosenUnit: String = ""
    private var unitList: MutableList<String> = mutableListOf()
    private lateinit var unitSpinner: AutoCompleteTextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var scrollView: ScrollView
    private val spinnerId = 1
    private val informationDialog = InformationDialog()

    private lateinit var calcPricePerBox: EditText

    private lateinit var price: TextInputEditText
    private lateinit var name: TextInputEditText
    private lateinit var tax: TextInputEditText
    private lateinit var waste: TextInputEditText
    private lateinit var calculateWasteBtn: Button
    private lateinit var calcPricePerPieceBtn: Button
    private lateinit var calcProductWeight: EditText
    private lateinit var calcQuantityBox: EditText
    private lateinit var calcWasteWeight: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
    }

    override fun onResume() {
        /**Spinner adapter*/
        unitList = getUnits(resources, sharedPreferences)
        val unitsAdapter = ArrayAdapter(requireActivity(), R.layout.dropdown_item, unitList)
        with(unitSpinner) {
            setSelection(0)
            setAdapter(unitsAdapter)
            onItemClickListener = this@Add
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
        val view: View = inflater.inflate(R.layout.fragment_add, container, false)
        val viewModel = ViewModelProvider(this).get(AddViewModel::class.java)

        /** BINDERS FOR BUTTONS AND FIELDS */
        name = view.findViewById(R.id.product_name)
        price = view.findViewById(R.id.product_price)
        tax = view.findViewById(R.id.product_tax)
        waste = view.findViewById(R.id.product_waste)
        val addButton = view.findViewById<Button>(R.id.addProduct)
        calculateWasteBtn = view.findViewById(R.id.count_waste_percent_btn)
        calcWasteWeight = view.findViewById(R.id.waste_calc_product_waste)
        calcProductWeight = view.findViewById(R.id.waste_calc_product_weight)
        calcPricePerPieceBtn = view.findViewById(R.id.count_price_per_piece_btn)
        calcQuantityBox = view.findViewById(R.id.calc_quantity_box)
        calcPricePerBox = view.findViewById(R.id.calc_price_per_box)
        val informationButton = view.findViewById<ImageButton>(R.id.info_button)
        val calculateWasteInfoButton =
            view.findViewById<ImageButton>(R.id.calculate_waste_info_button)
        val calculatePiecePriceInfoButton =
            view.findViewById<ImageButton>(R.id.calculate_price_per_piece_info_button)
        scrollView = view.findViewById(R.id.add_scroll_view)
        sharedPreferences = SharedPreferences(requireContext())
        unitList = getUnits(resources, sharedPreferences)
        unitSpinner = view.findViewById(R.id.units_spinner)


        /** BUTTONS FUNCTIONALITY */

        addButton.setOnClickListener {
            if (oneOfTheFieldsInputIsWrong()) showToast(message = "Fill all data!")
            else if(unitIsNotChosen()) showToast(message = "Choose unit!")
            else {
                val product = Product(
                    0,
                    name.text.toString(),
                    price.text.toString().toDouble(),
                    tax.text.toString().toDouble(),
                    waste.text.toString().toDouble(),
                    chosenUnit
                )
                viewModel.addProducts(product)
                sendDataAboutProduct(product)
                clearInputFields()
            }
        }

        calculateWasteBtn.setOnClickListener {
            if (wasteCalculatorFieldsAreNotEmpty()) {
                val productWeight = calcProductWeight.text.toString().toDouble()
                val productWaste = calcWasteWeight.text.toString().toDouble()
                calculateWaste(productWeight, productWaste)
                scrollUp()
            }
        }
        calcPricePerPieceBtn.setOnClickListener {
            if (priceCalculatorFieldsAreNotEmpty()) {
                val boxPrice = calcPricePerBox.text.toString().toDouble()
                val quantity = calcQuantityBox.text.toString().toDouble()
                calculatePricePerPiece(boxPrice, quantity)
                scrollUp()
            }
        }

        informationButton.setOnClickListener {
            informationDialog.show(parentFragmentManager, TAG)
        }

        calculateWasteInfoButton.setOnClickListener {
            informationDialog.show(parentFragmentManager, "WasteCalculatorInfo")
        }

        calculatePiecePriceInfoButton.setOnClickListener {
            informationDialog.show(parentFragmentManager, "BoxPriceCalculatorInfo")


        }

        return view
    }

    companion object {
        fun newInstance(): Add = Add()
        const val TAG = "Add"
    }

    private fun sendDataAboutProduct(product: Product){
        val bundle = Bundle()
        bundle.putString(Constants.PRODUCT_NAME,product.name)
        bundle.putString(Constants.PRODUCT_TAX,product.tax.toString())
        bundle.putString(Constants.PRODUCT_WASTE,product.waste.toString())
        bundle.putString(Constants.PRODUCT_UNIT,product.unit)
        bundle.putString(Constants.PRODUCT_PRICE_PER_UNIT,product.pricePerUnit.toString())
        firebaseAnalytics.logEvent(Constants.PRODUCT_CREATED,bundle)

    }

    private fun showToast(
        context: FragmentActivity? = activity,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(context, message, duration).show()
    }

    private fun clearInputFields() {
        name.text!!.clear()
        price.text!!.clear()
        tax.text!!.clear()
        waste.text!!.clear()
    }

    private fun unitIsNotChosen() : Boolean{
        return (unitSpinner.text.toString() == "Choose unit" || unitSpinner.text.isNullOrBlank())
    }

    private fun oneOfTheFieldsInputIsWrong(): Boolean {
        return (name.text.isNullOrEmpty() ||
                price.text.isNullOrEmpty() || price.text.toString() == "." ||
                tax.text.isNullOrEmpty() || tax.text.toString() == "." ||
                waste.text.isNullOrEmpty() || waste.text.toString() == ".")
    }

    /**Because some devices format number with commas which causes errors.*/
    private fun formatResultAndCheckCommas(number: Double): String {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val formattedResult = df.format(number)
        var formattedResultCheck = ""
        for (eachChar in formattedResult) {
            formattedResultCheck += if (eachChar == ',') '.'
            else eachChar
        }
        return formattedResultCheck
    }

    private fun wasteCalculatorFieldsAreNotEmpty(): Boolean {
        return (calcProductWeight.text.isNotEmpty() && calcWasteWeight.text.isNotEmpty() && calcProductWeight.text.toString() != "." && calcWasteWeight.text.toString() != ".")
    }

    private fun calculateWaste(calcWeight: Double, calcWaste: Double) {
        val result = (100 * calcWaste) / calcWeight
        waste.setText(formatResultAndCheckCommas(result))
        calcProductWeight.text.clear()
        calcWasteWeight.text.clear()
    }

    private fun priceCalculatorFieldsAreNotEmpty(): Boolean {
        return (calcPricePerBox.text.isNotEmpty() && calcQuantityBox.text.isNotEmpty()&& calcPricePerBox.text.toString() != "." && calcQuantityBox.text.toString() != ".")
    }

    private fun calculatePricePerPiece(pricePerBox: Double, quantityInBox: Double) {
        val result = pricePerBox / quantityInBox
        price.setText(formatResultAndCheckCommas(result))
        calcPricePerBox.text.clear()
        calcQuantityBox.text.clear()
    }

    private fun scrollUp() {
        scrollView.fullScroll(ScrollView.FOCUS_UP)
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


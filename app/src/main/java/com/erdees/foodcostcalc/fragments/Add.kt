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
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.SharedPreferences
import com.erdees.foodcostcalc.fragments.dialogs.InformationDialog
import com.erdees.foodcostcalc.getUnits
import com.erdees.foodcostcalc.model.Product
import com.erdees.foodcostcalc.viewmodel.AddViewModel
import com.google.android.material.textfield.TextInputEditText
import java.math.RoundingMode
import java.text.DecimalFormat



class Add : Fragment(), AdapterView.OnItemClickListener {

    private var chosenUnit: String = ""
    private var unitList: MutableList<String> = mutableListOf()
    private lateinit var unitSpinner: AutoCompleteTextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var scrollView : ScrollView
    private val spinnerId = 1
    private val informationDialog = InformationDialog()


    private lateinit var calcPricePerBox : EditText
    private lateinit var price : TextInputEditText
    private lateinit var waste : TextInputEditText
    private lateinit var calculateWasteBtn : Button
    private lateinit var calcPricePerPieceBtn : Button
    private lateinit var calcProductWeight : EditText
    private lateinit var calcQuantityBox : EditText
    private lateinit var calcWasteWeight : EditText


    private fun showToast(context: FragmentActivity? = activity, message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, message, duration).show()
    }

    override fun onResume() {
        /**Spinner adapter*/
        unitList = getUnits(resources,sharedPreferences)
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
        val name = view.findViewById<TextInputEditText>(R.id.product_name)
         price = view.findViewById(R.id.product_price)
        val tax = view.findViewById<TextInputEditText>(R.id.product_tax)
         waste = view.findViewById(R.id.product_waste)
        val addButton = view.findViewById<Button>(R.id.addProduct)
         calculateWasteBtn = view.findViewById(R.id.count_waste_percent_btn)
         calcWasteWeight = view.findViewById(R.id.waste_calc_product_waste)
         calcProductWeight = view.findViewById(R.id.waste_calc_product_weight)
         calcPricePerPieceBtn = view.findViewById(R.id.count_price_per_piece_btn)
         calcQuantityBox      = view.findViewById(R.id.calc_quantity_box)
         calcPricePerBox      = view.findViewById(R.id.calc_price_per_box)
        val informationButton       = view.findViewById<ImageButton>(R.id.info_button)
        val calculateWasteInfoButton = view.findViewById<ImageButton>(R.id.calculate_waste_info_button)
        val calculatePiecePriceInfoButton = view.findViewById<ImageButton>(R.id.calculate_price_per_piece_info_button)
        scrollView = view.findViewById(R.id.add_scroll_view)

        sharedPreferences = SharedPreferences(requireContext())
        unitList = getUnits(resources,sharedPreferences)
        unitSpinner = view.findViewById(R.id.units_spinner)



        /** BUTTONS FUNCTIONALITY */

        addButton.setOnClickListener {
            if (name.text.isNullOrEmpty() ||
                    price.text.isNullOrEmpty() ||
                    tax.text.isNullOrEmpty() ||
                    waste.text.isNullOrEmpty()) {
                showToast(message = "Fill all data!")
            } else {
                val product = Product(0,
                        name.text.toString(),
                        price.text.toString().toDouble(),
                        tax.text.toString().toDouble(),
                        waste.text.toString().toDouble(),
                        chosenUnit
                )
                viewModel.addProducts(product)

                name.text!!.clear()
                price.text!!.clear()
                tax.text!!.clear()
                waste.text!!.clear()
            }
        }

        calculateWasteBtn.setOnClickListener {
            if (calcProductWeight.text.isNotEmpty() && calcWasteWeight.text.isNotEmpty()){
                calculateWaste(calcProductWeight.text.toString().toDouble(),
                                calcWasteWeight.text.toString().toDouble())
                scrollUp()
            }

        }


        calcPricePerPieceBtn.setOnClickListener {
            if(calcPricePerBox.text.isNotEmpty()  && calcQuantityBox.text.isNotEmpty()){
                calculatePricePerPiece(calcPricePerBox.text.toString().toDouble(),
                    calcQuantityBox.text.toString().toDouble())
            scrollUp()
            }
        }


        informationButton.setOnClickListener{
            informationDialog.show(parentFragmentManager,TAG)
        }

        calculateWasteInfoButton.setOnClickListener {
            informationDialog.show(parentFragmentManager,"WasteCalculatorInfo")
        }

        calculatePiecePriceInfoButton.setOnClickListener {
            informationDialog.show(parentFragmentManager,"BoxPriceCalculatorInfo")


        }

        return view
    }

    companion object {
        fun newInstance(): Add = Add()
        const val TAG = "Add"
    }


    /**Because some devices format number with commas which causes errors.*/
    private fun  formatResultAndCheckCommas(number: Double):String {
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

    private fun calculateWaste(calcWeight: Double, calcWaste: Double) {
        val result = (100 * calcWaste) / calcWeight
        waste.setText(formatResultAndCheckCommas(result))
        calcProductWeight.text.clear()
        calcWasteWeight.text.clear()
    }

    private fun calculatePricePerPiece(pricePerBox: Double, quantityInBox: Double){
        val result = pricePerBox/quantityInBox
        price.setText(formatResultAndCheckCommas(result))
        calcPricePerBox.text.clear()
        calcQuantityBox.text.clear()
    }

    private fun scrollUp(){
        scrollView.fullScroll(ScrollView.FOCUS_UP)
    }


    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            spinnerId -> {
                Log.i(TAG,"ITEM SELECTED DOES IT WORK??")
                chosenUnit = unitList[position]
            }
            else -> {
                Log.i(TAG,"ANNOTHER CLICKED BUT WHY? + " + parent?.id + id)
                chosenUnit = unitList[position]
            }
        }
    }
}


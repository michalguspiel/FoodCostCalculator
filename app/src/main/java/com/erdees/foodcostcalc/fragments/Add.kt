package com.erdees.foodcostcalc.fragments

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
import com.erdees.foodcostcalc.SharedPreferences
import com.erdees.foodcostcalc.getUnits
import com.erdees.foodcostcalc.model.Product
import com.erdees.foodcostcalc.viewmodel.AddViewModel
import java.math.RoundingMode
import java.text.DecimalFormat



class Add : Fragment(), AdapterView.OnItemSelectedListener {

    private var chosenUnit: String = ""
    private var unitList: MutableList<String> = mutableListOf()


    private fun showToast(context: FragmentActivity? = activity, message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, message, duration).show()
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
        val name = view.findViewById<EditText>(R.id.product_name)
        val price = view.findViewById<EditText>(R.id.product_price)
        val tax = view.findViewById<EditText>(R.id.product_tax)
        val waste = view.findViewById<EditText>(R.id.product_waste)
        val addButton = view.findViewById<Button>(R.id.addProduct)
        val calculateWasteBtn = view.findViewById<Button>(R.id.count_waste_percent_btn)
        val calcWasteWeight = view.findViewById<EditText>(R.id.waste_calc_product_waste)
        val calcProductWeight = view.findViewById<EditText>(R.id.waste_calc_product_weight)
        val calcPricePerPieceBtn = view.findViewById<Button>(R.id.count_price_per_piece_btn)
        val calcQuantityBox      = view.findViewById<EditText>(R.id.calc_quantity_box)
        val calcPricePerBox      = view.findViewById<EditText>(R.id.calc_price_per_box)

         val sharedPreferences = SharedPreferences(requireContext())

        unitList = getUnits(resources,sharedPreferences)


        /**Spinner adapter*/
        val unitSpinner = view.findViewById<Spinner>(R.id.units_spinner)
        val unitsAdapter = ArrayAdapter(requireActivity(), R.layout.support_simple_spinner_dropdown_item, unitList)
        with(unitSpinner) {
            setSelection(0, false)
            adapter = unitsAdapter
            onItemSelectedListener = this@Add
            gravity = Gravity.CENTER
            this.prompt = "Choose unit"
            id = 1
        }

        /**Functions*/


        fun  formatResultAndCheckCommas(number: Double):String {
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

        /** Calculates waste % from given product weight and waste weight,
         * formattedResultCheck works as safety for devices that formats number to
         * for example 21,21 */
        fun calculateWaste(calcWeight: Double, calcWaste: Double): String {
                val result = (100 * calcWaste) / calcWeight
                waste.setText(formatResultAndCheckCommas(result))
                calcProductWeight.text.clear()
                calcWasteWeight.text.clear()
                return formatResultAndCheckCommas(result) // returns this only in order to test it
            }


        fun calculatePricePerPiece(pricePerBox: Double,quantityInBox: Double){
          val result = pricePerBox/quantityInBox
          price.setText(formatResultAndCheckCommas(result))
          calcPricePerBox.text.clear()
          calcQuantityBox.text.clear()
        }


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

                name.text.clear()
                price.text.clear()
                tax.text.clear()
                waste.text.clear()
            }
        }

        calculateWasteBtn.setOnClickListener {
            if (calcProductWeight.text.isNotEmpty() && calcWasteWeight.text.isNotEmpty()){
                calculateWaste(calcProductWeight.text.toString().toDouble(),
                                calcWasteWeight.text.toString().toDouble())
            }
        }


        calcPricePerPieceBtn.setOnClickListener {
            if(calcPricePerBox.text.isNotEmpty()  && calcQuantityBox.text.isNotEmpty()){
                calculatePricePerPiece(calcPricePerBox.text.toString().toDouble(),
                    calcQuantityBox.text.toString().toDouble())
            }
        }


        return view
    }

    companion object {
        fun newInstance(): Add = Add()
        const val TAG = "Add"
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> {
                chosenUnit = unitList[position]

            }
            else -> {
            }
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(requireContext(), "nothing selected", Toast.LENGTH_SHORT).show()
    }

}


package com.example.foodcostcalc.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.R
import com.example.foodcostcalc.model.ProductIncluded
import com.example.foodcostcalc.fragments.dialogs.CreateDish
import com.example.foodcostcalc.model.Product
import com.example.foodcostcalc.viewmodel.AddViewModel
import java.math.RoundingMode
import java.text.DecimalFormat


class Add : Fragment() {



    private fun showToast(context: FragmentActivity? = activity, message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, message, duration) .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_add, container, false)
        val viewModel = ViewModelProvider(this).get(AddViewModel::class.java)


            /** BINDERS FOR BUTTONS AND FIELDS */
            val name                 = view.findViewById<EditText>(R.id.product_name)
            val price                = view.findViewById<EditText>(R.id.product_price)
            val tax                  = view.findViewById<EditText>(R.id.product_tax)
            val waste                = view.findViewById<EditText>(R.id.product_waste)
            val addButton            = view.findViewById<Button>(R.id.addProduct)
            val calculateWasteBtn    = view.findViewById<Button>(R.id.count_waste_percent_btn)
            val calcWasteWeight      = view.findViewById<EditText>(R.id.waste_calc_product_waste)
            val calcProductWeight    = view.findViewById<EditText>(R.id.waste_calc_product_weight)



            /** BUTTONS FUNCTIONALITY */



            addButton.setOnClickListener{
                if(name.text.isNullOrEmpty()||
                    price.text.isNullOrEmpty()||
                    tax.text.isNullOrEmpty()||
                    waste.text.isNullOrEmpty()){showToast(message = "Fill all data!")}
                else {
                    val product = Product(0,
                        name.text.toString(),
                        price.text.toString().toDouble(),
                        tax.text.toString().toDouble(),
                        waste.text.toString().toDouble()
                    )
                    viewModel.addProducts(product)

                    name.text.clear()
                    price.text.clear()
                    tax.text.clear()
                    waste.text.clear()
                }
            }


            /** Calculates waste % from given product weight and waste weight,
             * formattedResultCheck works as safety if device formats number to
             * for example 21,21 */
            calculateWasteBtn.setOnClickListener{
                if(calcProductWeight.text.isNotEmpty() && calcWasteWeight.text.isNotEmpty()){
                    val calcWeight = calcProductWeight.text.toString().toDouble()
                    val calcWaste = calcWasteWeight.text.toString().toDouble()
                    val result = (100 * calcWaste) / calcWeight
                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.CEILING
                    val formattedResult = df.format(result)
                    var formattedResultCheck = ""
                    for(eachChar in formattedResult){
                        if(eachChar.equals(",")) formattedResultCheck += "."
                        else formattedResultCheck += eachChar
                    }
                    waste.setText(formattedResultCheck)
                    calcProductWeight.text.clear()
                    calcWasteWeight.text.clear()
                }
            }



        return view
    }
    companion object {
        fun newInstance():Add = Add()
        const val TAG = "Add"
    }

}


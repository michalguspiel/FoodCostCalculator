package com.erdees.foodcostcalc.fragments.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.fragments.Add

class InformationDialog : DialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.information_dialog, container, false)

        val cancelButton: Button = view.findViewById(R.id.cancel_button)
        val text: TextView = view.findViewById(R.id.content)


        when(this.tag) {
            AddProductToHalfProduct.TAG -> text.text = resources.getString(R.string.information_add_product_to_half_product)
            Add.TAG -> text.text = "Provide all of the necessary data in order to add product to your list, when adding product with per piece unit provide price per each piece," +
                    " in case You don't know waste percentage or price per piece of product use tools on to bottom of this screen."
            "WasteCalculatorInfo" -> text.text = "To learn the waste percentage of product, weight product before processing it and amount of waste afterwards. Press calculate and waste percentage will appear in waste percent field."
                "BoxPriceCalculatorInfo" -> text.text = "In Box price pass how much do You pay per box, in quantity pass how many pieces there is in box. Press CALCULATE Price per piece will appear in price field."
            else -> text.text = "test"
        }

        cancelButton.setOnClickListener { this.dismiss() }



        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    companion object {
        fun newInstance(): InformationDialog = InformationDialog()
        const val TAG = "InformationDialog"

    }
}
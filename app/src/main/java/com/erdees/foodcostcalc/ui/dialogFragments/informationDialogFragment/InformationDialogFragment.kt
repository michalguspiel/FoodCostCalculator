package com.erdees.foodcostcalc.ui.dialogFragments.informationDialogFragment

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
import com.erdees.foodcostcalc.ui.screens.createProduct.AddFragment
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.addProductToHalfProductDialogFragment.AddProductToHalfProductFragment

class InformationDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.information_dialog, container, false)

        val cancelButton: Button = view.findViewById(R.id.cancel_button)
        val text: TextView = view.findViewById(R.id.content)

        when (this.tag) {
            AddProductToHalfProductFragment.TAG -> text.text =
                resources.getString(R.string.information_add_product_to_half_product)
            AddFragment.TAG -> text.text =
                resources.getString(R.string.information_add_fragment_general)
            "WasteCalculatorInfo" -> text.text =
                resources.getString(R.string.information_waste_calculator)
            "BoxPriceCalculatorInfo" -> text.text =
                resources.getString(R.string.information_price_per_piece_calculator)
            else -> text.text = resources.getString(R.string.information_add_fragment_general)
        }
        cancelButton.setOnClickListener { this.dismiss() }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    companion object {
        fun newInstance(): InformationDialogFragment = InformationDialogFragment()
        const val TAG = "InformationDialogFragment"
    }
}

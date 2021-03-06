package com.example.foodcostcalc.fragments.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.foodcostcalc.R

class InformationDialog : DialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.information_dialog, container, false)

        val cancelButton: Button = view.findViewById<Button>(R.id.cancel_button)
        val text: TextView = view.findViewById<TextView>(R.id.content)


        when(this.tag) {
            AddProductToHalfProduct.TAG -> text.text = resources.getString(R.string.information_add_product_to_half_product)
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
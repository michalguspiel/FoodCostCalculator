package com.example.foodcostcalc.fragments.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.R
import com.example.foodcostcalc.viewmodel.AddViewModel

class SearchDialog : DialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.search_dialog, container, false)

        /** initialize ui with viewmodel*/
        val viewModel = ViewModelProvider(this).get(AddViewModel::class.java)

        /** binders*/
        val searchBtn = view.findViewById<Button>(R.id.search_dialog_button)
        val searchTextField = view.findViewById<TextView>(R.id.search_field)

        /** BUTTON LOGIC*/
        searchBtn.setOnClickListener {
            if (searchTextField.text.isNotEmpty()) {

                this.dismiss()
            } else Toast.makeText(activity, "Can't search for nothing!", Toast.LENGTH_SHORT).show()
        }


        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view


    }

    companion object {
        fun newInstance(): SearchDialog =
                SearchDialog()

        const val TAG = "SearchDialog"
    }


}

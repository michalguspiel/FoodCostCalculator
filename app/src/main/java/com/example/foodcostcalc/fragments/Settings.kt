package com.example.foodcostcalc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.foodcostcalc.R
import com.example.foodcostcalc.SharedPreferences

class Settings : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.settings, container, false)

        /**Binders*/
        val saveBtn             = view.findViewById<Button>(R.id.save_settings_button)
        val editMarginEditText  = view.findViewById<EditText>(R.id.default_margin_edittext)
        val editTaxEditText     = view.findViewById<EditText>(R.id.default_dish_tax_edit_text)
        val metricCheckBox      = view.findViewById<CheckBox>(R.id.checkBoxMetric)
        val usaCheckBox         = view.findViewById<CheckBox>(R.id.checkBoxUS)


        val sharedPreference = SharedPreferences(requireContext())

        val marginAsString = sharedPreference.getValueString("margin")
        val taxAsString = sharedPreference.getValueString("tax")
        if(taxAsString != null) editTaxEditText.setText(taxAsString)
        if(marginAsString != null) editMarginEditText.setText(marginAsString)

        val isMetricChecked = sharedPreference.getValueBoolean("metric",true)
        val isUsaChecked = sharedPreference.getValueBoolean("usa",false)

        metricCheckBox.isChecked = isMetricChecked
        usaCheckBox.isChecked = isUsaChecked

        saveBtn.setOnClickListener{
            val margin = editMarginEditText.text.toString()
            val tax = editTaxEditText.text.toString()
            sharedPreference.save("margin",margin)
            sharedPreference.save("tax",tax)
            if(metricCheckBox.isChecked) sharedPreference.save("metric",true)
            else sharedPreference.save("metric",false)
            if(usaCheckBox.isChecked) sharedPreference.save("usa",true)
            else sharedPreference.save("usa",false)

            Toast.makeText(requireContext(),"Settings saved!",Toast.LENGTH_SHORT).show()
        }




        return view

    }

    companion object {
        fun newInstance(): Settings = Settings()
        const val TAG = "Settings"
    }
}
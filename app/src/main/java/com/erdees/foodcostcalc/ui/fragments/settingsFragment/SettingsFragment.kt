package com.erdees.foodcostcalc.ui.fragments.settingsFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.erdees.foodcostcalc.R

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class SettingsFragment : Fragment() {

    private lateinit var editMarginEditText: EditText
    private lateinit var editTaxEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.settings, container, false)

        /**Binders*/
        val saveBtn             = view.findViewById<Button>(R.id.save_settings_button)
        editMarginEditText  = view.findViewById(R.id.default_margin_edittext)
        editTaxEditText     = view.findViewById(R.id.default_dish_tax_edit_text)
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
            if(marginAndTaxAreValid()) {
                val margin = editMarginEditText.text.toString()
                val tax = editTaxEditText.text.toString()
                sharedPreference.save("margin", margin)
                sharedPreference.save("tax", tax)
                if (metricCheckBox.isChecked) sharedPreference.save("metric", true)
                else sharedPreference.save("metric", false)
                if (usaCheckBox.isChecked) sharedPreference.save("usa", true)
                else sharedPreference.save("usa", false)

                Toast.makeText(requireContext(), "SettingsFragment saved!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(requireContext(),"Enter values to default tax and margin.",Toast.LENGTH_SHORT).show()
        }

        return view

    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
        const val TAG = "SettingsFragment"
    }

    private fun marginAndTaxAreValid() : Boolean {
        return (!editMarginEditText.text.isNullOrBlank() && editMarginEditText.text.toString() != "." &&
                !editTaxEditText.text.isNullOrBlank() && editTaxEditText.text.toString() != ".")
    }

}
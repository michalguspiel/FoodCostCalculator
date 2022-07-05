package com.erdees.foodcostcalc.ui.fragments.settingsFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.SettingsBinding
import com.erdees.foodcostcalc.utils.Constants

class SettingsFragment : Fragment() {

    private var _binding: SettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        val sharedPreference = SharedPreferences(requireContext())
        val marginAsString = sharedPreference.getValueString(Constants.MARGIN)
        val taxAsString = sharedPreference.getValueString(Constants.TAX)
        if (taxAsString != null) binding.defaultDishTaxEditText.setText(taxAsString)
        if (marginAsString != null) binding.defaultMarginEdittext.setText(marginAsString)

        val isMetricChecked = sharedPreference.getValueBoolean(Constants.METRIC, true)
        val isUsaChecked = sharedPreference.getValueBoolean(Constants.USA, false)

        binding.checkBoxMetric.isChecked = isMetricChecked
        binding.checkBoxUS.isChecked = isUsaChecked

        binding.saveSettingsButton.setOnClickListener {
            if (marginAndTaxAreValid()) {
                val margin = binding.defaultMarginEdittext.text.toString()
                val tax = binding.defaultDishTaxEditText.text.toString()
                sharedPreference.save(Constants.MARGIN, margin)
                sharedPreference.save(Constants.TAX, tax)
                if (binding.checkBoxMetric.isChecked) sharedPreference.save(Constants.METRIC, true)
                else sharedPreference.save(Constants.METRIC, false)
                if (binding.checkBoxUS.isChecked) sharedPreference.save(Constants.USA, true)
                else sharedPreference.save(Constants.USA, false)

                Toast.makeText(requireContext(), R.string.settings_saved_prompt, Toast.LENGTH_SHORT)
                    .show()
            } else Toast.makeText(
                requireContext(),
                R.string.enter_default_tax_and_margin,
                Toast.LENGTH_SHORT
            ).show()
        }
        return view
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
        const val TAG = "SettingsFragment"
    }

    private fun marginAndTaxAreValid(): Boolean {
        return (!binding.defaultMarginEdittext.text.isNullOrBlank() && binding.defaultMarginEdittext.text.toString() != "." &&
                !binding.defaultDishTaxEditText.text.isNullOrBlank() && binding.defaultDishTaxEditText.text.toString() != ".")
    }
}
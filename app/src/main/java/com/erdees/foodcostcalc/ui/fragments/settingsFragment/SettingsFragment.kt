package com.erdees.foodcostcalc.ui.fragments.settingsFragment

import android.icu.util.Currency
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.SettingsBinding
import com.erdees.foodcostcalc.ui.activities.mainActivity.MainActivity
import com.erdees.foodcostcalc.utils.Constants

class SettingsFragment : Fragment(), AdapterView.OnItemClickListener {

  private var _binding: SettingsBinding? = null
  private val binding get() = _binding!!
  private lateinit var viewModel: SettingsViewModel

  private var chosenCurrency: Currency? = null
  private var currencies: MutableSet<Currency> = mutableSetOf()

  private fun getCurrencies(): MutableSet<Currency> {
    val pattern = """\([^)]*\)"""
    return Currency
      .getAvailableCurrencies()
      .filter { !it.displayName.contains(Regex(pattern)) }
      .filter { !it.displayName.contains("Unknown") }
      .sortedBy { it.displayName }
      .toMutableSet()
  }

  override fun onResume() {
    currencies = getCurrencies()
    currencies.find { it == viewModel.getDefaultCurrency() }?.let {
      Log.i(TAG, "currency found: ${it.displayName}")
      chosenCurrency = it
    } ?: run {
      chosenCurrency = currencies.first()
    }
    setCurrenciesSpinner()
    super.onResume()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = SettingsBinding.inflate(inflater, container, false)
    val view = binding.root
    setToolbarTitle()
    viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
    binding.aboutTextView.text =
      getString(R.string.app_info_template, BuildConfig.VERSION_NAME, "Michał Guśpiel")

    binding.defaultDishTaxEditText.setText(viewModel.getDefaultTax())
    binding.defaultMarginEdittext.setText(viewModel.getDefaultMargin())

    binding.checkBoxMetric.isChecked = viewModel.getIsMetricUsed()
    binding.checkBoxImperial.isChecked = viewModel.getIsImperialUsed()

    binding.saveSettingsButton.setOnClickListener {
      val result = viewModel.saveSettings(
        binding.defaultMarginEdittext.text.toString(),
        binding.defaultDishTaxEditText.text.toString(),
        binding.checkBoxMetric.isChecked,
        binding.checkBoxImperial.isChecked,
        chosenCurrency?.currencyCode
      )
      val message = when (result) {
        Result.SUCCESS -> R.string.settings_saved_prompt
        Result.FAILED_UNIT_SYSTEM_MISSING -> R.string.settings_failed_units_missing
        Result.FAILED_TAX_MISSING -> R.string.settings_failed_tax_missing
        Result.FAILED_MARGIN_MISSING -> R.string.settings_failed_margin_missing
      }
      Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    return view
  }

  private fun setCurrenciesSpinner() {
    val currenciesAdapter =
      ArrayAdapter(
        requireActivity(),
        R.layout.dropdown_item,
        currencies.map { it.displayName })
    with(binding.currenciesSpinner) {
      setAdapter(currenciesAdapter)
      setText(chosenCurrency?.displayName, false)
      onItemClickListener = this@SettingsFragment
      gravity = Gravity.START
      id = Constants.CURRENCIES_SPINNER_ID
    }
  }

  private fun setToolbarTitle() {
    (activity as MainActivity).setToolBarTitle(getString(R.string.settings))
  }

  companion object {
    fun newInstance(): SettingsFragment = SettingsFragment()
    const val TAG = "SettingsFragment"
  }

  override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    chosenCurrency = currencies.elementAt(position)
    Log.i(TAG, "onItemClick: ${chosenCurrency?.displayName} , ${parent?.id}")
  }
}

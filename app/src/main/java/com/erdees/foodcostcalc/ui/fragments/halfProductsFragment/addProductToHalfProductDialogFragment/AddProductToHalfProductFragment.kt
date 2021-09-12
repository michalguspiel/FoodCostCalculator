package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.addProductToHalfProductDialogFragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.*
import com.erdees.foodcostcalc.ui.dialogFragments.informationDialogFragment.InformationDialogFragment
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.ProductIncludedInHalfProductModel
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.UnitsUtils.getPerUnitAsDescription
import com.erdees.foodcostcalc.utils.UnitsUtils.getUnitType
import com.erdees.foodcostcalc.utils.Utils.changeUnitList
import com.erdees.foodcostcalc.utils.ViewUtils.hideKeyboard

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class AddProductToHalfProductFragment : DialogFragment(), AdapterView.OnItemSelectedListener {
    private val PRODUCT_SPINNER_ID = 1
    private val HALFPRODUCT_SPINNER_ID = 2
    private val UNIT_SPINNER_ID = 3
    private var productPosition: Int? = null
    private var halfProductPosition: Int? = null
    private val unitList = arrayListOf<String>()
    private var chosenUnit: String = ""

    private var halfProductUnit = ""
    private var chosenProductName = ""
    private var halfProductUnitType = ""

    private var unitType = ""

    private var metricAsBoolean = true
    private var usaAsBoolean = true

    lateinit var fragmentViewModel: AddProductToHalfProductFragmentViewModel
    private lateinit var unitAdapter: ArrayAdapter<*>
    private lateinit var unitSpinner: Spinner
    lateinit var weightPerPieceEditText: EditText

    var isProductPiece: Boolean = false
    var isHalfProductPiece: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.add_product_to_half_product, container, false)
        fragmentViewModel =
            ViewModelProvider(this).get(AddProductToHalfProductFragmentViewModel::class.java)


        /**Binders*/
        val addProductButton = view.findViewById<ImageButton>(R.id.add_product_to_halfproduct_btn)
        val weightEditTextField = view.findViewById<EditText>(R.id.product_weight_in_half_product)
        weightEditTextField.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) view.hideKeyboard()
        }
        val halfProductSpinner = view.findViewById<Spinner>(R.id.half_product_spinner)
        val productSpinner = view.findViewById<Spinner>(R.id.product_spinner)
        val infoButton = view.findViewById<ImageButton>(R.id.calculateWasteInfoButton)
        weightPerPieceEditText = view.findViewById(R.id.weight_for_piece)
        weightPerPieceEditText.visibility = View.GONE // Initially off

        unitSpinner = view.findViewById(R.id.unit_spinner)


        /** Get the data about unit settings from shared preferences.
         * true means that user uses certain units.
         * metricAsBoolean is set as true because something needs to be chosen in order for app to work.*/
        val sharedPreferences = SharedPreferences(requireContext())
        metricAsBoolean = sharedPreferences.getValueBoolean("metric", true)
        usaAsBoolean = sharedPreferences.getValueBoolean("usa", false)


        /**Spinner Adapters!*/
        val halfProductsList = mutableListOf<String>()
        fragmentViewModel.readAllHalfProductModelData.observe(
            viewLifecycleOwner,
            {
                it.forEach { halfProduct -> halfProductsList.add(halfProduct.name) }
                if (this.isOpenFromHalfProductAdapter()) {
                    val halfProductToSelect = fragmentViewModel.getHalfProductToDialog().value
                    val positionToSelect = halfProductsList.indexOf(halfProductToSelect!!.name)
                    Log.i("TEST", positionToSelect.toString())
                    halfProductSpinner.setSelection(positionToSelect)
                }
            })
        val halfProductAdapter =
            ArrayAdapter(requireActivity(), R.layout.spinner_layout, halfProductsList)
        with(halfProductSpinner) {
            adapter = halfProductAdapter
            id = HALFPRODUCT_SPINNER_ID
            onItemSelectedListener = this@AddProductToHalfProductFragment
            prompt = "choose half product"
            gravity = Gravity.CENTER
        }
        halfProductAdapter.notifyDataSetChanged()

        val productList = mutableListOf<String>()
        fragmentViewModel.readAllProductModelData.observe(
            viewLifecycleOwner,
            { it.forEach { product -> productList.add(product.name) } })
        val productAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_layout, productList)
        with(productSpinner)
        {
            adapter = productAdapter
            setSelection(0, false)
            onItemSelectedListener = this@AddProductToHalfProductFragment
            prompt = "Select product"
            gravity = Gravity.CENTER
            id = PRODUCT_SPINNER_ID
        }
        productAdapter.notifyDataSetChanged()

        unitAdapter =
            ArrayAdapter(requireActivity(), R.layout.support_simple_spinner_dropdown_item, unitList)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        with(unitSpinner) {
            adapter = unitAdapter
            setSelection(0, false)
            onItemSelectedListener = this@AddProductToHalfProductFragment
            prompt = "Select unit"
            gravity = Gravity.CENTER
            id = UNIT_SPINNER_ID
        }


        /**OBSERVING 'LIVEDATA' FROM ADDVIEWMODEL
         *  WHICH OBSERVES 'LIVEDATA' IN REPOSITORY
         *  WHICH OBSERVES 'LIVEDATA' FROM DAO*/

        fragmentViewModel.readAllProductModelData.observe(
            viewLifecycleOwner,
            { products ->
                productAdapter.clear()
                products.forEach { product ->
                    productAdapter.add(product.name)
                    productAdapter.notifyDataSetChanged()
                }
            })

        fragmentViewModel.readAllHalfProductModelData.observe(
            viewLifecycleOwner,
            { halfProducts ->
                halfProductAdapter.clear()
                halfProducts.forEach { halfProduct ->
                    halfProductAdapter.add(halfProduct.name)
                    halfProductAdapter.notifyDataSetChanged()
                }
            })
        /**Button Logic*/

        infoButton.setOnClickListener {
            InformationDialogFragment().show(
                this.parentFragmentManager,
                TAG
            )
        }


        addProductButton.setOnClickListener {

            if (eitherOfSpinnersIsEmpty()) {
                showToast(message = "You must pick half product and product.")
                return@setOnClickListener
            } else if (weightEditTextField.text.isNullOrEmpty() || weightEditTextField.text.toString() == ".") {
                showToast(message = "You can't add product without weight.")
                return@setOnClickListener
            } else {
                val chosenHalfProduct =
                    fragmentViewModel.readAllHalfProductModelData.value?.get(
                        halfProductPosition!!
                    )
                val chosenProduct =
                    fragmentViewModel.readAllProductModelData.value?.get(productPosition!!)
                val weight = weightEditTextField.text.toString().toDouble()
                if (!isHalfProductPiece && isProductPiece && weightPerPieceEditText.text.isEmpty()) {
                    showToast(message = "You need to provide $halfProductUnitType of this product!")
                } else {
                    fragmentViewModel.addProductIncludedInHalfProduct(
                        ProductIncludedInHalfProductModel(
                            0,
                            chosenProduct!!,
                            chosenHalfProduct!!,
                            chosenHalfProduct.halfProductId,
                            weight,
                            chosenUnit,
                            if (!isHalfProductPiece && isProductPiece) weightPerPieceEditText.text.toString()
                                .toDouble() else 1.0
                        )
                    )
                    weightEditTextField.text.clear()
                    showToast(
                        message = "${
                            fragmentViewModel.readAllProductModelData.value?.get(
                                productPosition!!
                            )?.name
                        } added."
                    )
                }
            }

        }


        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }


    companion object {
        fun newInstance(): AddProductToHalfProductFragment =
            AddProductToHalfProductFragment()

        const val TAG = "AddProductToHalfProductFragment"
    }


    private fun eitherOfSpinnersIsEmpty(): Boolean {
        return (fragmentViewModel.readAllProductModelData.value.isNullOrEmpty() || fragmentViewModel.readAllHalfProductModelData.value.isNullOrEmpty())

    }

    private fun showToast(
        context: FragmentActivity? = activity,
        message: String,
        duration: Int = Toast.LENGTH_LONG
    ) {
        Toast.makeText(context, message, duration).show()
    }

    private fun setTextField() {
        if (isProductPiece && !isHalfProductPiece) {
            weightPerPieceEditText.visibility = View.VISIBLE
            weightPerPieceEditText.hint =
                "$chosenProductName ${getPerUnitAsDescription(halfProductUnit)}."
        } else weightPerPieceEditText.visibility = View.GONE
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> {
                productPosition = position
                val chosenProduct =
                    fragmentViewModel.readAllProductModelData.value?.get(position)
                unitType = getUnitType(
                    chosenProduct?.unit
                )
                chosenProductName = chosenProduct!!.name
                unitList.changeUnitList(unitType, metricAsBoolean, usaAsBoolean)
                unitAdapter.notifyDataSetChanged()
                unitSpinner.setSelection(0, false)
                chosenUnit = unitList.first()
                unitSpinner.setSelection(0) // when the product is chosen first units got chosen immediately
                isProductPiece = fragmentViewModel
                    .readAllProductModelData.value!![productPosition!!].unit == "per piece"
                setTextField()
            }
            2 -> {
                halfProductPosition = position
                val thisHalfProduct = fragmentViewModel
                    .readAllHalfProductModelData.value!![halfProductPosition!!]
                halfProductUnit = thisHalfProduct.halfProductUnit
                isHalfProductPiece = thisHalfProduct.halfProductUnit == "per piece"
                halfProductUnitType = getUnitType(thisHalfProduct.halfProductUnit)
                setTextField()
            }
            else -> {
                chosenUnit = unitList[position]
                Log.i("test", chosenUnit)
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        this.dismiss()
    }

    private fun isOpenFromHalfProductAdapter(): Boolean {
        return this.tag == "HalfProductFragmentRecyclerAdapter"
    }
}
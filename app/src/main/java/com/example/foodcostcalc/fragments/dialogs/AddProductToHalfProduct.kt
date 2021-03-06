package com.example.foodcostcalc.fragments.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.*
import com.example.foodcostcalc.model.ProductIncludedInHalfProduct
import com.example.foodcostcalc.viewmodel.AddViewModel
import com.example.foodcostcalc.viewmodel.HalfProductsViewModel

class AddProductToHalfProduct : DialogFragment(), AdapterView.OnItemSelectedListener {
    private val PRODUCT_SPINNER_ID = 1
    private val HALFPRODUCT_SPINNER_ID = 2
    private val UNIT_SPINNER_ID = 3
    private var productPosition: Int? = null
    private var halfProductPosition: Int? = null
    private val unitList = arrayListOf<String>() // list for units, to populate spinner
    private var chosenUnit: String = ""

    private var halfProductUnit     = ""
    private var chosenProductName   = ""
    private var halfProductUnitType = ""

    /**Holder for type of units*/
    private var unitType = ""

    /**Holder for booleans*/
    private var metricAsBoolean = true
    private var usaAsBoolean = true

    /** Initialized here so it can be called outside of 'onCreateView' */
    lateinit var viewModel: ViewModel
    lateinit var halfProductViewModel: ViewModel
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
        halfProductViewModel = ViewModelProvider(this).get(HalfProductsViewModel::class.java)
        viewModel = ViewModelProvider(this).get(AddViewModel::class.java)
        val hpViewModel = halfProductViewModel as HalfProductsViewModel


        /**Binders*/
        val addProductButton = view.findViewById<ImageButton>(R.id.add_product_to_halfproduct_btn)
        val weightEditTextField = view.findViewById<EditText>(R.id.product_weight_in_half_product)
        val halfProductSpinner = view.findViewById<Spinner>(R.id.half_product_spinner)
        val productSpinner = view.findViewById<Spinner>(R.id.product_spinner)
        val infoButton = view.findViewById<ImageButton>(R.id.info_button)
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
        (halfProductViewModel as HalfProductsViewModel).readAllHalfProductData.observe(
            viewLifecycleOwner,
            Observer { it.forEach { halfProduct -> halfProductsList.add(halfProduct.name) } })
        val halfProductAdapter =
            ArrayAdapter(requireActivity(), R.layout.spinner_layout, halfProductsList)
        with(halfProductSpinner) {
            adapter = halfProductAdapter
            id = HALFPRODUCT_SPINNER_ID
            setSelection(0, false)
            onItemSelectedListener = this@AddProductToHalfProduct
            prompt = "choose half product"
            gravity = Gravity.CENTER
        }
        halfProductAdapter.notifyDataSetChanged()

        val productList = mutableListOf<String>()
        (viewModel as AddViewModel).readAllProductData.observe(
            viewLifecycleOwner,
            Observer { it.forEach { product -> productList.add(product.name) } })
        val productAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_layout, productList)
        with(productSpinner)
        {
            adapter = productAdapter
            setSelection(0, false)
            onItemSelectedListener = this@AddProductToHalfProduct
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
            onItemSelectedListener = this@AddProductToHalfProduct
            prompt = "Select unit"
            gravity = Gravity.CENTER
            id = UNIT_SPINNER_ID
        }


        /**OBSERVING 'LIVEDATA' FROM ADDVIEWMODEL
         *  WHICH OBSERVES 'LIVEDATA' IN REPOSITORY
         *  WHICH OBSERVES 'LIVEDATA' FROM DAO*/

        (viewModel as AddViewModel).readAllProductData.observe(
            viewLifecycleOwner,
            Observer { products ->
                productAdapter.clear()
                products.forEach { product ->
                    productAdapter.add(product.name)
                    productAdapter.notifyDataSetChanged()
                }
            })

        (halfProductViewModel as HalfProductsViewModel).readAllHalfProductData.observe(
            viewLifecycleOwner,
            Observer { halfProducts ->
                halfProductAdapter.clear()
                halfProducts.forEach { halfProduct ->
                    halfProductAdapter.add(halfProduct.name)
                    halfProductAdapter.notifyDataSetChanged()
                }
            })
        /**Button Logic*/

        infoButton.setOnClickListener { InformationDialog().show(this.parentFragmentManager, TAG) }


        addProductButton.setOnClickListener {
            if (weightEditTextField.text.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "You can't add product without weight.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val chosenHalfProduct =
                    hpViewModel.readAllHalfProductData.value?.get(
                        halfProductPosition!!
                    )
                val chosenProduct =
                    (viewModel as AddViewModel).readAllProductData.value?.get(productPosition!!)
                val weight = weightEditTextField.text.toString().toDouble()
                if (!isHalfProductPiece && isProductPiece && weightPerPieceEditText.text.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "You need to provide $halfProductUnitType of this product!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    hpViewModel.addProductIncludedInHalfProduct(
                        ProductIncludedInHalfProduct(
                            0,
                            chosenProduct!!,
                            chosenHalfProduct!!,
                            chosenHalfProduct.halfProductId,
                            weight,
                            chosenUnit,
                            if (!isHalfProductPiece && isProductPiece) weightPerPieceEditText.text.toString().toDouble() else 1.0
                        )
                    )


                    weightEditTextField.text.clear()
                    Toast.makeText(
                        requireContext(),
                        "${(viewModel as AddViewModel).readAllProductData.value?.get(productPosition!!)?.name} added.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }


        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }


    companion object {
        fun newInstance(): AddProductToHalfProduct =
            AddProductToHalfProduct()

        const val TAG = "AddProductToHalfProduct"
    }


    fun setTextField() {
        if (isProductPiece && !isHalfProductPiece) {
            weightPerPieceEditText.visibility = View.VISIBLE
            weightPerPieceEditText.hint = "$chosenProductName ${perUnitToAbbreviation(halfProductUnit)}."
        } else weightPerPieceEditText.visibility = View.GONE
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> {
                productPosition = position
                val chosenProduct =
                    (viewModel as AddViewModel).readAllProductData.value?.get(position)
                unitType = setAdapterList(
                    chosenProduct?.unit
                )
                chosenProductName = chosenProduct!!.name
                unitList.changeUnitList(unitType, metricAsBoolean, usaAsBoolean)
                unitAdapter.notifyDataSetChanged()
                unitSpinner.setSelection(0, false)
                chosenUnit = unitList.first()
                unitSpinner.setSelection(0) // when the product is chosen first units got chosen immediately
                isProductPiece = (viewModel as AddViewModel)
                    .readAllProductData.value!![productPosition!!].unit == "per piece"
                setTextField()
            }
            2 -> {
                halfProductPosition = position
                val thisHalfProduct = (halfProductViewModel as HalfProductsViewModel)
                    .readAllHalfProductData.value!![halfProductPosition!!]
                halfProductUnit = thisHalfProduct.halfProductUnit
                isHalfProductPiece = thisHalfProduct.halfProductUnit == "per piece"
                halfProductUnitType = setAdapterList(thisHalfProduct.halfProductUnit)
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
}
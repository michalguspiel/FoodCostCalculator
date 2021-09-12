package com.erdees.foodcostcalc.ui.fragments.productsFragment.editProductDialogFragment

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.*
import com.erdees.foodcostcalc.ui.dialogFragments.areYouSureFragment.AreYouSure
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.ProductIncludedInHalfProductModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.UnitsUtils.filterVol
import com.erdees.foodcostcalc.utils.UnitsUtils.filterWeight
import com.erdees.foodcostcalc.utils.Utils.getUnits
import com.erdees.foodcostcalc.utils.ViewUtils.hideKeyboard

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class EditProductFragment : DialogFragment(), AdapterView.OnItemClickListener {

    private var unitPosition: Int? = null
    private var productId: Long? = null
    private var unitList: MutableList<String> = mutableListOf()
    private lateinit var unitSpinner: AutoCompleteTextView
    private val spinnerId = 1
    private lateinit var fragmentViewModel: EditProductFragmentViewModel
    private lateinit var sharedPreferences: SharedPreferences


    private lateinit var name: EditText
    private lateinit var price: EditText
    private lateinit var tax: EditText
    private lateinit var waste: EditText

    override fun onResume() {
        unitList = getUnits(resources, sharedPreferences)
        unitList = when (productModelPassedFromAdapter.unit) {
            "per piece" -> mutableListOf("per piece")
            "per kilogram" -> unitList.filterWeight()
            "per liter" -> unitList.filterVol()
            "per pound" -> unitList.filterWeight()
            "per gallon" -> unitList.filterVol()
            else -> mutableListOf("error!")
        }

        val unitsAdapter = ArrayAdapter(
            requireActivity(),
            R.layout.dropdown_item,
            unitList
        )
        with(unitSpinner) {
            setAdapter(unitsAdapter)
            onItemClickListener = this@EditProductFragment
            gravity = Gravity.CENTER
            id = spinnerId
        }

        super.onResume()
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_edit_product, container, false)

        fragmentViewModel = ViewModelProvider(this).get(EditProductFragmentViewModel::class.java)
        sharedPreferences = SharedPreferences(requireContext())

        /** Binders*/
         name = view.findViewById(R.id.edit_product_name)
        price = view.findViewById(R.id.edit_product_price)
        tax = view.findViewById(R.id.edit_product_tax)
        waste = view.findViewById(R.id.edit_product_waste)
        unitSpinner = view.findViewById(R.id.spinner_edit_product)
        unitSpinner.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) view.hideKeyboard()
        }
        val saveButton = view.findViewById<Button>(R.id.save_changes_button)
        val deleteButton = view.findViewById<Button>(R.id.delete_product_button)

        /**Send data about which dishModel is being edited
         * so .setPosition(index of this dishModel in main list)*/
        fragmentViewModel.getProducts().observe(viewLifecycleOwner, { products ->
            fragmentViewModel.setPosition(products.indexOf(productModelPassedFromAdapter))
        })

        /** empty lists which gets populated by every 'ProductIncluded' and
         * 'ProductIncludedInHalfProductModel' that has the same ID as edited product. */
        var productIncludedList = listOf<ProductIncluded>()
        var productIncludedInHalfProductList = listOf<ProductIncludedInHalfProductModel>()

        /** OBSERVE DATA FROM VIEWMODEL
         * Flag works as a safety, if flag is set to false that means that
         * product is deleted and doesn't exist
         * so dialog is deleted and flag set to true so it will load information about
         * next chosen product
         *
         * Also gets information about every product included with same productID and
         * saves it as list so the product will be edited in every dishModel as well
         *
         * All of this needs to be inside Observer because otherwise dialog isn't closed when
         * product is deleted.*/

        fragmentViewModel.getFlag().observe(viewLifecycleOwner, { flag ->
            if (flag == false) {
                this.dismiss()
                fragmentViewModel.setFlag(true)
            } else if (flag == true) {
                saveProductIdOfEditedProduct(productModelPassedFromAdapter)
                setDialogFieldsAccordinglyToProductEdited(productModelPassedFromAdapter)
                /**GET LIST OF PRODUCTS INCLUDED
                 * WITH THE SAME ID AS productID
                 * AND SAVE IT IN 'productIncludedList
                 * IT NEEDS TO BE HERE BECAUSE IF FLAG IS FALSE productId DOESN'T EXIST*/
                fragmentViewModel.getCertainProductsIncluded(productId!!).observe(
                    viewLifecycleOwner,
                    { listOfProducts ->
                        productIncludedList = listOfProducts
                    })
                fragmentViewModel.getCertainProductsIncludedInHalfProduct(productId!!)
                    .observe(viewLifecycleOwner,
                        { listOfProducts ->
                            productIncludedInHalfProductList = listOfProducts
                        })
            }
        })

        /** BUTTON LOGIC*/
        saveButton.setOnClickListener {
            if(allFieldsAreLegit()) {
                if (unitPosition == null) unitPosition =
                    unitList.indexOf(productModelPassedFromAdapter.unit)
                val productToChange = ProductModel(
                    productId!!,
                    name.text.toString(),
                    price.text.toString().toDouble(),
                    tax.text.toString().toDouble(),
                    waste.text.toString().toDouble(),
                    unitList[unitPosition!!]
                )
                fragmentViewModel.editProduct(productToChange)
                changeEveryProductIncluded(productToChange, productIncludedList)
                changeEveryProductIncludedInHalfProduct(
                    productToChange,
                    productIncludedInHalfProductList
                )
                Thread.sleep(100) // This is here because otherwise dialog gets closed before all viewmodel functions are called
                this.dismiss()
            }
            else Toast.makeText(requireContext(),"Fields must not be empty!",Toast.LENGTH_SHORT).show()
            }

        deleteButton.setOnClickListener {
            AreYouSure().show(childFragmentManager, TAG)
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        return view
    }


    companion object {
        const val TAG = "EditProductFragment"

        /**Position of Edited product */
        lateinit var productModelPassedFromAdapter: ProductModel
    }

    private fun allFieldsAreLegit():Boolean{
        return (!name.text.isNullOrBlank() &&
                !price.text.isNullOrBlank() && price.text.toString() != "." &&
                !tax.text.isNullOrBlank() && tax.text.toString() != "." &&
                !waste.text.isNullOrBlank() && waste.text.toString() != "." )
    }

    private fun changeEveryProductIncluded(
        productModelToChange: ProductModel,
        listOfProductsToChange: List<ProductIncluded>
    ) {
        listOfProductsToChange.forEach {
            fragmentViewModel.editProductsIncluded(
                ProductIncluded(
                    it.productIncludedId,
                    productModelToChange,
                    it.dishOwnerId,
                    it.dishModel,
                    it.productOwnerId,
                    it.weight,
                    it.weightUnit
                )
            )
        }
    }

    private fun changeEveryProductIncludedInHalfProduct(
        productModelToChange: ProductModel,
        listToChange: List<ProductIncludedInHalfProductModel>
    ) {
        listToChange.forEach {
            fragmentViewModel.editProductIncludedInHalfProduct(
                ProductIncludedInHalfProductModel(
                    it.productIncludedInHalfProductId,
                    productModelToChange,
                    it.halfProductModel,
                    it.halfProductHostId,
                    it.weight,
                    it.weightUnit,
                    it.weightOfPiece
                )
            )
        }
    }

    private fun saveProductIdOfEditedProduct(productModelEdited: ProductModel) {
        productId = productModelEdited.productId
    }

    private fun setDialogFieldsAccordinglyToProductEdited(productModelPassedFromAdapter: ProductModel) {
        name.setText(productModelPassedFromAdapter.name)
        price.setText(productModelPassedFromAdapter.pricePerUnit.toString())
        tax.setText(productModelPassedFromAdapter.tax.toString())
        waste.setText(productModelPassedFromAdapter.waste.toString())
        unitSpinner.setText(productModelPassedFromAdapter.unit, false)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> {
                unitPosition = position
            }
            else -> {
                unitPosition = position

            }
        }
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}
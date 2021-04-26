package com.erdees.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.SharedFunctions
import com.erdees.foodcostcalc.SharedFunctions.abbreviateUnit
import com.erdees.foodcostcalc.SharedFunctions.abbreviateUnitWithPer
import com.erdees.foodcostcalc.SharedFunctions.formatPrice
import com.erdees.foodcostcalc.SharedFunctions.getBasicRecipeAsPercentageOfTargetRecipe
import com.erdees.foodcostcalc.fragments.dialogs.AddProductToHalfProduct
import com.erdees.foodcostcalc.fragments.dialogs.EditHalfProduct
import com.erdees.foodcostcalc.SharedFunctions.getListSize
import com.erdees.foodcostcalc.SharedFunctions.getPriceForHundredPercentOfRecipe
import com.erdees.foodcostcalc.model.HalfProductWithProductsIncluded
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.HalfProductAdapterViewModel
import com.erdees.foodcostcalc.views.MaskedItemView
import io.reactivex.subjects.PublishSubject
import java.lang.NumberFormatException

class HalfProductAdapter(
    private val viewLifeCycleOwner: LifecycleOwner,
    private val list: ArrayList<HalfProductWithProductsIncluded>,
    private val fragmentManager: FragmentManager,
    val viewModel: HalfProductAdapterViewModel,
    val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val LAST_ITEM_TYPE = 0
    val HALF_PRODUCT_ITEM_TYPE = 1

    inner class HalfProductsRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val eachLinearLayout: LinearLayout = view.findViewById(R.id.linear_layout_dish_card)
        private val halfProductName: TextView = view.findViewById(R.id.half_product_name_in_adapter)
        private val editButton: ImageButton = view.findViewById(R.id.edit_button_in_dish_adapter)
        private val addProductButton: ImageButton =
            view.findViewById(R.id.add_product_to_halfproduct_button)
        private val listView: ListView = view.findViewById(R.id.list_view)
        private val unitOfHalfProduct: TextView =
            view.findViewById(R.id.unit_to_populate_half_product_card_view)
        private val finalPriceOfHalfProductPerUnit: TextView =
            view.findViewById(R.id.price_of_half_product_per_unit)
        private val quantityOfDataTV = view.findViewById<TextView>(R.id.quantity_of_data_tv)
        private val priceOfHalfProductPerRecipeTV = view.findViewById<TextView>(R.id.price_of_half_product_per_recipe)
        private val quantitySubject = PublishSubject.create<Double>()
        private var halfProductUnit = ""
        private var quantity = 0.0
        private var totalWeightOfMainRecipe = 0.0



        private fun setEditButton(position: Int) {
            editButton.setOnClickListener {
                EditHalfProduct().show(fragmentManager, EditHalfProduct.TAG)
                EditHalfProduct.halfProductPassedFromAdapter = list[position]
            }
        }

        private fun setAddButton(position: Int) {
            addProductButton.setOnClickListener {
                AddProductToHalfProduct().show(fragmentManager, TAG)
                viewModel.passHalfProductToDialog(list[position].halfProduct)

            }
        }

        private fun setWholeLayoutAsListenerWhichOpensAndClosesListOfProducts(position: Int) {
            eachLinearLayout.setOnClickListener {
                if (listView.adapter == null) {
                    quantityOfDataTV.visibility = View.VISIBLE
                    listView.adapter =
                        HalfProductListViewAdapter(activity, list[position].halfProductsList,quantity,totalWeightOfMainRecipe)
                    listView.layoutParams =
                        LinearLayout.LayoutParams(
                            listView.layoutParams.width,
                            getListSize(list[position].halfProductsList.indices.toList(), listView)
                        )
                } else {
                    quantityOfDataTV.visibility = View.GONE
                    listView.adapter = null
                    listView.layoutParams =
                        LinearLayout.LayoutParams(listView.layoutParams.width, 0)
                }
            }
        }

        private fun setNameAndUnitAccordingly(position: Int) {
            halfProductName.text = list[position].halfProduct.name
            unitOfHalfProduct.text = list[position].halfProduct.halfProductUnit + ":"
        }

        private fun setHalfProductFinalPrice(position: Int) {
            viewModel
                .getCertainHalfProductWithProductsIncluded(list[position].halfProduct.halfProductId)
                .observe(viewLifeCycleOwner,
                    {
                        if (it != null){
                            finalPriceOfHalfProductPerUnit.text =
                                it.formattedPricePerUnit
                            priceOfHalfProductPerRecipeTV.text = it.formattedPricePerRecipe
                        }
                    })
        }


        private fun setQuantityOfDataTextView(position: Int){
            val halfProduct = list[position]
            val totalWeight = halfProduct.totalWeight()
            totalWeightOfMainRecipe = totalWeight
            val unit = halfProduct.halfProduct.halfProductUnit
            quantity = totalWeight
            quantityOfDataTV.text = "Recipe per $totalWeight ${abbreviateUnitWithPer(unit)} of product."
        }

        private fun updateQuantityTV(position: Int){
            val halfProduct = list[position]
            val unit = halfProduct.halfProduct.halfProductUnit
            quantityOfDataTV.text = "Recipe per $quantity ${abbreviateUnitWithPer(unit)} of product."
        }

        private fun makeAdapterForList(position: Int, quantity: Double):ListAdapter {
            return HalfProductListViewAdapter(activity, list[position].halfProductsList,quantity,totalWeightOfMainRecipe)

        }

        private fun setPositiveButtonFunctionality(button: Button, editText: EditText, alertDialog: AlertDialog,position: Int){
            button.setOnClickListener {
                if(editText.text.isNullOrBlank() || editText.text.toString() == "."){
                    Toast.makeText(activity,"Wrong input!",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                quantitySubject.onNext(editText.text.toString().toDouble())
                listView.adapter =
                    makeAdapterForList(position, quantity) // TO REFRESH LIST
                listView.layoutParams =
                    LinearLayout.LayoutParams(
                        listView.layoutParams.width,
                        getListSize(list[position].halfProductsList.indices.toList(), listView)
                    )
                alertDialog.dismiss()
            }
        }

        private fun setQuantityOfDataTextViewAsButton(position: Int){
            quantityOfDataTV.setOnClickListener {
                val textInputLayout = activity.layoutInflater.inflate(R.layout.text_input_layout_decimal,null)
                val editText = textInputLayout.findViewById<EditText>(R.id.text_input_layout_quantity)
                val linearLayout = LinearLayout(activity)
                val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(25, 0, 25, 0)
                editText.setText(quantity.toString())
                linearLayout.addView(textInputLayout, params)
                val alertDialog = AlertDialog.Builder(activity)
                    .setMessage("Product weight")
                    .setView(linearLayout)
                    .setPositiveButton("Submit", null)
                    .setNegativeButton("Back", null)
                    .show()
                alertDialog.window?.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.background_for_dialogs
                    )
                )
                val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                setPositiveButtonFunctionality(positiveButton,editText,alertDialog,position)
            }
        }

        private fun updatePriceOfHalfProductPerRecipe(position: Int){
            val quantityPercent = getBasicRecipeAsPercentageOfTargetRecipe(quantity,totalWeightOfMainRecipe)
            val pricePerMainRecipe = list[position].pricePerRecipe()
            val pricePerRecipeForGivenQuantity = getPriceForHundredPercentOfRecipe(pricePerMainRecipe,quantityPercent)
            priceOfHalfProductPerRecipeTV.text = formatPrice(pricePerRecipeForGivenQuantity)

        }

        @SuppressLint("CheckResult")
        fun bind(position: Int) {
            setNameAndUnitAccordingly(position)
            setHalfProductFinalPrice(position)
            setQuantityOfDataTextView(position)
            setEditButton(position)
            setAddButton(position)
            setWholeLayoutAsListenerWhichOpensAndClosesListOfProducts(position)
            setQuantityOfDataTextViewAsButton(position)
            quantitySubject.subscribe { i ->
                quantity = i
                updateQuantityTV(position)
                updatePriceOfHalfProductPerRecipe(position)
                updatePriceOfHalfProductPerRecipe(position)
                Log.i("TEST", "update from RXKOTLIN new value : $i")
            }
        }

    }

    inner class LastItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val layoutAsButton: MaskedItemView =
            view.findViewById(R.id.products_last_item_layout)
        private val text: TextView = view.findViewById(R.id.last_item_text)

        fun bind() {
            text.text = "Create Half Product"
            layoutAsButton.setOnClickListener {
                Log.i("TEST", "WORKS")
                viewModel.setOpenCreateHalfProductFlag(true)
                viewModel.setOpenCreateHalfProductFlag(false)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HALF_PRODUCT_ITEM_TYPE -> {
                val adapterLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.half_product_card_view, parent, false)
                HalfProductsRecyclerViewHolder(adapterLayout)
            }
            else -> {
                val adapterLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.products_recycler_last_item, parent, false)
                LastItemViewHolder(adapterLayout)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == HALF_PRODUCT_ITEM_TYPE) (holder as HalfProductAdapter.HalfProductsRecyclerViewHolder).bind(
            position
        )
        else (holder as HalfProductAdapter.LastItemViewHolder).bind()

    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            list.size -> LAST_ITEM_TYPE
            else -> HALF_PRODUCT_ITEM_TYPE
        }
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    companion object {
        const val TAG = "HalfProductAdapter"
    }

}
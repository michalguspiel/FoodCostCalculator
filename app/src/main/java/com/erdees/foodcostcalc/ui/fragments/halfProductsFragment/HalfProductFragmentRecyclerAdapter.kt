package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.addProductToHalfProductDialogFragment.AddProductToHalfProductFragment
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.editHalfProductDialogFragment.EditHalfProductFragment
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductWithProductsIncludedModel
import com.erdees.foodcostcalc.ui.views.MaskedItemView
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Constants.HALF_PRODUCT_AD_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Constants.HALF_PRODUCT_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Constants.LAST_ITEM_TYPE
import com.erdees.foodcostcalc.utils.SharedFunctions.abbreviateUnitWithPer
import com.erdees.foodcostcalc.utils.SharedFunctions.formatPrice
import com.erdees.foodcostcalc.utils.SharedFunctions.getBasicRecipeAsPercentageOfTargetRecipe
import com.erdees.foodcostcalc.utils.SharedFunctions.getListSize
import com.erdees.foodcostcalc.utils.SharedFunctions.getPriceForHundredPercentOfRecipe
import com.erdees.foodcostcalc.utils.ads.AdHelper
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.HalfProductAdapterViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import io.reactivex.subjects.PublishSubject


class HalfProductFragmentRecyclerAdapter(
    private val viewLifeCycleOwner: LifecycleOwner,
    private val list: ArrayList<HalfProductWithProductsIncludedModel>,
    private val fragmentManager: FragmentManager,
    val viewModel: HalfProductAdapterViewModel,
    val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val adCase = AdHelper(list.size, Constants.HALF_PRODUCTS_AD_FREQUENCY)

    private val itemsSizeWithAds = adCase.finalListSize + 1 // +1 to include button as footer.

    private val positionsOfAds = adCase.positionsOfAds()

    private var currentNativeAd: NativeAd? = null

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
        private val priceOfHalfProductPerRecipeTV =
            view.findViewById<TextView>(R.id.price_of_half_product_per_recipe)
        private val quantitySubject = PublishSubject.create<Double>()
        private var quantity = 0.0
        private var totalWeightOfMainRecipe = 0.0


        private fun setEditButton(position: Int) {
            editButton.setOnClickListener {
                EditHalfProductFragment().show(fragmentManager, EditHalfProductFragment.TAG)
                EditHalfProductFragment.halfProductPassedFromAdapterModel = list[position]
            }
        }

        private fun setAddButton(position: Int) {
            addProductButton.setOnClickListener {
                AddProductToHalfProductFragment().show(fragmentManager, TAG)
                viewModel.passHalfProductToDialog(list[position].halfProductModel)

            }
        }

        private fun setWholeLayoutAsListenerWhichOpensAndClosesListOfProducts(position: Int) {
            eachLinearLayout.setOnClickListener {
                if (listView.adapter == null) {
                    quantityOfDataTV.visibility = View.VISIBLE
                    listView.adapter =
                        HalfProductDetailedListViewAdapter(
                            activity,
                            list[position].halfProductsListModel,
                            quantity,
                            totalWeightOfMainRecipe
                        )
                    listView.layoutParams =
                        LinearLayout.LayoutParams(
                            listView.layoutParams.width,
                            getListSize(
                                list[position].halfProductsListModel.indices.toList(),
                                listView
                            )
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
            halfProductName.text = list[position].halfProductModel.name
            unitOfHalfProduct.text = list[position].halfProductModel.halfProductUnit + ":"
        }

        private fun setHalfProductFinalPrice(position: Int) {
            viewModel
                .getCertainHalfProductWithProductsIncluded(list[position].halfProductModel.halfProductId)
                .observe(viewLifeCycleOwner,
                    {
                        if (it != null) {
                            finalPriceOfHalfProductPerUnit.text =
                                it.formattedPricePerUnit
                            priceOfHalfProductPerRecipeTV.text = it.formattedPricePerRecipe
                        }
                    })
        }


        private fun setQuantityOfDataTextView(position: Int) {
            val halfProduct = list[position]
            val totalWeight = halfProduct.totalWeight()
            totalWeightOfMainRecipe = totalWeight
            val unit = halfProduct.halfProductModel.halfProductUnit
            quantity = totalWeight
            quantityOfDataTV.text =
                "Recipe per $totalWeight ${abbreviateUnitWithPer(unit)} of product."
        }

        private fun updateQuantityTV(position: Int) {
            val halfProduct = list[position]
            val unit = halfProduct.halfProductModel.halfProductUnit
            quantityOfDataTV.text =
                "Recipe per $quantity ${abbreviateUnitWithPer(unit)} of product."
        }

        private fun makeAdapterForList(position: Int, quantity: Double): ListAdapter {
            return HalfProductDetailedListViewAdapter(
                activity,
                list[position].halfProductsListModel,
                quantity,
                totalWeightOfMainRecipe
            )

        }

        private fun setPositiveButtonFunctionality(
            button: Button,
            editText: EditText,
            alertDialog: AlertDialog,
            position: Int
        ) {
            button.setOnClickListener {
                if (editText.text.isNullOrBlank() || editText.text.toString() == ".") {
                    Toast.makeText(activity, "Wrong input!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                quantitySubject.onNext(editText.text.toString().toDouble())
                listView.adapter =
                    makeAdapterForList(position, quantity) // TO REFRESH LIST
                listView.layoutParams =
                    LinearLayout.LayoutParams(
                        listView.layoutParams.width,
                        getListSize(list[position].halfProductsListModel.indices.toList(), listView)
                    )
                alertDialog.dismiss()
            }
        }

        private fun setQuantityOfDataTextViewAsButton(position: Int) {
            quantityOfDataTV.setOnClickListener {
                val textInputLayout =
                    activity.layoutInflater.inflate(R.layout.text_input_layout_decimal, null)
                val editText =
                    textInputLayout.findViewById<EditText>(R.id.text_input_layout_quantity)
                val linearLayout = LinearLayout(activity)
                val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(25, 0, 25, 0)
                editText.setText(quantity.toString())
                linearLayout.addView(textInputLayout, params)
                val alertDialog = AlertDialog.Builder(activity)
                    .setMessage(activity.getString(R.string.product_weight))
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
                setPositiveButtonFunctionality(positiveButton, editText, alertDialog, position)
            }
        }

        private fun updatePriceOfHalfProductPerRecipe(position: Int) {
            val quantityPercent =
                getBasicRecipeAsPercentageOfTargetRecipe(quantity, totalWeightOfMainRecipe)
            val pricePerMainRecipe = list[position].pricePerRecipe()
            val pricePerRecipeForGivenQuantity =
                getPriceForHundredPercentOfRecipe(pricePerMainRecipe, quantityPercent)
            priceOfHalfProductPerRecipeTV.text = formatPrice(pricePerRecipeForGivenQuantity)

        }

        @SuppressLint("CheckResult")
        fun bind(position: Int) {
            val positionIncludedAdsBinded = adCase.correctElementFromListToBind(position)

            setNameAndUnitAccordingly(positionIncludedAdsBinded)
            setHalfProductFinalPrice(positionIncludedAdsBinded)
            setQuantityOfDataTextView(positionIncludedAdsBinded)
            setEditButton(positionIncludedAdsBinded)
            setAddButton(positionIncludedAdsBinded)
            setWholeLayoutAsListenerWhichOpensAndClosesListOfProducts(positionIncludedAdsBinded)
            setQuantityOfDataTextViewAsButton(positionIncludedAdsBinded)
            quantitySubject.subscribe { i ->
                quantity = i
                updateQuantityTV(positionIncludedAdsBinded)
                updatePriceOfHalfProductPerRecipe(positionIncludedAdsBinded)
                updatePriceOfHalfProductPerRecipe(positionIncludedAdsBinded)
            }
        }

    }

    inner class AdItemViewHolder(val view: NativeAdView) : RecyclerView.ViewHolder(view) {
        fun bind() {
            val builder = AdLoader.Builder(activity, Constants.ADMOB_HALF_PRODUCTS_RV_AD_UNIT_ID)
            builder.forNativeAd { nativeAd ->
                // OnUnifiedNativeAdLoadedListener implementation.
                // If this callback occurs after the activity is destroyed, you must call
                // destroy and return or you may get a memory leak.
                val activityDestroyed: Boolean = activity.isDestroyed
                if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                    nativeAd.destroy()
                    return@forNativeAd
                }
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                currentNativeAd?.destroy()
                currentNativeAd = nativeAd
                adCase.populateNativeAdView(nativeAd, view)
            }
            val videoOptions = VideoOptions.Builder()
                .build()
            val adOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()
            builder.withNativeAdOptions(adOptions)
            val adLoader = builder.withAdListener(object : AdListener() {
            }).build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }


    inner class LastItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val layoutAsButton: MaskedItemView =
            view.findViewById(R.id.products_last_item_layout)
        private val text: TextView = view.findViewById(R.id.last_item_text)

        fun bind() {
            text.text = activity.getString(R.string.create_half_product)
            layoutAsButton.setOnClickListener {
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
            HALF_PRODUCT_AD_ITEM_TYPE -> {
                val adapterLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.product_ad_custom_layout, parent, false) as NativeAdView
                AdItemViewHolder(adapterLayout)
            }
            else -> {
                val adapterLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.products_recycler_last_item, parent, false)
                LastItemViewHolder(adapterLayout)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == HALF_PRODUCT_ITEM_TYPE) (holder as HalfProductsRecyclerViewHolder).bind(
            position
        )
        else if (holder.itemViewType == HALF_PRODUCT_AD_ITEM_TYPE) (holder as AdItemViewHolder).bind()
        else (holder as LastItemViewHolder).bind()

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemsSizeWithAds - 1) LAST_ITEM_TYPE
        else if (positionsOfAds.contains(position)) HALF_PRODUCT_AD_ITEM_TYPE
        else HALF_PRODUCT_ITEM_TYPE
    }

    override fun getItemCount(): Int {
        return itemsSizeWithAds
    }

    companion object {
        const val TAG = "HalfProductFragmentRecyclerAdapter"
    }


}
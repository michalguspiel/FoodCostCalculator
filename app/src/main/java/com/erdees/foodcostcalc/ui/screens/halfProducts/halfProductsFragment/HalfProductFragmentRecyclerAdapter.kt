package com.erdees.foodcostcalc.ui.screens.halfProducts.halfProductsFragment

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.HalfProductCardViewBinding
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Constants.HALF_PRODUCT_AD_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Constants.HALF_PRODUCT_ITEM_TYPE
import com.erdees.foodcostcalc.utils.UnitsUtils.getPerUnitAbbreviation
import com.erdees.foodcostcalc.utils.Utils.formatPrice
import com.erdees.foodcostcalc.utils.Utils.getBasicRecipeAsPercentageOfTargetRecipe
import com.erdees.foodcostcalc.utils.Utils.getPriceForHundredPercentOfRecipe
import com.erdees.foodcostcalc.utils.ViewUtils.getListSize
import com.erdees.foodcostcalc.utils.ads.AdHelper
import com.erdees.foodcostcalc.utils.diffutils.HalfProductDomainDiffUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class HalfProductFragmentRecyclerAdapter(
    private val activity: Activity,
    private val viewModel: HalfProductsFragmentViewModel,
    private val navigateToAddItemsToHalfProductScreen: (HalfProductDomain) -> Unit,
    private val navigateToEditHalfProductScreen: (HalfProductDomain) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var list: List<HalfProductDomain> = listOf()

    private var adCase = AdHelper(list.size, Constants.HALF_PRODUCTS_AD_FREQUENCY)

    private var positionsOfAds = adCase.positionsOfAds()

    private var currentNativeAd: NativeAd? = null

    private var positionOfListAdapterToUpdate: Int? = null

    fun setHalfProductsList(passedList: List<HalfProductDomain>) {
        val diffUtil =
            HalfProductDomainDiffUtil(oldList = this.list, newList = passedList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        this.list = passedList
        refreshAdCase()
        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }

    private fun refreshAdCase() {
        adCase = AdHelper(list.size, Constants.HALF_PRODUCTS_AD_FREQUENCY)
        positionsOfAds = adCase.positionsOfAds()
    }

    inner class HalfProductsRecyclerViewHolder(private val viewBinding: HalfProductCardViewBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {

        private fun setEditButton(position: Int) {
            viewBinding.editButtonInDishAdapter.setOnClickListener {
                navigateToEditHalfProductScreen(list[position])
            }
        }

        private fun setAddButton(position: Int) {
            viewBinding.addProductToHalfproductButton.setOnClickListener {
                navigateToAddItemsToHalfProductScreen(list[position])
            }
        }

        private fun getHalfProductQuantity(position: Int): Double {
            val halfProductID = list[position].id
            return viewModel.idToQuantityMap[halfProductID] ?: list[position].totalQuantity
        }

        private fun makeExpandable(position: Int) {
            viewBinding.linearLayoutDishCard.setOnClickListener {
                val halfProductID = list[position].id
                val isExpanded = viewModel.determineIfCardIsExpanded(halfProductID)
                if (isExpanded) {
                    viewModel.expandedList.remove(halfProductID)
                    hideCardElements()
                } else {
                    viewModel.expandedList.add(halfProductID)
                    showCardElements(position)
                }
            }
        }

        private fun openOrCloseCard(position: Int) {
            val halfProductID = list[position].id
            val isExpanded = viewModel.determineIfCardIsExpanded(halfProductID)
            if (isExpanded) showCardElements(position)
            else hideCardElements()
        }

        private fun hideCardElements() {
            viewBinding.quantityOfDataTv.visibility = View.GONE
            viewBinding.listView.adapter = null
            viewBinding.listView.layoutParams =
                LinearLayout.LayoutParams(viewBinding.listView.layoutParams.width, 0)
        }

        private fun showCardElements(position: Int) {
            viewBinding.quantityOfDataTv.visibility = View.VISIBLE
            val adapter = HalfProductDetailedListViewAdapter(
                activity,
                halfProductDomain = list[position],
                getHalfProductQuantity(position),
                list[position].totalQuantity
            )
            viewBinding.listView.adapter = adapter
            viewBinding.listView.layoutParams =
                LinearLayout.LayoutParams(
                    viewBinding.listView.layoutParams.width,
                    getListSize(
                        list[position].products.indices.toList(),
                        viewBinding.listView
                    )
                )
        }

        private fun setNameAndUnitAccordingly(position: Int) {
            viewBinding.halfProductNameInAdapter.text = list[position].name
            viewBinding.unitToPopulateHalfProductCardView.text =
                "${list[position].halfProductUnit}:"
        }

        private fun setHalfProductFinalPrice(position: Int) {
            viewBinding.priceOfHalfProductPerUnit.text =
                list[position].formattedPricePerUnit(activity)
            viewBinding.priceOfHalfProductPerRecipe.text =
                list[position].formattedPricePerRecipe(activity)
        }

        private fun getTotalQuantityOfOneRecipe(position: Int): Double {
            return list[position].totalQuantity
        }

        private fun setQuantityOfDataTextView(position: Int) {
            val halfProduct = list[position]
            val unit = halfProduct.halfProductUnit
            viewBinding.quantityOfDataTv.text =
                "Recipe per ${getHalfProductQuantity(position)} ${getPerUnitAbbreviation(unit)} of product."
        }

        private fun updateQuantityTV(position: Int) {
            val halfProduct = list[position]
            val unit = halfProduct.halfProductUnit
            viewBinding.quantityOfDataTv.text =
                "Recipe per ${getHalfProductQuantity(position)} ${getPerUnitAbbreviation(unit)} of product."
        }

        private fun makeAdapterForList(position: Int, quantity: Double): ListAdapter {
            return HalfProductDetailedListViewAdapter(
                activity,
                list[position],
                quantity,
                list[position].totalQuantity
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
                val halfProductID = list[position].id
                viewModel.idToQuantityMap[halfProductID] = editText.text.toString().toDouble()
                setHalfProductsList(list)
                refreshAdapter(position)
                alertDialog.dismiss()
            }
        }

        private fun refreshAdapter(position: Int) {
            viewBinding.listView.adapter =
                makeAdapterForList(position, getHalfProductQuantity(position)) // TO REFRESH LIST
            viewBinding.listView.layoutParams =
                LinearLayout.LayoutParams(
                    viewBinding.listView.layoutParams.width,
                    getListSize(list[position].products.indices.toList(), viewBinding.listView)
                )
        }

        private fun setQuantityOfDataTextViewAsButton(position: Int) {
            viewBinding.quantityOfDataTv.setOnClickListener {
                val textInputLayout =
                    activity.layoutInflater.inflate(R.layout.text_input_layout_decimal, null)
                val editText =
                    textInputLayout.findViewById<EditText>(R.id.text_input_layout_quantity)
                val linearLayout = LinearLayout(activity)
                val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(25, 0, 25, 0)
                editText.setText(getHalfProductQuantity(position).toString())
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
                getBasicRecipeAsPercentageOfTargetRecipe(
                    getHalfProductQuantity(position),
                    getTotalQuantityOfOneRecipe(position)
                )
            val pricePerMainRecipe = list[position].totalPrice
            val pricePerRecipeForGivenQuantity =
                getPriceForHundredPercentOfRecipe(pricePerMainRecipe, quantityPercent)
            viewBinding.priceOfHalfProductPerRecipe.text =
                formatPrice(pricePerRecipeForGivenQuantity, activity)
        }

        private val positionIncludedAdsBinded get() = adCase.correctElementFromListToBind(this.adapterPosition)

        fun bind() {
            positionOfListAdapterToUpdate?.let {
                if (it == positionIncludedAdsBinded) refreshAdapter(it)
            }
            setNameAndUnitAccordingly(positionIncludedAdsBinded)
            setHalfProductFinalPrice(positionIncludedAdsBinded)
            setQuantityOfDataTextView(positionIncludedAdsBinded)
            setEditButton(positionIncludedAdsBinded)
            setAddButton(positionIncludedAdsBinded)
            makeExpandable(positionIncludedAdsBinded)
            openOrCloseCard(positionIncludedAdsBinded)
            setQuantityOfDataTextViewAsButton(positionIncludedAdsBinded)
            updateQuantityTV(positionIncludedAdsBinded)
            updatePriceOfHalfProductPerRecipe(positionIncludedAdsBinded)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HALF_PRODUCT_ITEM_TYPE -> {
                HalfProductsRecyclerViewHolder(
                    HalfProductCardViewBinding.inflate(
                        LayoutInflater.from(activity),
                        parent,
                        false
                    )
                )
            }

            else -> {
                val adapterLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.product_ad_custom_layout, parent, false) as NativeAdView
                AdItemViewHolder(adapterLayout)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return if (holder.itemViewType == HALF_PRODUCT_ITEM_TYPE) (holder as HalfProductsRecyclerViewHolder).bind()
        else (holder as AdItemViewHolder).bind()
    }

    override fun getItemViewType(position: Int): Int {
        return if (positionsOfAds.contains(position)) HALF_PRODUCT_AD_ITEM_TYPE
        else HALF_PRODUCT_ITEM_TYPE
    }

    override fun getItemCount(): Int {
        return adCase.finalListSize
    }
}
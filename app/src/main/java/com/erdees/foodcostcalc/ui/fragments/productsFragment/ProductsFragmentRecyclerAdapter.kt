package com.erdees.foodcostcalc.ui.fragments.productsFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.ListProductBinding
import com.erdees.foodcostcalc.domain.model.product.ProductModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.editProductDialogFragment.EditProductFragment
import com.erdees.foodcostcalc.ui.views.MaskedItemView
import com.erdees.foodcostcalc.utils.Constants.ADMOB_PRODUCTS_RV_AD_UNIT_ID
import com.erdees.foodcostcalc.utils.Constants.LAST_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Constants.PRODUCTS_AD_FREQUENCY
import com.erdees.foodcostcalc.utils.Constants.PRODUCT_AD_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Constants.PRODUCT_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Utils.formatPrice
import com.erdees.foodcostcalc.utils.ads.AdHelper
import com.erdees.foodcostcalc.utils.diffutils.ProductDiffUtil
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import java.util.*

class ProductsFragmentRecyclerAdapter(
  val activity: Activity,
  val tag: String?,
  private val fragmentManager: FragmentManager,
  private val navigateToAdd: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var list : List<ProductModel> = listOf()

  private var adCase = AdHelper(list.size, PRODUCTS_AD_FREQUENCY)

  private var itemsSizeWithAds = adCase.finalListSize + 1 // +1 to include button as footer.

  private var positionsOfAds = adCase.positionsOfAds()

  private var currentNativeAd: NativeAd? = null

  fun switchLists(passedList: List<ProductModel>) {
    Log.i(ProductsFragment.TAG,"Switching lists")
    val diffUtil = ProductDiffUtil(oldList = this.list, newList = passedList)
    val diffResult = DiffUtil.calculateDiff(diffUtil)
    this.list = passedList
    refreshAdCase()
    diffResult.dispatchUpdatesTo(this)
    notifyDataSetChanged()
  }

  private fun refreshAdCase(){
    adCase = AdHelper(list.size, PRODUCTS_AD_FREQUENCY)
    itemsSizeWithAds = adCase.finalListSize + 1 // +1 to include button as footer.
    positionsOfAds = adCase.positionsOfAds()
  }

  inner class RecyclerViewHolder(private val viewBinding: ListProductBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {
    private val positionIncludedAdsBinded get() =  adCase.correctElementFromListToBind(this.adapterPosition)

    @SuppressLint("SetTextI18n")
    fun bind() {
      Log.i(
        "ProductsRecycler",
        "position : ${this.adapterPosition} , positionIncludedWithAdsBinded : $positionIncludedAdsBinded , listSize : ${list.size} , positionOfAds : $positionsOfAds"
      )
      viewBinding.productTitle.text = list[positionIncludedAdsBinded].name
      viewBinding.productNettoPrice.text =
        "Price ${list[positionIncludedAdsBinded].unit} netto: ${formatPrice(list[positionIncludedAdsBinded].pricePerUnit,activity)}."
      viewBinding.productFoodcostPrice.text =
        "Price ${list[positionIncludedAdsBinded].unit} with foodcost: ${formatPrice(list[positionIncludedAdsBinded].priceAfterWasteAndTax,activity)}."
      viewBinding.editButton.setOnClickListener {
        EditProductFragment().show(fragmentManager, EditProductFragment.TAG)
        EditProductFragment.productModelPassedFromAdapter = list[positionIncludedAdsBinded]
      }
    }
  }

  inner class LastItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val layoutAsButton: MaskedItemView =
      view.findViewById(R.id.products_last_item_layout)

    fun bind() {
      layoutAsButton.setOnClickListener {
        navigateToAdd()
      }
    }
  }

  inner class AdItemViewHolder(val view: NativeAdView) : RecyclerView.ViewHolder(view) {
    fun bind() {
      val builder = AdLoader.Builder(activity, ADMOB_PRODUCTS_RV_AD_UNIT_ID)
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
    Log.i(ProductsFragment.TAG,"OnCreateViewHolder")

    return when (viewType) {
      PRODUCT_ITEM_TYPE -> {
        RecyclerViewHolder(
          ListProductBinding.inflate(
          LayoutInflater.from(activity),parent,false
        ))
      }

      PRODUCT_AD_ITEM_TYPE -> {
        val adapterLayout = LayoutInflater.from(parent.context)
          .inflate(R.layout.product_ad_custom_layout, parent, false) as NativeAdView
        AdItemViewHolder(adapterLayout)
      }

      else -> {
        val adapterLayout =
          LayoutInflater.from(parent.context)
            .inflate(R.layout.products_recycler_last_item, parent, false)
        LastItemViewHolder(adapterLayout)
      }
    }
  }

  override fun getItemCount(): Int {
    return itemsSizeWithAds
  }

  override fun getItemViewType(position: Int): Int {
    Log.i(ProductsFragment.TAG,"getItemType")
    return if (position == itemsSizeWithAds - 1) LAST_ITEM_TYPE
    else if (positionsOfAds.contains(position)) PRODUCT_AD_ITEM_TYPE
    else PRODUCT_ITEM_TYPE
  }

  @SuppressLint("WrongConstant", "ShowToast")
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    Log.i(ProductsFragment.TAG,"OnBindViewHolder")
    if (holder.itemViewType == PRODUCT_ITEM_TYPE) (holder as RecyclerViewHolder).bind()
    else if (holder.itemViewType == PRODUCT_AD_ITEM_TYPE) (holder as AdItemViewHolder).bind()
    else (holder as LastItemViewHolder).bind()
  }
}

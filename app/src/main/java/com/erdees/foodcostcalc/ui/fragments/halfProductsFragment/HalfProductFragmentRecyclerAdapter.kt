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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.HalfProductCardViewBinding
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.addProductToHalfProductDialogFragment.AddProductToHalfProductFragment
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.editHalfProductDialogFragment.EditHalfProductFragment
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductWithProductsIncludedModel
import com.erdees.foodcostcalc.ui.views.MaskedItemView
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Constants.HALF_PRODUCT_AD_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Constants.HALF_PRODUCT_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Constants.LAST_ITEM_TYPE
import com.erdees.foodcostcalc.utils.UnitsUtils.getPerUnitAbbreviation
import com.erdees.foodcostcalc.utils.Utils.formatPrice
import com.erdees.foodcostcalc.utils.Utils.getBasicRecipeAsPercentageOfTargetRecipe
import com.erdees.foodcostcalc.utils.Utils.getPriceForHundredPercentOfRecipe
import com.erdees.foodcostcalc.utils.ViewUtils.getListSize
import com.erdees.foodcostcalc.utils.ads.AdHelper
import com.erdees.foodcostcalc.utils.diffutils.HalfProductWithProductsIncludedDiffUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import io.reactivex.subjects.PublishSubject

class HalfProductFragmentRecyclerAdapter(
  private val fragmentManager: FragmentManager,
  val activity: Activity,
  private val openCreateHalfProductDialog: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var list : List<HalfProductWithProductsIncludedModel> = listOf()

  private var adCase = AdHelper(list.size, Constants.HALF_PRODUCTS_AD_FREQUENCY)

  private var itemsSizeWithAds = adCase.finalListSize + 1 // +1 to include button as footer.

  private var positionsOfAds = adCase.positionsOfAds()

  private var currentNativeAd: NativeAd? = null

  private var positionOfListAdapterToUpdate: Int? = null

  fun switchList(passedList: List<HalfProductWithProductsIncludedModel>){
      val diffUtil = HalfProductWithProductsIncludedDiffUtil(oldList = this.list, newList = passedList)
      val diffResult = DiffUtil.calculateDiff(diffUtil)
      this.list = passedList
      refreshAdCase()
      diffResult.dispatchUpdatesTo(this)
      notifyDataSetChanged()
  }
  private fun refreshAdCase(){
    adCase = AdHelper(list.size, Constants.PRODUCTS_AD_FREQUENCY)
    itemsSizeWithAds = adCase.finalListSize + 1 // +1 to include button as footer.
    positionsOfAds = adCase.positionsOfAds()
  }

  inner class HalfProductsRecyclerViewHolder(private val viewBinding: HalfProductCardViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {
    private val quantitySubject = PublishSubject.create<Double>()
    private var quantity = 0.0
    private var totalWeightOfMainRecipe = 0.0

    private fun setEditButton(position: Int) {
      viewBinding.editButtonInDishAdapter.setOnClickListener {
        positionOfListAdapterToUpdate = position
        EditHalfProductFragment().show(fragmentManager, EditHalfProductFragment.TAG)
        EditHalfProductFragment.halfProductPassedFromAdapter = list[position]
      }
    }

    private fun setAddButton(position: Int) {
      viewBinding.addProductToHalfproductButton.setOnClickListener {
        positionOfListAdapterToUpdate = position
        AddProductToHalfProductFragment().show(fragmentManager, TAG)
        AddProductToHalfProductFragment.halfProduct = list[position].halfProductModel
      }
    }


    private fun setWholeLayoutAsListenerWhichOpensAndClosesListOfProducts(position: Int) {
      viewBinding.linearLayoutDishCard.setOnClickListener {
        if (viewBinding.listView.adapter == null) {
          viewBinding.quantityOfDataTv.visibility = View.VISIBLE
           val adapter = HalfProductDetailedListViewAdapter(
            activity,
             list[position].halfProductsList,
            quantity,
            totalWeightOfMainRecipe
          )
          viewBinding.listView.adapter = adapter
          viewBinding.listView.layoutParams =
            LinearLayout.LayoutParams(
              viewBinding.listView.layoutParams.width,
              getListSize(
                list[position].halfProductsList.indices.toList(),
                viewBinding.listView
              )
            )
        } else {
          viewBinding.quantityOfDataTv.visibility = View.GONE
          viewBinding.listView.adapter = null
          viewBinding.listView.layoutParams =
            LinearLayout.LayoutParams(viewBinding.listView.layoutParams.width, 0)
        }
      }
    }

    private fun setNameAndUnitAccordingly(position: Int) {
      viewBinding.halfProductNameInAdapter.text = list[position].halfProductModel.name
      viewBinding.unitToPopulateHalfProductCardView.text = list[position].halfProductModel.halfProductUnit + ":"
    }

    private fun setHalfProductFinalPrice(position: Int) {
      viewBinding.priceOfHalfProductPerUnit.text = list[position].formattedPricePerUnit
      viewBinding.priceOfHalfProductPerRecipe.text = list[position].formattedPricePerRecipe
    }

    private fun setQuantityOfDataTextView(position: Int) {
      val halfProduct = list[position]
      val totalWeight = halfProduct.totalWeight()
      totalWeightOfMainRecipe = totalWeight
      val unit = halfProduct.halfProductModel.halfProductUnit
      quantity = totalWeight
      viewBinding.quantityOfDataTv.text =
        "Recipe per $totalWeight ${getPerUnitAbbreviation(unit)} of product."
    }

    private fun updateQuantityTV(position: Int) {
      val halfProduct = list[position]
      val unit = halfProduct.halfProductModel.halfProductUnit
      viewBinding.quantityOfDataTv.text =
        "Recipe per $quantity ${getPerUnitAbbreviation(unit)} of product."
    }

    private fun makeAdapterForList(position: Int, quantity: Double): ListAdapter {
      return HalfProductDetailedListViewAdapter(
        activity,
        list[position].halfProductsList,
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
        refreshAdapter(position)
        alertDialog.dismiss()
      }
    }

    private fun refreshAdapter(position: Int){
      viewBinding.listView.adapter =
        makeAdapterForList(position, quantity) // TO REFRESH LIST
      viewBinding.listView.layoutParams =
        LinearLayout.LayoutParams(
          viewBinding.listView.layoutParams.width,
          getListSize(list[position].halfProductsList.indices.toList(),   viewBinding.listView)
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
      viewBinding.priceOfHalfProductPerRecipe.text = formatPrice(pricePerRecipeForGivenQuantity)
    }

    private val positionIncludedAdsBinded get() =  adCase.correctElementFromListToBind(this.adapterPosition)

    @SuppressLint("CheckResult")
    fun bind() {
      positionOfListAdapterToUpdate?.let {
      if(it == positionIncludedAdsBinded) refreshAdapter(it)
      }
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
        openCreateHalfProductDialog()
      }
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
    if (holder.itemViewType == HALF_PRODUCT_ITEM_TYPE) (holder as HalfProductsRecyclerViewHolder).bind()
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

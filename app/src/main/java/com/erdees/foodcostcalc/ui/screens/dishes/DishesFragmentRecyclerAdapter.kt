package com.erdees.foodcostcalc.ui.screens.dishes

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.DishCardViewBinding
import com.erdees.foodcostcalc.databinding.ListviewDishRowBinding
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Constants.DISH_AD_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Constants.DISH_ITEM_TYPE
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.ViewUtils.makeGone
import com.erdees.foodcostcalc.utils.ViewUtils.makeVisible
import com.erdees.foodcostcalc.utils.ads.AdHelper
import com.erdees.foodcostcalc.utils.diffutils.DishDomainDiffUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.play.core.review.ReviewManagerFactory

class DishesFragmentRecyclerAdapter(
  private val viewModel: DishesFragmentViewModel,
  private val activity: Activity,
  private val navigateToAddItemsToDish: ((Long, String) -> Unit),
  private val navigateToEditDish: ((DishDomain) -> Unit)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  var list: List<DishDomain> = listOf()
  var adCase: AdHelper = AdHelper(list.size, Constants.DISHES_AD_FREQUENCY)
  private var currentNativeAd: NativeAd? = null
  private var positionOfListAdapterToUpdate: Int? = null

  fun setDishList(listToSet: List<DishDomain>) {
    Log.i(TAG, "setDishList")
    val diffUtil = DishDomainDiffUtil(oldList = this.list, newList = listToSet)
    val diffResult = DiffUtil.calculateDiff(diffUtil)
    this.list = listToSet
    diffResult.dispatchUpdatesTo(this)
    refreshAdHelper()
    notifyDataSetChanged()
  }


  private fun refreshAdHelper() {
    adCase = AdHelper(list.size, Constants.DISHES_AD_FREQUENCY)
  }

  inner class DishRecyclerViewHolder(private val viewBinding: DishCardViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {

    private fun setPriceData(amountOfServings: Int, dishModelId: Long) {
      Log.i(TAG, "Setting price data for dish $dishModelId with: $amountOfServings servings")
      viewBinding.totalPriceDishCardView.text =
        viewModel.formattedPriceData(dishModelId, amountOfServings, activity)
      viewBinding.totalPriceWithMarginDishCardView.text = viewModel.formattedTotalPriceData(
        dishModelId,
        amountOfServings,
        activity
      )
    }

    private fun setHowManyServingsTV(i: Int) {
      val quantity =
        viewModel.idToQuantityMap[list[i].dishId] ?: 1
      Log.i(TAG, "setHowManyServingsTV, $i")
      if (quantity == 1) viewBinding.howManyServingsTextView.text =
        activity.getString(R.string.data_per_serving)
      else viewBinding.howManyServingsTextView.text =
        activity.getString(R.string.data_per_x_servings, quantity.toString())
    }


    private fun getAmountOfServings(position: Int): Int {
      val dishID = list[position].dishId
      val amountOfServings = viewModel.idToQuantityMap[dishID]
      return amountOfServings ?: 1 // If we don't have data it's 1
    }

    private fun setNameTaxAndMarginAccordingly(position: Int) {
      viewBinding.dishNameInAdapter.text = list[position].name
      viewBinding.dishMarginTvInAdapter.text = (activity.getString(
        R.string.dish_x_margin, String.format(
          list[position].marginPercent.toString()
        )
      ))
      viewBinding.dishTaxTvInAdapter.text = activity.getString(
        R.string.dish_x_tax, String.format(
          list[position].taxPercent.toString()
        )
      )
    }

    private fun setButtons(position: Int) {
      viewBinding.editButtonInDishAdapter.setOnClickListener {
        positionOfListAdapterToUpdate = position
        navigateToEditDish(list[position])
      }
      viewBinding.addProductToDishButton.setOnClickListener {
        positionOfListAdapterToUpdate = position
        navigateToAddItemsToDish(list[position].dishId, list[position].name)
      }
    }


    private fun setIngredientList(position: Int) {
      if (viewBinding.ingredientList.isGone) return
      Log.i(TAG, "setIngredientList")
      viewBinding.ingredientList.removeAllViews()
      val dish = list[position]
      val servings = getAmountOfServings(position)
      val ingredients = (dish.products + dish.halfProducts)
      ingredients.forEachIndexed { index, ingredient ->
        val row = ListviewDishRowBinding.inflate(LayoutInflater.from(activity))
        when (ingredient) {
          is UsedHalfProductDomain -> {
            setRowAsHalfProduct(row, ingredient, servings)
          }

          is UsedProductDomain -> setRowAsProduct(ingredient, row, servings)
        }
        viewBinding.ingredientList.addView(
          row.root
        )
        if (index < ingredients.size - 1) {
          Log.i(TAG, "Index : $index, size : ${ingredients.size} , adding divider")
          val divider = View(activity)
          divider.setBackgroundColor(ContextCompat.getColor(activity, R.color.gray_200))
          divider.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
          )
          viewBinding.ingredientList.addView(
            divider
          )
        }
      }
    }

    private fun setRowAsProduct(
      product: UsedProductDomain,
      view: ListviewDishRowBinding,
      servings: Int
    ) {
      view.productNameInDishRow.text =
        product.item.name
      view.productWeightInDishRow.text =
        Utils.formatPriceOrWeight(product.quantity * servings)
      view.productPriceInDishRow.text = Utils.formatPrice(product.totalPrice * servings, activity)
      view.productWeightUnitInDishRow.text =
        UnitsUtils.getUnitAbbreviation(product.quantityUnit)
    }

    private fun setHalfProductRowPrice(
      servings: Int,
      halfProduct: UsedHalfProductDomain,
      productPriceTextView: TextView
    ) {
      productPriceTextView.text = Utils.formatPrice(halfProduct.totalPrice * servings, activity)
    }

    private fun setRowAsHalfProduct(
      view: ListviewDishRowBinding,
      halfProduct: UsedHalfProductDomain,
      servings: Int
    ) {
      view.productNameInDishRow.text =
        halfProduct.item.name
      view.productWeightInDishRow.text =
        (halfProduct.quantity * servings).toString()
      view.productWeightUnitInDishRow.text =
        UnitsUtils.getUnitAbbreviation(halfProduct.quantityUnit)
      setHalfProductRowPrice(servings, halfProduct, view.productPriceInDishRow)
    }

    /**
     * Makes dish card expandable.
     * When clicked, adds the dish id to the list in viewmodel.
     * Based on that list UI determines if it should be open or closed.
     * @param position Int
     */
    private fun makeDishCardExpandable(position: Int) {
      viewBinding.linearLayoutDishCard.setOnClickListener {
        val dishId = list[position].dishId
        val isExpanded = viewModel.determineIfDishIsExpanded(dishId)
        if (isExpanded) {
          viewModel.expandedList.remove(dishId)
          hideDishCardElements()
        } else {
          viewModel.expandedList.add(dishId)
          showDishCardElements(position)
        }
      }
    }

    private fun openOrCloseCard(position: Int) {
      val dishId = list[position].dishId
      val isExpanded = viewModel.determineIfDishIsExpanded(dishId)
      if (isExpanded) showDishCardElements(position)
      else hideDishCardElements()
    }

    private fun showDishCardElements(position: Int) {
      viewBinding.ingredientList.makeVisible()
      viewBinding.howManyServingsTextView.makeVisible()
      setIngredientList(position)
    }

    private fun hideDishCardElements() {
      viewBinding.ingredientList.makeGone()
      viewBinding.howManyServingsTextView.makeGone()
    }

    private fun setPositiveButtonFunctionality(
      positiveButton: Button,
      editText: EditText,
      alertDialog: AlertDialog,
      position: Int
    ) {
      positiveButton.setOnClickListener {
        if (editText.text.isNullOrBlank() || !editText.text.isDigitsOnly()) {
          Toast.makeText(activity, "Wrong input!", Toast.LENGTH_SHORT).show()
          return@setOnClickListener
        }
        val dishId = list[position].dishId
        viewModel.idToQuantityMap[dishId] = editText.text.toString().toInt()
        setDishList(list) // to refresh the list
        alertDialog.dismiss()
      }
    }

    private fun setHowManyServingsTvAsButton(textView: TextView, position: Int) {
      textView.setOnClickListener {
        positionOfListAdapterToUpdate = position
        val textInputLayout =
          activity.layoutInflater.inflate(R.layout.text_input_layout, null)
        val editText =
          textInputLayout.findViewById<EditText>(R.id.text_input_layout_quantity)
        val linearLayout = LinearLayout(activity)
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(25, 0, 25, 0)
        editText.setText(getAmountOfServings(position).toString())
        linearLayout.addView(textInputLayout, params)
        val alertDialog = AlertDialog.Builder(activity)
          .setMessage(R.string.serving_amount)
          .setView(linearLayout)
          .setPositiveButton(R.string.submit, null)
          .setNegativeButton(R.string.back, null)
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

    private val positionIncludedAdsBinded get() = adCase.correctElementFromListToBind(this.adapterPosition)

    fun bind() {
      Log.i(TAG, "bind, pos: $positionIncludedAdsBinded")
      if (positionIncludedAdsBinded > list.size) {
        Log.e("TEST", "position smaller than list")
        return
      }
      setPriceData(
        getAmountOfServings(positionIncludedAdsBinded),
        list[positionIncludedAdsBinded].dishId
      )
      setHowManyServingsTV(positionIncludedAdsBinded)
      setIngredientList(positionIncludedAdsBinded)
      if (positionIncludedAdsBinded == 6) openFeedBackForm()
      setNameTaxAndMarginAccordingly(positionIncludedAdsBinded)
      setButtons(positionIncludedAdsBinded)
      makeDishCardExpandable(positionIncludedAdsBinded)
      openOrCloseCard(positionIncludedAdsBinded)
      setHowManyServingsTvAsButton(
        viewBinding.howManyServingsTextView,
        positionIncludedAdsBinded
      )
    }
  }

  inner class AdItemViewHolder(val view: NativeAdView) : RecyclerView.ViewHolder(view) {
    fun bind() {
      val builder = AdLoader.Builder(activity, Constants.ADMOB_DISHES_RV_AD_UNIT_ID)
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
      DISH_AD_ITEM_TYPE -> {
        val adapterLayout = LayoutInflater.from(parent.context)
          .inflate(R.layout.product_ad_custom_layout, parent, false) as NativeAdView
        AdItemViewHolder(adapterLayout)
      }

      else -> {
        DishRecyclerViewHolder(
          DishCardViewBinding.inflate(
            LayoutInflater.from(activity),
            parent,
            false
          )
        )
      }
    }
  }

  override fun getItemCount(): Int {
    return adCase.finalListSize
  }

  override fun getItemViewType(position: Int): Int {
    return if (adCase.positionsOfAds().contains(position)) DISH_AD_ITEM_TYPE
    else DISH_ITEM_TYPE
  }

  @SuppressLint("WrongConstant", "ShowToast", "SetTextI18n")
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder.itemViewType) {
      DISH_AD_ITEM_TYPE -> (holder as AdItemViewHolder).bind()
      else -> (holder as DishRecyclerViewHolder).bind()
    }
  }

  private fun openFeedBackForm() {
    val manager = ReviewManagerFactory.create(activity)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { thisRequest ->
      if (thisRequest.isSuccessful) {
        val reviewInfo = thisRequest.result
        val flow = manager.launchReviewFlow(activity, reviewInfo)
        flow.addOnCompleteListener {
          Log.i(TAG, "review success!")
        }
      } else {
        Log.i(TAG, "review fail!")
      }
    }
  }

  companion object {
    const val TAG = "DishesFragmentRV"
  }
}

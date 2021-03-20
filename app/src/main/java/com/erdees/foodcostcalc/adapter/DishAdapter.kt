package com.erdees.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.calculatePrice
import com.erdees.foodcostcalc.formatPrice
import com.erdees.foodcostcalc.fragments.dialogs.EditDish
import com.erdees.foodcostcalc.model.GrandDish
import com.erdees.foodcostcalc.viewmodel.AddViewModel
import com.erdees.foodcostcalc.viewmodel.HalfProductsViewModel
import java.util.ArrayList


class DishAdapter(
    val tag: String?,
    private val list: ArrayList<GrandDish>,
    private val fragmentManager: FragmentManager,
    val viewModel: AddViewModel,
    val halfProductsViewModel: HalfProductsViewModel,
    val viewLifecycleOwner: LifecycleOwner,
    val activity: Activity
) : RecyclerView.Adapter<DishAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eachLinearLayout: LinearLayout = view.findViewById(R.id.linear_layout_dish_card)
        val dishNameTextView: TextView = view.findViewById(R.id.dish_name_in_adapter)
        val dishMarginTextView: TextView = view.findViewById(R.id.dish_margin_in_adapter)
        val dishTaxTextView: TextView = view.findViewById(R.id.dish_tax_in_adapter)
        val editButton: ImageButton = view.findViewById(R.id.edit_button_in_dish_adapter)
        val listView: ListView = view.findViewById(R.id.list_view)
        val totalPriceOfDish: TextView = view.findViewById(R.id.total_price_dish_card_view)
        val finalPriceWithMarginAndTax: TextView =
            view.findViewById(R.id.total_price_with_margin_dish_card_view)

        var totalPrice: Double = 0.0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val adapterLayout =
            LayoutInflater.from(parent.context).inflate(R.layout.dish_card_view, parent, false)
        return RecyclerViewHolder(adapterLayout)

    }

    override fun getItemCount(): Int {
        return list.size
    }


    @SuppressLint("WrongConstant", "ShowToast", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        /**Computes height of listView based on each row height, includes dividers.
         * I'm using this approach so listView size is set and doesn't need to be scrollable. */
        fun getListSize(): Int {
            var result = 0
            for (eachProduct in list[position].productsIncluded.indices + // first products included
                    list[position].halfProducts.indices) { // plus halfProducts
                val listItem = holder.listView.adapter.getView(eachProduct, null, holder.listView)
                listItem.measure(0, View.MeasureSpec.UNSPECIFIED)
                result += listItem.measuredHeight
            }
            return result + (holder.listView.dividerHeight * (holder.listView.adapter.count - 1))
        }


        fun countPriceAfterMarginAndTax(number: Double): Double {
         val priceWithMargin = number * list[position].dish.marginPercent / 100
            val amountOfTax = priceWithMargin * list[position].dish.dishTax / 100
            return priceWithMargin + amountOfTax
        }

        /**Summing up total price of products included and then one by one adding price of each half product,
         * after each call formats total price and sets totalPriceOfDish and finalPriceWithMarginAndTax*/

        /**To set correct price when there's no halfproducts.*/
        holder.totalPrice = list[position].totalPrice
        holder.totalPriceOfDish.text = formatPrice(holder.totalPrice)
        holder.finalPriceWithMarginAndTax.text = formatPrice(countPriceAfterMarginAndTax(holder.totalPrice))

        list[position].halfProducts.forEach {
            halfProductsViewModel
                .getCertainHalfProductWithProductsIncluded(it.halfProductOwnerId)
                .observe(viewLifecycleOwner, Observer { halfProductWithProductsIncluded ->
                    holder.totalPrice = (holder.totalPrice +
                            calculatePrice(halfProductWithProductsIncluded.pricePerUnit(),it.weight,
                            halfProductWithProductsIncluded.halfProduct.halfProductUnit,it.unit))
                   if(list[position].halfProducts.indexOf(it) == list[position].halfProducts.size - 1)//Text fields are changed only when price of last half product is added to total price.
                   {
                        holder.totalPriceOfDish.text =  formatPrice(holder.totalPrice)
                        holder.finalPriceWithMarginAndTax.text = formatPrice(countPriceAfterMarginAndTax(holder.totalPrice))
                    }
                })

        }
        holder.dishNameTextView.text = list[position].dish.name
        holder.dishMarginTextView.text = "Margin: ${list[position].dish.marginPercent}%"
        holder.dishTaxTextView.text = "Tax: ${list[position].dish.dishTax}%"
        holder.editButton.setOnClickListener {
            EditDish().show(fragmentManager, EditDish.TAG)
            EditDish.dishPassedFromAdapter = list[position]
        }

        /**Opening/closing list after click*/
        holder.eachLinearLayout.setOnClickListener {
            if (holder.listView.adapter == null) {
                holder.listView.adapter = DishListViewAdapter(
                    activity,
                    list[position],
                    halfProductsViewModel,
                    viewLifecycleOwner
                )
                holder.listView.layoutParams =
                    LinearLayout.LayoutParams(holder.listView.layoutParams.width, getListSize())
            } else {
                holder.listView.adapter = null
                holder.listView.layoutParams =
                    LinearLayout.LayoutParams(holder.listView.layoutParams.width, 0)
            }
        }

    }
}
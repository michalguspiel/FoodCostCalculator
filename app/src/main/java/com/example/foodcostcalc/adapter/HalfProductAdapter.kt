package com.example.foodcostcalc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcostcalc.R
import com.example.foodcostcalc.fragments.dialogs.EditDish
import com.example.foodcostcalc.fragments.dialogs.EditHalfProduct
import com.example.foodcostcalc.model.HalfProductWithProductsIncluded
import com.example.foodcostcalc.viewmodel.AddViewModel
import com.example.foodcostcalc.viewmodel.HalfProductsViewModel

class HalfProductAdapter(
    private val viewLifeCycleOwner: LifecycleOwner,
    private val list: ArrayList<HalfProductWithProductsIncluded>,
    private val fragmentManager: FragmentManager,
    val viewModel: AddViewModel,
    val halfProductViewModel: HalfProductsViewModel,
    val activity: Activity
) : RecyclerView.Adapter<HalfProductAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eachLinearLayout: LinearLayout = view.findViewById(R.id.linear_layout_dish_card)


        val halfProductName: TextView = view.findViewById(R.id.half_product_name_in_adapter)
        val editButton: ImageButton = view.findViewById(R.id.edit_button_in_dish_adapter)
        val listView: ListView = view.findViewById(R.id.list_view)
        val unitOfHalfProduct: TextView =
            view.findViewById(R.id.unit_to_populate_half_product_card_view)
        val finalPriceOfHalfProductPerUnit: TextView =
            view.findViewById(R.id.price_of_half_product_per_unit)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.half_product_card_view, parent, false)
        return RecyclerViewHolder(adapterLayout)
    }


    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {

        /**Computes height of listView based on each row height, includes dividers.
         * I'm using this approach so listView size is set and doesn't need to be scrollable. */
        fun getListSize(): Int {
            var result = 0
            for (eachProduct in list[position].halfProductsList.indices) {
                val listItem = holder.listView.adapter.getView(eachProduct, null, holder.listView)
                listItem.measure(0, View.MeasureSpec.UNSPECIFIED)
                result += listItem.measuredHeight
            }
            return result + (holder.listView.dividerHeight * (holder.listView.adapter.count - 1))
        }
        holder.halfProductName.text = list[position].halfProduct.name
        holder.unitOfHalfProduct.text = list[position].halfProduct.halfProductUnit
             halfProductViewModel
            .getCertainHalfProductWithProductsIncluded(list[position].halfProduct.halfProductId).observe(viewLifeCycleOwner,
                Observer { holder.finalPriceOfHalfProductPerUnit.text = it.formattedPricePerUnit })


        holder.editButton.setOnClickListener {
            EditHalfProduct().show(fragmentManager, EditHalfProduct.TAG)
            EditHalfProduct.halfProductPassedFromAdapter = list[position]
        }

        holder.eachLinearLayout.setOnClickListener {
            if (holder.listView.adapter == null) {
                holder.listView.adapter =
                    HalfProductListViewAdapter(activity, list[position].halfProductsList)
                holder.listView.layoutParams =
                    LinearLayout.LayoutParams(holder.listView.layoutParams.width, getListSize())
            } else {
                holder.listView.adapter = null
                holder.listView.layoutParams =
                    LinearLayout.LayoutParams(holder.listView.layoutParams.width, 0)
            }
        }

    }


    override fun getItemCount(): Int {
        return list.size
    }


}
package com.example.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcostcalc.R
import com.example.foodcostcalc.fragments.Dishes
import com.example.foodcostcalc.fragments.Products
import com.example.foodcostcalc.fragments.dialogs.EditDish
import com.example.foodcostcalc.fragments.dialogs.EditProduct
import java.util.ArrayList


class RecyclerViewAdapter<T>(val tag: String?, private val list: ArrayList<T>, private val fragmentManager: FragmentManager)
    : RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(view: View) :RecyclerView.ViewHolder(view){
        val textView: TextView = view.findViewById(R.id.product_title)
        val editButton: ImageButton = view.findViewById(R.id.edit_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val adapterLayout = when(tag ){
            Products.TAG    -> LayoutInflater.from(parent.context).inflate(R.layout.list_product,parent,false)
            Dishes.TAG      -> LayoutInflater.from(parent.context).inflate(R.layout.list_product,parent,false)
            else            -> LayoutInflater.from(parent.context).inflate(R.layout.small_list_products,parent,false)}
        return RecyclerViewHolder(adapterLayout)

    }

    override fun getItemCount(): Int {
        return list.size
    }


    @SuppressLint("WrongConstant", "ShowToast")
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
         holder.textView.text = list[position].toString()

         holder.editButton.setOnClickListener{
            if(this.tag == Products.TAG) {EditProduct().show(fragmentManager,EditProduct.TAG)
            EditProduct.position = position
            }
             else if(this.tag == Dishes.TAG) {EditDish().show(fragmentManager,EditDish.TAG)
            EditDish.position = position }
         }
    }




}
package com.example.foodcostcalc.fragments.dialogs

import android.os.Bundle
import android.renderscript.Allocation
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.example.foodcostcalc.InjectorUtils
import com.example.foodcostcalc.MainActivity
import com.example.foodcostcalc.R
import com.example.foodcostcalc.fragments.AddViewModel
import com.example.foodcostcalc.fragments.AddViewModelFactory

class AreYouSure : DialogFragment(){


    override fun onStop() {
        super.onStop()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val view: View =  inflater.inflate(R.layout.fragment_dialog_are_you_sure,container,false)
        fun initilizeUi(){
            val factory: AddViewModelFactory = InjectorUtils.provideAddViewModelFactory()
            val viewModel: AddViewModel = ViewModelProviders.of(this,factory).get(AddViewModel::class.java)

       /** Binders*/
            val confirmBtn = view.findViewById<Button>(R.id.button_yes)
            val cancelBtn = view.findViewById<Button>(R.id.button_cancel)


         /**position of product to delete */
            var pos:Int? = null
            var secondPos: Int? = null
        /**Observe data */
          viewModel.getPosition().observe(this, Observer { position ->
              pos = position
          })
            viewModel.getSecondPosition().observe(this, Observer { position ->
                secondPos = position
            })

        /**Button function */


            confirmBtn.setOnClickListener{
                viewModel.setFlag(false)
                 when(this.tag) {
                 EditProduct.TAG ->   pos?.let { it1 -> viewModel.getProducts().value?.get(it1)?.let { it1 -> viewModel.deleteProduct(it1) } }
                    EditDish.TAG -> pos?.let { it1 -> viewModel.getDishes().value?.get(it1)?.let { it1 -> viewModel.deleteDish(it1) } }
                     "EditDishAdapter" -> {
                         viewModel.getDishes().value?.get(pos!!)?.let { it1 ->
                                 viewModel.deleteProductFromDish(it1,
                                         it1.productsIncluded[secondPos!!])
                             }
                         }
                         else -> this.dismiss()
                }
                this.dismiss()
                }


            cancelBtn.setOnClickListener{
                this.dismiss()
            }


        }
        initilizeUi()
        return view
    }

    companion object {
        fun newInstance():AreYouSure =
            AreYouSure()
            const val TAG = "AreYouSure"

    }
}
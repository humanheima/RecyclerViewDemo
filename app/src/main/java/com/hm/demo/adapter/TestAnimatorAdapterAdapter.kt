package com.hm.demo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.model.CheckBoxModel

/**
 * Created by dumingwei on 2017/10/10.
 */
class TestAnimatorAdapterAdapter(
    private val context: Context
) : RecyclerView.Adapter<TestAnimatorAdapterAdapter.ViewHolder>() {

    companion object {
        val TYPE_FOOTER = 1
        private const val TAG = "TestAnimatorAdapterAdap"
    }

    val dataList = mutableListOf<CheckBoxModel>()

    fun onDataSourceChanged(dataList: MutableList<CheckBoxModel>) {
        this.dataList.clear()
        this.dataList.addAll(dataList)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        if (viewType == TYPE_FOOTER) {
            val view =
                LayoutInflater.from(context).inflate(R.layout.footer_view_load_more, parent, false)
            return FooterViewHolder(view)
        }
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_test_animation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == itemCount - 1) {
            return
        }
        val model = dataList[position]
        holder.checkBox?.isSelected = model.isChecked
        holder.textDescription?.text = model.description
        Log.i(TAG, "onBindViewHolder: position = $position holder = $holder model = $model")
    }

    override fun getItemCount(): Int {
        return dataList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == itemCount - 1) {
            return TYPE_FOOTER
        }
        return super.getItemViewType(position)
    }


    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var checkBox: CheckBox? = null
        var textDescription: TextView? = null

        init {
            checkBox = itemView.findViewById(R.id.check_box)
            textDescription = itemView.findViewById(R.id.text_description)
        }
    }

    class FooterViewHolder(itemView: View) : ViewHolder(itemView) {

//        var textFooter: TextView
//
//        init {
//            textFooter = itemView.findViewById(R.id.footer_view_load_now)
//        }
    }

}

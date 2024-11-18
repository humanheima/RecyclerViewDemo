package com.hm.demo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.model.CheckBoxModel

/**
 * Created by dumingwei on 2017/10/10.
 */
class TestRvTheoryAdapter(
    private val context: Context,
    private val data: MutableList<CheckBoxModel>?
) : RecyclerView.Adapter<TestRvTheoryAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "TestAnimatorAdapterAdap"
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_test_rv_theory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = data!![position]
        holder.checkBox.isSelected = model.isChecked
        holder.textDescription.text = model.description
        Log.i(TAG, "onBindViewHolder: position = $position holder = $holder model = $model")

        holder.layoutPosition
        holder.itemView.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "点击了${model.description} position = $position layoutPosition = ${holder.layoutPosition} " +
                        " adapterPosition = ${holder.absoluteAdapterPosition} bindingPosition = ${holder.bindingAdapterPosition}",
                Toast.LENGTH_SHORT
            ).show()

            Log.d(
                TAG,
                "onBindViewHolder: 点击了 ${model.description} position = $position layoutPosition = ${holder.layoutPosition} " +
                        " absoluteAdapterPosition = ${holder.absoluteAdapterPosition} bindingPosition = ${holder.bindingAdapterPosition}"
            )
        }
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    fun getDataList(): MutableList<CheckBoxModel>? {
        return data
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var checkBox: CheckBox
        var textDescription: TextView

        init {
            checkBox = itemView.findViewById(R.id.check_box)
            textDescription = itemView.findViewById(R.id.text_description)
        }

    }

}

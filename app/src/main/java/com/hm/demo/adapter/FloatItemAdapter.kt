package com.hm.demo.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hm.demo.R
import com.hm.demo.model.TestBean
import kotlinx.android.synthetic.main.item_diff.view.*

/**
 * Created by dumingwei on 2020/4/27.
 *
 * Desc:
 */
class FloatItemAdapter(
        val context: Context,
        val dataList: List<TestBean>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val normalType = 100
    val floatType = 200

    class NormalVH(view: View) : RecyclerView.ViewHolder(view) {


    }

    class FloatVH(view: View) :RecyclerView.ViewHolder(view) {

    }

    override fun getItemViewType(position: Int): Int {
        if (position == 2) {
            return floatType
        }
        return normalType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        if (viewType == floatType) {
            val view = LayoutInflater.from(context).inflate(R.layout.view_float_layout, parent, false)
            return FloatVH(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_diff, parent, false)
            return NormalVH(view)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val data = dataList[position]
        if (holder is NormalVH) {
            holder.itemView.tv1.text = data.name
            holder.itemView.tv2.text = data.desc
            holder.itemView.iv.setImageResource(data.picture)
        } else if (holder is FloatVH) {
            // do nothing
        }
    }
}
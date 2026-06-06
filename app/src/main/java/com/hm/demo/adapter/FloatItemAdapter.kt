package com.hm.demo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.model.TestBean

/**
 * Created by dumingwei on 2020/4/27.
 *
 * Desc:
 */
class FloatItemAdapter(
    val context: Context,
    val dataList: List<TestBean>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val normalType = 100
    val floatType = 200

    companion object {

        private const val TAG = "FloatItemAdapter"

    }

    override fun getItemViewType(position: Int): Int {
        if (position == 2) {
            return floatType
        }
        return normalType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == floatType) {
            val view =
                LayoutInflater.from(context).inflate(R.layout.view_float_layout, parent, false)
            return FloatVH(view)
        } else {
            val binding = LayoutInflater.from(context).inflate(R.layout.item_diff, parent, false)
            return NormalVH(binding.rootView)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = dataList[position]
        if (holder is NormalVH) {
            holder.tv1.text = data.name
            holder.tv2.text = data.desc
            holder.iv.setImageResource(data.picture)
        } else if (holder is FloatVH) {

            // do nothing
        }
    }

    class NormalVH(view: View) : RecyclerView.ViewHolder(view) {
        val tv1: TextView = view.findViewById(R.id.tv1)
        val tv2: TextView = view.findViewById(R.id.tv2)
        val iv: ImageView = view.findViewById(R.id.iv)


    }

    class FloatVH(view: View) : RecyclerView.ViewHolder(view) {

    }
}
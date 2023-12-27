package com.hm.demo.test_diff

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.model.TestBean

/**
 * Created by p_dmweidu on 2023/12/27
 * Desc: 测试DiffUtil
 */
class MyDiffAdapter(diffCallback: MyDiffCallback) :
    ListAdapter<TestBean, MyDiffAdapter.ViewHolder>(diffCallback) {

    private val TAG = "MyDiffAdapter"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_diff_util, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position in 0 until itemCount) {
            val bean: TestBean = getItem(position)
            Log.i(TAG, "onBindViewHolder: position = $position name = ${bean.name}")
            holder.iv?.setImageResource(bean.picture)
            holder.tvName?.text = bean.name
            holder.tvDesc?.text = bean.desc
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var iv: ImageView? = null
        var tvName: TextView? = null
        var tvDesc: TextView? = null

        init {
            tvName = itemView.findViewById(R.id.tvName)
            tvDesc = itemView.findViewById(R.id.tvDesc)
            iv = itemView.findViewById(R.id.iv)
        }

    }

}
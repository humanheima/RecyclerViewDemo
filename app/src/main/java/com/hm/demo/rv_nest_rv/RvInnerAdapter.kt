package com.hm.demo.rv_nest_rv

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.test_payload.PayloadBean

/**
 * Created by p_dmweidu on 2024/11/11
 * Desc:
 */
class RvInnerAdapter(private val items: List<PayloadBean>) :
    RecyclerView.Adapter<RvInnerAdapter.InnerViewHolder>() {

    private val TAG = "RvInnerAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
        Log.d(TAG, "onCreateViewHolder: ")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inner_rv_item, parent, false)
        return InnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: position = $position")
    }

    override fun onBindViewHolder(holder: InnerViewHolder, position: Int, payloads: List<Any>) {
        //Log.e(TAG, "onBindViewHolder: 3个参数的方法${Log.getStackTraceString(Throwable())}")
        Log.e(TAG, "onBindViewHolder: 3个参数的方法")
        // 全量更新
        val item = items[position]
        holder.textView1.text = item.text1
        holder.textView2.text = item.text2
    }

    override fun getItemCount(): Int = items.size

    class InnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv: ImageView = itemView.findViewById(R.id.ivLeft)
        val textView1: TextView = itemView.findViewById(R.id.tv1)
        val textView2: TextView = itemView.findViewById(R.id.tv2)

    }
}
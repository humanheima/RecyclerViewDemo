package com.hm.demo.test_payload

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R

class PayloadTestAdapter(private val items: List<PayloadBean>) : RecyclerView.Adapter<PayloadTestAdapter.MyViewHolder>() {


    private val TAG = "MyAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test_payload, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: ")
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: List<Any>) {
        Log.d(TAG, "onBindViewHolder: ${Log.getStackTraceString(Throwable())}")
        if (payloads.isNotEmpty()) {
            Log.d(TAG, "onBindViewHolder: payloads = $payloads  position = $position")
            // 处理 payloads
            for (payload in payloads) {
                if (payload is String) {
                    holder.textView1.text = payload
                }
            }
        } else {
            Log.d(TAG, "onBindViewHolder: 全量更新 position = $position")
            // 全量更新
            val item = items[position]
            holder.textView1.text = item.text1
            holder.textView2.text = item.text2
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * 使用 payload notifyItemChanged，只更新部分View
     */
    fun updateItemTextPayload(position: Int, newText: String) {
        //注意，这里数据也要变化
        items[position].text1 = newText
        // 使用 payload 更新
        notifyItemChanged(position, newText)
    }


    /**
     * 使用 notifyItemChanged，不使用payload
     */
    fun updateItemText(position: Int, newText: String) {
        //注意，这里数据也要变化
        items[position].text1 = newText
        notifyItemChanged(position)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv: ImageView = itemView.findViewById(R.id.ivLeft)
        val textView1: TextView = itemView.findViewById(R.id.tv1)
        val textView2: TextView = itemView.findViewById(R.id.tv2)

    }
}
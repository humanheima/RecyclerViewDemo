package com.hm.demo.rv_nest_rv

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R

/**
 * Created by p_dmweidu on 2024/11/11
 * Desc:
 */
class RvOutAdapter(private val items: List<TestRvNestRVBean>) :
    RecyclerView.Adapter<RvOutAdapter.OutViewHolder>() {


    private val TAG = "RvOutAdapter"

    private val recyclerViewPool = RecyclerView.RecycledViewPool().apply {
        //设置RecyclerViewPool中最大缓存数量，默认每个ItemViewType 5个。
        //setMaxRecycledViews(0, 10)
    }

    private val viewCacheExtension: RecyclerView.ViewCacheExtension =
        object : RecyclerView.ViewCacheExtension() {

            override fun getViewForPositionAndType(
                recycler: RecyclerView.Recycler,
                position: Int,
                type: Int
            ): View? {
                return null
            }

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutViewHolder {
        Log.d(TAG, "onCreateViewHolder: ")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rv_item, parent, false)
        return OutViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        //return super.getItemViewType(position)
        return 0;
    }

    override fun onBindViewHolder(holder: OutViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: ")
    }

    override fun onBindViewHolder(holder: OutViewHolder, position: Int, payloads: List<Any>) {
        val item = items[position]
        holder.tvOutParent.text = item.text1

        val rvInnerAdapter = item.children?.let { RvInnerAdapter(it) }

        holder.rvInner.setRecycledViewPool(recyclerViewPool)
        holder.rvInner.setViewCacheExtension(viewCacheExtension)

        holder.rvInner.adapter = rvInnerAdapter
        holder.rvInner.layoutManager = LinearLayoutManager(holder.rvInner.context)

        holder.rvInner.addRecyclerListener(object : RecyclerView.RecyclerListener {
            override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
                Log.e(
                    TAG,
                    "rvInner onViewRecycled: ViewHolder = $holder ${
                        Log.getStackTraceString(
                            Throwable()
                        )
                    }"
                )
            }
        })

    }

    override fun getItemCount(): Int = items.size

    class OutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOutParent: TextView = itemView.findViewById(R.id.tvOutParent)
        val rvInner: RecyclerView = itemView.findViewById(R.id.rvInner)
    }

}
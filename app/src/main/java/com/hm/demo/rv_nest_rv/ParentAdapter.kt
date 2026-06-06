package com.hm.demo.rv_nest_rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import com.hm.demo.R

class ParentAdapter(private val data: List<ParentItem>) : RecyclerView.Adapter<ParentAdapter.ParentViewHolder>() {

    private val viewPool = RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_parent, parent, false)
        return ParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item, viewPool)
    }

    override fun getItemCount(): Int = data.size

    class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.tv_title)
        private val childRv: RecyclerView = itemView.findViewById(R.id.recycler_child)

        fun bind(item: ParentItem, pool: RecycledViewPool) {
            titleTv.text = item.title
            val layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            childRv.layoutManager = layoutManager
            childRv.setRecycledViewPool(pool)
            childRv.adapter = ChildAdapter(item.children)
        }
    }
}

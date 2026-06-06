package com.hm.demo.rv_nest_rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R

class ChildAdapter(private val data: List<String>) : RecyclerView.Adapter<ChildAdapter.ChildViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_child, parent, false)
        return ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tv_child)
        fun bind(text: String) {
            textView.text = text
        }
    }
}

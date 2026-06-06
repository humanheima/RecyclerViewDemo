package com.hm.demo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.model.CheckBoxModel

/**
 * Created by dumingwei on 2017/10/10.
 */
class TestPathLayoutManagerAdapter(
    private val context: Context,
    private val data: MutableList<CheckBoxModel>?
) : RecyclerView.Adapter<TestPathLayoutManagerAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "TestPathLayoutManagerAd"

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.item_test_path_layout_manager, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = data!![position]
        holder.imageView.setImageResource(R.drawable.ic_balloon)
        holder.textDescription.text = model.description
        Log.i(TAG, "onBindViewHolder: position = $position holder = $holder model = $model")
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    fun getDataList(): MutableList<CheckBoxModel>? {
        return data
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.main_cover_iv)
        var textDescription: TextView = itemView.findViewById(R.id.main_title_tv)

    }

}

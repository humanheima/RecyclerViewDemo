package com.hm.demo.widget

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by dumingwei on 2021/6/20.
 *
 * Desc:
 */
class OddEvenLayoutManager : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}
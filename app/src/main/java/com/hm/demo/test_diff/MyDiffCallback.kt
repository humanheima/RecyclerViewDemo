package com.hm.demo.test_diff

import androidx.recyclerview.widget.DiffUtil
import com.hm.demo.model.TestBean


/**
 * Created by p_dmweidu on 2023/12/27
 * Desc:
 */
class MyDiffCallback : DiffUtil.ItemCallback<TestBean>() {


    override fun areItemsTheSame(oldItem: TestBean, newItem: TestBean): Boolean {
        return oldItem.equals(newItem)
    }

    override fun areContentsTheSame(oldItem: TestBean, newItem: TestBean): Boolean {
        return oldItem.equals(newItem)
    }


}

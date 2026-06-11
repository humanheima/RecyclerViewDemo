package com.hm.demo.test_diff

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.hm.demo.model.TestBean


/**
 * Created by p_dmweidu on 2023/12/27
 * Desc:
 */
class MyDiffCallback : DiffUtil.ItemCallback<TestBean>() {

    companion object {
        const val KEY_DESC = "KEY_DESC"
        const val KEY_PIC = "KEY_PIC"
    }


    /**
     * 两个 item 是否是“同一个对象”（通常比较唯一 id）。
     */
    override fun areItemsTheSame(oldItem: TestBean, newItem: TestBean): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * 在 `areItemsTheSame` 为 true 的前提下，内容是否相同（决定是否要刷新）。
     */
    override fun areContentsTheSame(oldItem: TestBean, newItem: TestBean): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: TestBean, newItem: TestBean): Any? {
        val payload = Bundle()
        if (oldItem.desc != newItem.desc) {
            payload.putString(KEY_DESC, newItem.desc)
        }
        if (oldItem.picture != newItem.picture) {
            payload.putInt(KEY_PIC, newItem.picture)
        }
        return if (payload.isEmpty) null else payload
    }


}

package com.hm.demo.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * 水平RecyclerView的间距装饰器
 */
class HorizontalSpaceItemDecoration(
    private val spaceSize: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        when (position) {
            0 -> {
                // 第一个item，左边添加额外间距，右边正常间距
                outRect.left = spaceSize * 2
                outRect.right = spaceSize / 2
            }
            itemCount - 1 -> {
                // 最后一个item，右边添加额外间距，左边正常间距
                outRect.left = spaceSize / 2
                outRect.right = spaceSize * 2
            }
            else -> {
                // 中间的item，左右都是正常间距的一半
                outRect.left = spaceSize / 2
                outRect.right = spaceSize / 2
            }
        }
    }
}
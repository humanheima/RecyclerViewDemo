package com.hm.demo.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by p_dmweidu on 2022/6/9
 * Desc: 将指定 position 滑动到RecyclerView 顶端的 RecyclerView
 *
 * 参考链接：https://blog.csdn.net/shanshan_1117/article/details/78780137
 */
class ScrollToPositionRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.recyclerview.widget.RecyclerView(context, attrs, defStyleAttr) {

    private var mShouldScroll = false

    //记录目标项位置
    private var mToPosition = -1

    companion object {

        private const val TAG = "ScrollToTopRecyclerView"

    }

    init {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (mShouldScroll && SCROLL_STATE_IDLE == newState) {
                    mShouldScroll = false
                    smoothMoveToPosition(mToPosition);
                }
            }
        })

        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    /**
                     * 收到down事件后，取消自动滑动
                     */
                    mShouldScroll = false
                }
            }
            //返回false，让RecyclerView继续处理事件
            false
        }
    }

    /**
     * 滑动到指定位置
     */
    fun smoothMoveToPosition(position: Int) {
        if (position < 0) {
            return
        }
        // 第一个可见位置
        val firstItem = getChildLayoutPosition(getChildAt(0))
        // 最后一个可见位置
        val lastItem =
            getChildLayoutPosition(getChildAt(childCount - 1))
        if (position < firstItem) {
            // 第一种可能:跳转位置在第一个可见位置之前
            smoothScrollToPosition(position)
        } else if (position <= lastItem) {
            // 第二种可能:跳转位置在第一个可见位置之后
            val movePosition = position - firstItem
            if (movePosition in 0 until childCount) {
                val top = getChildAt(movePosition).top
                smoothScrollBy(0, top)
            }
        } else {
            // 第三种可能:跳转位置在最后可见项之后
            smoothScrollToPosition(position)
            mToPosition = position
            mShouldScroll = true
        }
    }


}
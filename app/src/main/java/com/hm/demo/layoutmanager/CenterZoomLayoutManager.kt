package com.hm.demo.layoutmanager

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.min

/**
 * 支持中间item缩放效果和循环滑动的LayoutManager
 */
class CenterZoomLayoutManager(
    context: Context,
    private val maxScale: Float = 1.3f,  // 最大缩放比例
    private val minScale: Float = 1.0f,  // 最小缩放比例
    private val enableLoop: Boolean = true  // 是否启用循环效果
) : LinearLayoutManager(context, HORIZONTAL, false) {

    // 用于实现循环效果的虚拟item数量
    private val loopMultiplier = 1000
    private var realItemCount = 0

    private var isFirstLayout = true
    
    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        scaleChildren()
        
        // 如果启用循环且是第一次布局，则滚动到中间位置
        if (enableLoop && realItemCount > 0 && isFirstLayout) {
            isFirstLayout = false
            val centerPosition = getCenterLoopPosition()
            scrollToPositionWithOffset(centerPosition, 0)
        }
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        val scrolled = super.scrollHorizontallyBy(dx, recycler, state)
        scaleChildren()
        
        // 处理循环滚动
        if (enableLoop) {
            handleLoopScroll()
        }
        
        return scrolled
    }

    /**
     * 处理循环滚动逻辑
     */
    private fun handleLoopScroll() {
        if (realItemCount <= 0) return
        
        val firstVisiblePosition = findFirstVisibleItemPosition()
        val lastVisiblePosition = findLastVisibleItemPosition()
        val totalItems = loopMultiplier * realItemCount
        val centerPosition = totalItems / 2
        
        // 当滚动到接近边界时，跳转到循环位置
        if (firstVisiblePosition != RecyclerView.NO_POSITION && lastVisiblePosition != RecyclerView.NO_POSITION) {
            // 检查是否接近左边界（前25%区域）
            if (firstVisiblePosition < centerPosition - realItemCount * 100) {
                // 滚动到了左边界，跳转到中间偏右的对应位置
                val firstChild = getChildAt(0)
                val offset = firstChild?.let { getDecoratedLeft(it) } ?: 0
                val targetPosition = firstVisiblePosition + realItemCount * 200
                scrollToPositionWithOffset(targetPosition, offset)
            } 
            // 检查是否接近右边界（后25%区域）
            else if (lastVisiblePosition > centerPosition + realItemCount * 100) {
                // 滚动到了右边界，跳转到中间偏左的对应位置
                val firstChild = getChildAt(0)
                val offset = firstChild?.let { getDecoratedLeft(it) } ?: 0
                val targetPosition = firstVisiblePosition - realItemCount * 200
                scrollToPositionWithOffset(targetPosition, offset)
            }
        }
    }

    /**
     * 获取循环模式下的中心位置
     */
    private fun getCenterLoopPosition(): Int {
        return if (enableLoop && realItemCount > 0) {
            // 从中间位置开始，确保可以向两个方向循环
            (loopMultiplier / 2) * realItemCount
        } else 0
    }

    /**
     * 对所有可见的子View进行缩放
     */
    private fun scaleChildren() {
        val midpoint = width / 2f
        val d0 = 0f
        val d1 = 0.9f * midpoint
        val s0 = maxScale
        val s1 = minScale

        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val childMidpoint = (getDecoratedRight(child) + getDecoratedLeft(child)) / 2f
            val d = min(d1, abs(midpoint - childMidpoint))
            val scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0)
            
            child.scaleX = scale
            child.scaleY = scale
        }
    }

    /**
     * 在滚动停止时自动将最近的item居中
     */
    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            scrollToCenter()
        }
    }

    /**
     * 将距离中心最近的item滚动到中心位置
     */
    private fun scrollToCenter() {
        val recyclerView = getRecyclerView() ?: return
        val midpoint = width / 2f
        var closestChild: View? = null
        var closestDistance = Float.MAX_VALUE

        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val childMidpoint = (getDecoratedRight(child) + getDecoratedLeft(child)) / 2f
            val distance = abs(midpoint - childMidpoint)
            
            if (distance < closestDistance) {
                closestDistance = distance
                closestChild = child
            }
        }

        closestChild?.let { child ->
            val position = getPosition(child)
            val smoothScroller = CenterSmoothScroller(recyclerView.context)
            smoothScroller.targetPosition = position
            startSmoothScroll(smoothScroller)
        }
    }

    /**
     * 设置数据项数量（用于循环计算）
     */
    fun setRealItemCount(count: Int) {
        realItemCount = count
    }

    /**
     * 获取真实的position（去除循环效果）
     */
    fun getRealPosition(position: Int): Int {
        return if (enableLoop && realItemCount > 0) {
            position % realItemCount
        } else position
    }

    /**
     * 获取当前中心item的真实position
     */
    fun getCurrentCenterPosition(): Int {
        val centerX = width / 2f
        var closestPosition = 0
        var closestDistance = Float.MAX_VALUE

        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val childCenterX = (getDecoratedRight(child) + getDecoratedLeft(child)) / 2f
            val distance = abs(centerX - childCenterX)
            
            if (distance < closestDistance) {
                closestDistance = distance
                closestPosition = getPosition(child)
            }
        }
        
        return getRealPosition(closestPosition)
    }

    /**
     * 获取RecyclerView实例
     */
    private fun getRecyclerView(): RecyclerView? {
        return try {
            val field = LinearLayoutManager::class.java.getDeclaredField("mRecyclerView")
            field.isAccessible = true
            field.get(this) as? RecyclerView
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 自定义SmoothScroller，确保滚动到中心位置
     */
    private inner class CenterSmoothScroller(context: Context) : LinearSmoothScroller(context) {
        override fun calculateDtToFit(
            viewStart: Int,
            viewEnd: Int,
            boxStart: Int,
            boxEnd: Int,
            snapPreference: Int
        ): Int {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
        }
    }
}
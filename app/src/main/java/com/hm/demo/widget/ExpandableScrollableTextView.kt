package com.hm.demo.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.abs

class ExpandableScrollableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var isExpanded = false
    private val maxCollapsedLines = 4

    // 滑动相关
    private var scroller: OverScroller = OverScroller(context)
    private var velocityTracker: VelocityTracker? = null
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val minFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val maxFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity

    private var lastY = 0f
    private var lastX = 0f
    private var initialY = 0f
    private var initialX = 0f
    private var isDragging = false
    private var maxScrollY = 0

    init {
        // 设置可滚动
        isVerticalScrollBarEnabled = true
        overScrollMode = OVER_SCROLL_NEVER
    }

    /**
     * 设置展开/收起状态
     */
    fun setExpanded(expanded: Boolean) {
        if (isExpanded != expanded) {
            isExpanded = expanded
            scrollTo(0, 0)
            requestLayout()
        }
    }

    /**
     * 切换展开/收起状态
     */
    fun toggleExpanded() {
        setExpanded(!isExpanded)
    }

    /**
     * 获取当前展开状态
     */
    fun isExpanded(): Boolean = isExpanded

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (!isExpanded && lineCount > maxCollapsedLines) {
            // 收起状态：限制为4行高度
            val lineHeight = lineHeight
            val targetHeight = paddingTop + paddingBottom + lineHeight * maxCollapsedLines
            setMeasuredDimension(measuredWidth, targetHeight)

            // 计算最大滚动距离
            val totalTextHeight = lineCount * lineHeight
            maxScrollY = totalTextHeight - (targetHeight - paddingTop - paddingBottom)
        } else {
            // 展开状态：使用完整高度
            maxScrollY = 0
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // 展开状态或内容不超过4行时，不处理滑动拦截
        if (isExpanded || lineCount <= maxCollapsedLines) {
            return super.dispatchTouchEvent(event)
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // DOWN事件让父View不要拦截，子View有机会处理
                parent.requestDisallowInterceptTouchEvent(true)
                lastY = event.y
                lastX = event.x
                initialY = event.y
                initialX = event.x
                isDragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                val deltaY = (lastY - y).toInt()
                val deltaX = x - initialX
                val deltaYTotal = initialY - y

                if (!isDragging) {
                    // 判断是否开始拖动
                    if (abs(deltaYTotal) > touchSlop || abs(deltaX) > touchSlop) {
                        // 判断滑动方向
                        if (abs(deltaYTotal) > abs(deltaX)) {
                            // 垂直滑动
                            isDragging = true

                            // 判断是否需要拦截事件
                            val canScrollDown = scrollY > 0 && deltaYTotal < 0 // 向下滑动且未到顶部
                            val canScrollUp = scrollY < maxScrollY && deltaYTotal > 0 // 向上滑动且未到底部

                            if (canScrollDown || canScrollUp) {
                                // 可以滚动，不让父View拦截
                                parent.requestDisallowInterceptTouchEvent(true)
                            } else {
                                // 不能滚动，让父View拦截
                                parent.requestDisallowInterceptTouchEvent(false)
                            }
                        } else {
                            // 水平滑动，让父View拦截
                            parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                } else {
                    // 已经在拖动中，检查边界
                    val newScrollY = scrollY + deltaY

                    // 边界处理：到达边界时让父View可以拦截
                    if ((deltaY > 0 && newScrollY >= maxScrollY) || (deltaY < 0 && newScrollY <= 0)) {
                        parent.requestDisallowInterceptTouchEvent(false)
                    } else {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }

                lastY = y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 展开状态或内容不超过4行时，不处理滑动
        if (isExpanded || lineCount <= maxCollapsedLines) {
            return super.onTouchEvent(event)
        }

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!scroller.isFinished) {
                    scroller.abortAnimation()
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val y = event.y
                    val deltaY = (lastY - y).toInt()

                    // 使用 scrollBy 执行滑动
                    scrollBy(0, deltaY)
                    lastY = y
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    velocityTracker?.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                    val yVelocity = velocityTracker?.yVelocity?.toInt() ?: 0

                    if (abs(yVelocity) > minFlingVelocity) {
                        scroller.fling(
                            0, scrollY,
                            0, -yVelocity,
                            0, 0,
                            0, maxScrollY
                        )
                        postInvalidateOnAnimation()
                    }
                }

                velocityTracker?.recycle()
                velocityTracker = null
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.recycle()
                velocityTracker = null
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(0, scroller.currY)
            postInvalidateOnAnimation()
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        // 限制滚动范围
        val clampedY = y.coerceIn(0, maxScrollY)
        super.scrollTo(x, clampedY)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        velocityTracker?.recycle()
        velocityTracker = null
    }
}
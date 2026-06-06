package com.hm.demo.widget;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by p_dmweidu on 2025/9/25
 * Desc:支持展开和收起功能的TextView（收起时最大4行），使用内部拦截法处理触摸事件并实现fling
 */
public class ExpandableTextView3 extends AppCompatTextView {

    private static final String TAG = "ExpandableTextView";
    // 收起状态最大行数
    private static final int COLLAPSED_MAX_LINES = 10;
    // 是否处于展开状态
    private boolean isExpanded = false;
    // 原始最大行数（用于恢复）
    private int originalMaxLines = Integer.MAX_VALUE;

    // 展开/收起状态变化监听器
    private OnExpandStateChangeListener expandStateChangeListener;

    // Fling相关变量
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private boolean isFlinging = false;

    public ExpandableTextView3(Context context) {
        super(context);
        init();
    }

    public ExpandableTextView3(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExpandableTextView3(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 保存原始最大行数配置
        //originalMaxLines = getMaxLines();
        // 默认初始化为收起状态
        setCollapsed();
        // 设置点击事件切换展开/收起状态
        setOnClickListener(v -> toggleExpandState());

        // 初始化fling相关组件
        mScroller = new Scroller(getContext());
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    /**
     * 切换展开/收起状态
     */
    public void toggleExpandState() {
        if (isExpanded) {
            setCollapsed();
        } else {
            setExpanded();
        }
        // 停止fling动画
        if (isFlinging && !mScroller.isFinished()) {
            mScroller.abortAnimation();
            isFlinging = false;
        }
    }

    /**
     * 设置为收起状态
     */
    public void setCollapsed() {
        Log.d(TAG, "setCollapsed: this = " + this);
        isExpanded = false;
        setMaxLines(COLLAPSED_MAX_LINES);
        if (expandStateChangeListener != null) {
            expandStateChangeListener.onStateChanged(false);
        }
        requestLayout();

    }

    /**
     * 设置为展开状态
     */
    public void setExpanded() {
        Log.d(TAG, "setExpanded: this = " + this);
        isExpanded = true;
        // 恢复原始最大行数（不限制）
        setMaxLines(originalMaxLines);
        if (expandStateChangeListener != null) {
            expandStateChangeListener.onStateChanged(true);
        }
        requestLayout();
    }

    /**
     * 判断是否处于展开状态
     */
    public boolean isExpanded() {
        return isExpanded;
    }

    /**
     * 设置展开状态变化监听器
     */
    public void setOnExpandStateChangeListener(OnExpandStateChangeListener listener) {
        this.expandStateChangeListener = listener;
    }

    /**
     * 展开/收起状态变化监听器接口
     */
    public interface OnExpandStateChangeListener {
        void onStateChanged(boolean isExpanded);
    }

    // 触摸事件分发逻辑保持不变
    private int lastX;
    private int lastY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isExpanded) {
            return super.dispatchTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = (int) ev.getY();
                lastX = (int) ev.getX();
                // 请求父控件（RecyclerView）不拦截触摸事件
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                int y = (int) ev.getY();
                int x = (int) ev.getX();
                int deltaY = y - lastY;
                int deltaX = x - lastX;

                if (isAtBottom() && deltaY <= 0) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else if (isAtTop() && deltaY >= 0) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        lastY = (int) ev.getY();
        lastX = (int) ev.getX();
        return super.dispatchTouchEvent(ev);
    }

    // 触摸事件处理（添加fling逻辑）
    private int touchY = 0;
    private int touchX = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int deltaX;
        int deltaY = 0;
        boolean used = false;

        // 初始化或更新速度追踪器
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 停止当前fling
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    isFlinging = false;
                }
                touchX = (int) event.getX();
                touchY = (int) event.getY();
                used = true;
                break;

            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getX();
                int y = (int) event.getY();
                deltaX = x - touchX;
                deltaY = y - touchY;

                if (deltaY > 0 && !isAtTop()) {
                    int newScrollY = getScrollY() - deltaY;
                    newScrollY = Math.max(newScrollY, 0);
                    scrollTo(0, newScrollY);
                    used = true;
                } else if (deltaY < 0 && !isAtBottom()) {
                    int newScrollY = getScrollY() + (-deltaY);
                    newScrollY = Math.min(newScrollY, getMaxScrollDistance());
                    scrollTo(0, newScrollY);
                    used = true;
                } else {
                    used = false;
                }
                break;

            case MotionEvent.ACTION_UP:
                // 计算滑动速度并触发fling
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) mVelocityTracker.getYVelocity();

                if (Math.abs(initialVelocity) > mMinimumVelocity) {
                    fling(-initialVelocity);
                    used = true;
                }
                recycleVelocityTracker();
                break;

            case MotionEvent.ACTION_CANCEL:
                if (isFlinging && !mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    isFlinging = false;
                }
                recycleVelocityTracker();
                break;
        }

        touchX = (int) event.getX();
        touchY = (int) event.getY();

        return used || isFlinging;
    }

    // Fling相关方法
    private void fling(int velocityY) {
        if (getLayout() == null) {
            return;
        }

        int maxScroll = getMaxScrollDistance();
        int currentScroll = getScrollY();

        mScroller.fling(
                0, currentScroll,
                0, velocityY,
                0, 0,
                0, maxScroll
        );

        isFlinging = true;
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        } else if (isFlinging) {
            isFlinging = false;
        }
    }

    // 速度追踪器管理
    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private String getEventName(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return "ACTION_DOWN";
            case MotionEvent.ACTION_MOVE:
                return "ACTION_MOVE";
            case MotionEvent.ACTION_UP:
                return "ACTION_UP";
            case MotionEvent.ACTION_CANCEL:
                return "ACTION_CANCEL";
            default:
                return "ACTION_UNKNOWN";
        }
    }

    private boolean isAtBottom() {
        return getScrollY() + getHeight() >= getLayout().getHeight();
    }

    private boolean isAtTop() {
        return getScrollY() == 0;
    }

    public int getDisplayedContentHeight() {
        int height = getHeight();
        Log.d(TAG, "getDisplayedContentHeight: height = " + height);
        return height;
    }

    public int getMaxScrollDistance() {
        Layout layout = getLayout();
        if (layout == null) {
            return 0;
        }
        int contentHeight = layout.getHeight();
        int viewHeight = getHeight();
        int max = Math.max(0, contentHeight - viewHeight);
        Log.d(TAG, "getMaxScrollDistance: contentHeight = " + contentHeight + " , viewHeight = " + viewHeight + " , max = " + max);
        return max;
    }

    public int getFullContentHeight() {
        if (getText() == null) return 0;
        int contentHeight = getLayout().getHeight();
        Log.d(TAG, "getFullContentHeight: contentHeight = " + contentHeight);
        return contentHeight;
    }

    public boolean isScrolledToBottom() {
        return getScrollY() + getHeight() >= getLayout().getHeight();
    }
}
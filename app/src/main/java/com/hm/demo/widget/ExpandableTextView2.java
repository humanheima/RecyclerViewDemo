package com.hm.demo.widget;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by p_dmweidu on 2025/9/25
 * Desc:支持展开和收起功能的TextView（收起时最大4行），使用内部拦截法处理触摸事件
 */
public class ExpandableTextView2 extends AppCompatTextView {

    private static final String TAG = "ExpandableTextView";
    // 收起状态最大行数
    private static final int COLLAPSED_MAX_LINES = 4;
    // 是否处于展开状态
    private boolean isExpanded = false;
    // 原始最大行数（用于恢复）
    private int originalMaxLines = Integer.MAX_VALUE;

    // 展开/收起状态变化监听器
    private OnExpandStateChangeListener expandStateChangeListener;

    public ExpandableTextView2(Context context) {
        super(context);
        init();
    }

    public ExpandableTextView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExpandableTextView2(Context context, AttributeSet attrs, int defStyleAttr) {
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

    // 以下为原有触摸事件处理逻辑保持不变
    private int lastX;
    private int lastY;
    private boolean consumed = false;

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            getParent().requestDisallowInterceptTouchEvent(true);
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = (int) ev.getY();
                lastX = (int) ev.getX();
                // 请求父控件（RecyclerView）不拦截触摸事件
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                // 如果 TextView 未滑动到底部或顶部，优先处理滑动

                int y = (int) ev.getY();
                int x = (int) ev.getX();
                int deltaY = y - lastY;
                int deltaX = x - lastX;
                //Log.d(TAG, "dispatchTouchEvent: deltaY = " + deltaY + " , deltaX = " + deltaX);
                if (isAtBottom() && deltaY <= 0) {

//                  //滑动到下边界，继续向上滑动，允许RecyclerView 处理
                    getParent().requestDisallowInterceptTouchEvent(false);
                    //Log.d(TAG, "dispatchTouchEvent: 滑动到下边界，继续向上滑动，允许RecyclerView 处理");
                    //consumed = false;
                } else if (isAtTop() && deltaY >= 0) {
//                  // 滑动到上边界，继续向上滑，允许RecyclerView 处理
                    getParent().requestDisallowInterceptTouchEvent(false);
                    //Log.d(TAG, "dispatchTouchEvent: 滑动到上边界，继续向上滑，允许RecyclerView 处理");
                } else {
                    //Log.d(TAG, "dispatchTouchEvent: TextView 未滑动到底部或顶部，不让RecyclerView拦截");
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
        //Log.d(TAG, "dispatchTouchEvent: consumed = " + consumed + " , getEventName(ev) = " + getEventName(ev));
        return super.dispatchTouchEvent(ev);
    }

    private int touchY = 0;
    private int touchX = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int deltaX;
        int deltaY = 0;

        boolean used = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = (int) event.getX();
                touchY = (int) event.getY();
                used = true;
                break;
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getX();
                int y = (int) event.getY();
                deltaX = x - touchX;
                deltaY = y - touchY;
                Log.d(TAG, "onTouchEvent: deltaX = " + deltaX + " , deltaY = " + deltaY);

                if (deltaY > 0 && !isAtTop()) {
                    int newScrollY = getScrollY() - deltaY;
                    //小于0以后，不能再往上滚动了。
                    if (newScrollY < 0) {
                        newScrollY = 0;
                    }
                    scrollTo(0, newScrollY);
                    used = true;
                } else if (deltaY < 0 && !isAtBottom()) {
                    int newScrollY = getScrollY() + (-deltaY);
                    // 限制滚动距离不超过最大值
                    newScrollY = Math.min(newScrollY, getMaxScrollDistance());
                    scrollTo(0, newScrollY);
                    //return true;
                    used = true;
                } else {
                    used = false;
                }
                break;
        }

        touchX = (int) event.getX();
        touchY = (int) event.getY();

        return used;
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

    /**
     * 判断是否滑动到顶部
     *
     * @return true if scrolled to top, false otherwise
     */
    private boolean isAtTop() {
        return getScrollY() == 0;
    }

    /**
     * 获取当前显示的内容高度（受 maxLines 限制）
     */
    public int getDisplayedContentHeight() {
        int height = getHeight();
        Log.d(TAG, "getDisplayedContentHeight: height = " + height);
        return height;
    }

    /**
     * 获取竖直方向最大滚动距离（受 maxLines 限制）
     *
     * @return 最大滚动距离，若内容未超过显示区域则返回0
     */
    public int getMaxScrollDistance() {
        Layout layout = getLayout();
        if (layout == null) {
            return 0; // 布局未完成时返回0
        }
        // 内容总高度（受 maxLines 限制）
        int contentHeight = layout.getHeight();
        // 显示区域高度（包含内边距）
        int viewHeight = getHeight();
        // 最大滚动距离 = 内容总高度 - 显示区域高度（最小为0）
        int max = Math.max(0, contentHeight - viewHeight);
        Log.d(TAG, "getMaxScrollDistance: contentHeight = " + contentHeight + " , viewHeight = " + viewHeight + " , max = " + max);
        return max;
    }

    /**
     * 获取完整内容高度（不受 maxLines 限制）
     */
    public int getFullContentHeight() {
        if (getText() == null) return 0;
        int contentHeight = getLayout().getHeight();
        Log.d(TAG, "getFullContentHeight: contentHeight = " + contentHeight);
        return contentHeight;
    }

    /**
     * 判断是否滑动到底部
     *
     * @return true if scrolled to bottom, false otherwise
     */
    public boolean isScrolledToBottom() {
        // 计算内容总高度和当前滚动位置
        return getScrollY() + getHeight() >= getLayout().getHeight();
    }

}
package com.hm.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.widget.NestedScrollView;

/**
 * Created by p_dmweidu on 2025/9/25
 * Desc:
 */
public class CustomNestedScrollView extends NestedScrollView {

    private static final String TAG = "CustomNestedScrollView";

    public CustomNestedScrollView(Context context) {
        super(context);
    }

    public CustomNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 请求父控件（RecyclerView）不拦截触摸事件
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                // 如果 NestedScrollView 未滑动到底部或顶部，优先处理滑动
                if (!isAtBottom() && !isAtTop()) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    // 滑动到边界，允许 RecyclerView 处理
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        boolean b = super.onTouchEvent(ev);

        Log.d(TAG, "onTouchEvent: b  = " + b);
        return b;
    }

    private boolean isAtBottom() {
        View scrollViewChild = getChildAt(0);
        return (getHeight() + getScrollY()) >= (scrollViewChild.getHeight() - getPaddingBottom());
    }

    private boolean isAtTop() {
        return getScrollY() == 0;
    }
}
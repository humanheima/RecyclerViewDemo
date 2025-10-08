package com.hm.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by p_dmweidu on 2025/9/25
 * Desc:内部拦截法，还得好好看一看
 */
public class CustomTextView extends AppCompatTextView {

    private static final String TAG = "CustomTextView";

    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int lastX;
    private int lastY;
    private boolean consumed = false;

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

//                  //滑动到下边界，继续向上滑动，允许 RecyclerView 处理
                    getParent().requestDisallowInterceptTouchEvent(false);
                    //Log.d(TAG, "dispatchTouchEvent: 滑动到下边界，继续向上滑动，允许 RecyclerView 处理");
                    //consumed = false;
                } else if (isAtTop() && deltaY >= 0) {
//                  // 滑动到上边界，继续向上滑，允许 RecyclerView 处理
                    getParent().requestDisallowInterceptTouchEvent(false);
                    //Log.d(TAG, "dispatchTouchEvent: 滑动到上边界，继续向上滑，允许 RecyclerView 处理");
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

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = (int) event.getX();
                touchY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getX();
                int y = (int) event.getY();
                deltaX = x - touchX;
                deltaY = y - touchY;
                Log.d(TAG, "onTouchEvent: deltaX = " + deltaX + " , deltaY = " + deltaY);
                break;
        }

        touchX = (int) event.getX();
        touchY = (int) event.getY();

        if (deltaY > 0 && !isAtTop()) {
            int newScrollY = getScrollY() - deltaY;
            //小于0以后，不能再往上滚动了。
            if (newScrollY < 0) {
                newScrollY = 0;
            }
            scrollTo(0, newScrollY);
            return true;
        } else if (deltaY < 0 && !isAtBottom()) {
            scrollBy(0, -deltaY);
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        // TODO: 2025/10/8 理论上，走到这里，应该都返回true吧。
        boolean b = super.onTouchEvent(event);

        Log.d(TAG, "onTouchEvent: b= " + b + " , getEventName(event) = " + getEventName(event));
        return b;
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
     * 判断是否滑动到底部
     *
     * @return true if scrolled to bottom, false otherwise
     */
    public boolean isScrolledToBottom() {
        // 计算内容总高度和当前滚动位置
        return getScrollY() + getHeight() >= getLayout().getHeight();
    }

}
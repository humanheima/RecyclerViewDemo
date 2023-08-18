package com.hm.demo.layoutmanager;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;

public class TempLayoutManager extends LinearLayoutManager {

    private static final String TAG = "TempLayoutManager";

    public TempLayoutManager(Context context) {
        super(context);
    }


    public TempLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public TempLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void measureChildWithMargins(@NonNull View child, int widthUsed, int heightUsed) {
        //super.measureChildWithMargins(child, widthUsed, heightUsed);
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();

        Log.i(TAG, "measureChildWithMargins: child.getVisibility()  = " + child.getVisibility() + "，" + child.getTag());

        if (child.getVisibility() != View.GONE) {

            //Note: 处理itemDecoration，这里不需要
            //final Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
            //widthUsed += insets.left + insets.right;
            //heightUsed += insets.top + insets.bottom;

            final int widthSpec = getChildMeasureSpec(getWidth(), getWidthMode(),
                    getPaddingLeft() + getPaddingRight()
                            + lp.leftMargin + lp.rightMargin + widthUsed, lp.width,
                    canScrollHorizontally());
            final int heightSpec = getChildMeasureSpec(getHeight(), getHeightMode(),
                    getPaddingTop() + getPaddingBottom()
                            + lp.topMargin + lp.bottomMargin + heightUsed, lp.height,
                    canScrollVertically());
            //测量Child
            //if (shouldMeasureChild(child, widthSpec, heightSpec, lp)) {
            child.measure(widthSpec, heightSpec);
            Log.i(TAG, "measureChildWithMargins " + child + "，child.getMeasuredHeight() = "
                    + child.getMeasuredHeight());
            //}
        } else {
            //child.measure(0, 0);
            Log.i(TAG, "measureChildWithMargins: 为gone" + child + "，child.getMeasuredHeight() = "
                    + child.getMeasuredHeight());


        }

    }
}

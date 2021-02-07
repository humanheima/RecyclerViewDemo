package com.hm.demo.widget

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent

/**
 * Created by dumingwei on 2021/1/29.
 *
 * Desc:
 */
class MyRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.recyclerview.widget.RecyclerView(context, attrs, defStyleAttr) {


    private val TAG: String = "MyRecyclerView"

    init {

    }

    private var mLastTouchX = 0f
    private var mLastTouchY = 0f


    override fun onTouchEvent(e: MotionEvent): Boolean {

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastTouchX = e.x + 0.5f
                mLastTouchY = e.y + 0.5f

                Log.i(TAG, "onTouchEvent:ACTION_DOWN mLastTouchX = $mLastTouchX , mLastTouchY = $mLastTouchY")
            }

            MotionEvent.ACTION_MOVE -> {
                val x = e.x
                val y = e.y
                //Log.i(TAG, "onTouchEvent: ACTION_MOVE e.x = $x , e.y = $y")

                val deltaX = mLastTouchX - x
                //手指从下向上滑动，deltaY大于0
                val deltaY = mLastTouchY - y
                Log.i(TAG, "onTouchEvent: ACTION_MOVE deltaY = $deltaY")

            }

        }
        return super.onTouchEvent(e)
    }


}
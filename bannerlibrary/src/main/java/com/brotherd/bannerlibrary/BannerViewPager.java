package com.brotherd.bannerlibrary;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.lang.reflect.Field;

class BannerViewPager extends ViewPager {
    private boolean scrollable = true;

    public BannerViewPager(Context context) {
        super(context);
    }

    public BannerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return this.scrollable && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.scrollable && super.onInterceptTouchEvent(ev);
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    /**
     * 设置调用setCurrentItem(int item, boolean smoothScroll)方法时，page切换的时间长度
     *
     * @param duration page切换的时间长度
     */
    public void setPageChangeDuration(int duration) {
        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            scrollerField.set(this, new BGABannerScroller(getContext(), duration));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

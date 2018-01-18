package com.brotherd.bannerlibrary.transformer;

import android.view.View;

import static android.support.v4.view.ViewCompat.setPivotX;
import static android.support.v4.view.ViewCompat.setPivotY;
import static android.support.v4.view.ViewCompat.setRotation;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:15/6/19 上午8:41
 * 描述:
 */
public class RotatePageTransformer extends BGAPageTransformer {
    private float mMaxRotation = 15.0f;

    public RotatePageTransformer() {
    }

    public RotatePageTransformer(float maxRotation) {
        setMaxRotation(maxRotation);
    }

    @Override
    public void handleInvisiblePage(View view, float position) {
        setPivotX(view, view.getMeasuredWidth() * 0.5f);
        setPivotY(view, view.getMeasuredHeight());
        setRotation(view, 0);
    }

    @Override
    public void handleLeftPage(View view, float position) {
        float rotation = (mMaxRotation * position);
        setPivotX(view, view.getMeasuredWidth() * 0.5f);
        setPivotY(view, view.getMeasuredHeight());
        setRotation(view, rotation);
    }

    @Override
    public void handleRightPage(View view, float position) {
        handleLeftPage(view, position);
    }

    public void setMaxRotation(float maxRotation) {
        if (maxRotation >= 0.0f && maxRotation <= 40.0f) {
            mMaxRotation = maxRotation;
        }
    }
}
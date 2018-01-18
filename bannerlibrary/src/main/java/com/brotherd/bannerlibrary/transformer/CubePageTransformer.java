package com.brotherd.bannerlibrary.transformer;

import android.view.View;

import static android.support.v4.view.ViewCompat.setPivotX;
import static android.support.v4.view.ViewCompat.setPivotY;
import static android.support.v4.view.ViewCompat.setRotationY;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:15/6/19 17:39
 * 描述:
 */
public class CubePageTransformer extends BGAPageTransformer {
    private float mMaxRotation = 90.0f;

    public CubePageTransformer() {
    }

    public CubePageTransformer(float maxRotation) {
        setMaxRotation(maxRotation);
    }

    @Override
    public void handleInvisiblePage(View view, float position) {
        setPivotX(view, view.getMeasuredWidth());
        setPivotY(view, view.getMeasuredHeight() * 0.5f);
        setRotationY(view, 0);
    }

    @Override
    public void handleLeftPage(View view, float position) {
        setPivotX(view, view.getMeasuredWidth());
        setPivotY(view, view.getMeasuredHeight() * 0.5f);
        setRotationY(view, mMaxRotation * position);
    }

    @Override
    public void handleRightPage(View view, float position) {
        setPivotX(view, 0);
        setPivotY(view, view.getMeasuredHeight() * 0.5f);
        setRotationY(view, mMaxRotation * position);
    }

    public void setMaxRotation(float maxRotation) {
        if (maxRotation >= 0.0f && maxRotation <= 90.0f) {
            mMaxRotation = maxRotation;
        }
    }

}

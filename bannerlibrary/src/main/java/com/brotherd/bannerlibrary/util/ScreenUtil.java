package com.brotherd.bannerlibrary.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by dumingwei on 2016/11/24.
 */
public class ScreenUtil {

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
                context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue,
                context.getResources().getDisplayMetrics());
    }
}

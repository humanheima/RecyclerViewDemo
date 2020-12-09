package com.hm.demo.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by dumingwei on 2017/4/20.
 */
public class ScreenUtil {


    private static final String TAG = "ScreenUtil";

    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static void testDensity(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        float density = displayMetrics.density;
        Log.i(TAG, "density: " + density);
        Log.i(TAG, "width: " + widthPixels);
        Log.i(TAG, "height: " + heightPixels);
        Log.i(TAG, "xdpi: " + widthPixels / density);
        Log.i(TAG, "ydpi: " + heightPixels / density);
        Log.i(TAG, "densityDpi: " + displayMetrics.densityDpi);
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    /**
     * 获取真实屏幕高度，不包含虚拟导航栏
     *
     * @param context
     * @return
     */
    public static int getRealScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display defaultDisplay = windowManager.getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= 19) {
            defaultDisplay.getRealMetrics(displayMetrics);
        } else {
            defaultDisplay.getMetrics(displayMetrics);
        }
        return displayMetrics.heightPixels;
    }

    public static void getDisplayMetricsInfo(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        Log.e(TAG, "getDisplayMetricsInfo: " + displayMetrics.toString() + "densityDpi=" + displayMetrics.densityDpi);
    }

    public static int getStateBarHeight(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        Log.e(TAG, "getStateBarHeight: " + rect.top + "rect.bottom=" + rect.bottom);
        return rect.top;
    }

    public static int getActionBarHeight(AppCompatActivity activity) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            Log.e(TAG, "getActionBarHeight: " + actionBar.getHeight());
            return actionBar.getHeight();
        }
        return 0;
    }

    /**
     * 获取我们的View显示的区域距屏幕顶部的高度
     *
     * @param activity
     * @return
     */
    public static int getContentViewTop(AppCompatActivity activity) {
        int top = getStateBarHeight(activity) + getActionBarHeight(activity);
        Log.e(TAG, "getViewTop: " + top);
        return top;
    }

    public static int dpToPx(Context context, int dpValue) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return (int) (displayMetrics.density * dpValue);
    }

    public static int spToPx(Context context, int dpValue) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return (int) (displayMetrics.scaledDensity * dpValue);
    }
}

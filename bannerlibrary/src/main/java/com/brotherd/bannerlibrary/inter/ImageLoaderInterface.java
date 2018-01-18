package com.brotherd.bannerlibrary.inter;

import android.content.Context;
import android.view.View;

/**
 * Created by dumingwei on 2017/7/13.
 */
public interface ImageLoaderInterface<T extends View> {

    void displayImage(Context context, Object path, T imageView);

    T createImageView(Context context);
}

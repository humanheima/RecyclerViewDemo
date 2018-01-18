package com.brotherd.bannerlibrary.impl;

import android.content.Context;
import android.widget.ImageView;

import com.brotherd.bannerlibrary.inter.ImageLoaderInterface;

/**
 * Created by dumingwei on 2017/7/13.
 */
public abstract class ImageLoader implements ImageLoaderInterface<ImageView> {

    @Override
    public ImageView createImageView(Context context) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }
}

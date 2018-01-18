package com.example.loadmoredemo.Util;

import android.content.Context;
import android.widget.ImageView;

import com.brotherd.bannerlibrary.impl.ImageLoader;
import com.bumptech.glide.Glide;
import com.example.loadmoredemo.R;

/**
 * Created by dumingwei on 2017/7/13.
 */
public class GlideImageLoader extends ImageLoader {

    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        Glide.with(context.getApplicationContext())
                .load(path)
                .placeholder(R.drawable.img_bg)
                .error(R.drawable.img_bg)
                .into(imageView);
    }
}

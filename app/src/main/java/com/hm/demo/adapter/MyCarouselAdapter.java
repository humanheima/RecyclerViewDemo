package com.hm.demo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hm.demo.R;

import org.cchao.carousel.listener.CarouselAdapter;

import java.util.List;

/**
 * Created by dumingwei on 2018/1/17 0017.
 */

public class MyCarouselAdapter implements CarouselAdapter {

    private List<String> data;

    public MyCarouselAdapter(List<String> data) {
        this.data = data;
    }

    @Override
    public View getView(ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel, parent, false);
        ImageView imgCarousel = (ImageView) view.findViewById(R.id.img_carousel);
        Glide.with(parent.getContext())
                .load(data.get(position))
                .into(imgCarousel);
        return view;
    }

    @Override
    public int getCount() {
        return data.size();
    }

}

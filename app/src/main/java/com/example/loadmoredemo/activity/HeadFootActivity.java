package com.example.loadmoredemo.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.brotherd.bannerlibrary.SimpleBanner;
import com.brotherd.bannerlibrary.inter.OnBannerClickListener;
import com.example.loadmoredemo.R;
import com.example.loadmoredemo.Util.GlideImageLoader;
import com.example.loadmoredemo.Util.Images;
import com.example.loadmoredemo.adapter.MyCarouselAdapter;
import com.example.loadmoredemo.base.BaseActivity;
import com.example.loadmoredemo.base.BaseAdapter;
import com.example.loadmoredemo.base.BaseViewHolder;
import com.example.loadmoredemo.databinding.ActivityHeadFootBinding;
import com.example.loadmoredemo.interfaces.OnItemClickListener;
import com.example.loadmoredemo.model.TestBean;

import org.cchao.carousel.CarouselView;

import java.util.ArrayList;
import java.util.List;

public class HeadFootActivity extends BaseActivity {

    private ActivityHeadFootBinding binding;
    private View headView;
    private View headView1;
    private BaseAdapter<TestBean> adapter;
    private List<String> multiTitles;
    private List<String> multiImgs;
    private SimpleBanner banner;
    private CarouselView carouselView;

    public static void launch(Context context) {
        Intent intent = new Intent(context, HeadFootActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_head_foot);
        initData();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BaseAdapter<TestBean>(this, mDatas) {

            @Override
            public int getHolderType(int position, TestBean testBean) {
                return R.layout.item_diff;
            }

            @Override
            public void bindViewHolder(BaseViewHolder holder, TestBean testBean, int position) {
                if (holder.getItemViewType() == R.layout.item_diff) {
                    holder.setTextViewText(R.id.tv1, testBean.getName());
                    holder.setTextViewText(R.id.tv2, testBean.getDesc());
                    holder.setImageViewResource(R.id.iv, testBean.getPicture());
                    holder.setOnItemClickListener(R.id.iv, new OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Toast.makeText(HeadFootActivity.this, "onItemClick position=" + position, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
        headView = getLayoutInflater().inflate(R.layout.head_layout, null);
        initBanner(headView);
        adapter.addHeadView(headView);
    /*    headView1 = getLayoutInflater().inflate(R.layout.head_layout_1, null);
        initCarouselBanner(headView1);
        adapter.addHeadView(headView1);*/
        binding.recyclerView.setAdapter(adapter);
    }

    private void initCarouselBanner(View headView1) {
        carouselView= ((CarouselView) headView1.findViewById(R.id.carouselView));
        List<String> data = new ArrayList<>();
        data.add(Images.imageThumbUrls[0]);
        data.add(Images.imageThumbUrls[1]);
        data.add(Images.imageThumbUrls[2]);
        carouselView.with(this)
                .setAdapter(new MyCarouselAdapter(data))
                .setDelayTime(6000)
                .setShowIndicator(true)
                .setAutoSwitch(true)
                .setCanLoop(true)
                .start();
    }

    private void initBanner(View view) {
        banner = (SimpleBanner) view.findViewById(R.id.simple_banner);
        multiTitles = new ArrayList<>();
        multiImgs = new ArrayList<>();
        multiTitles.add("当春乃发生");
        multiTitles.add("随风潜入夜");
        multiTitles.add("润物细无声");
        multiImgs.add(Images.imageThumbUrls[0]);
        multiImgs.add(Images.imageThumbUrls[1]);
        multiImgs.add(Images.imageThumbUrls[2]);
        banner.setOnBannerClickListener(new OnBannerClickListener() {
            @Override
            public void OnBannerClick(int position) {
                Toast.makeText(HeadFootActivity.this, "position=" + position, Toast.LENGTH_SHORT).show();
            }
        });
        banner.setImages(multiImgs)
                .setImageLoader(new GlideImageLoader())
                .setTitles(multiTitles)
                .setAbortAnimation(false)
                .isAutoPlay(true)
                .start();
    }

}

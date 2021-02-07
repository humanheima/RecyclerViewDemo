package com.hm.demo.activity;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.hm.banner.HmBanner;
import com.hm.banner.inter.OnBannerClickListener;
import com.hm.demo.R;
import com.hm.demo.Util.GlideImageLoader;
import com.hm.demo.Util.Images;
import com.hm.demo.base.BaseActivity;
import com.hm.demo.base.BaseAdapter;
import com.hm.demo.base.BaseViewHolder;
import com.hm.demo.databinding.ActivityHeadFootBinding;
import com.hm.demo.interfaces.OnItemClickListener;
import com.hm.demo.model.TestBean;

import java.util.ArrayList;
import java.util.List;

public class HeadFootActivity extends BaseActivity<ActivityHeadFootBinding> {

    private View headView;
    private BaseAdapter<TestBean> adapter;
    private List<String> multiTitles;
    private List<String> multiImgs;
    private HmBanner banner;

    public static void launch(Context context) {
        Intent intent = new Intent(context, HeadFootActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_head_foot;
    }

    @Override
    protected void initData() {
        adapter = new BaseAdapter<TestBean>(this, getTestData()) {

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
        viewBind.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        viewBind.recyclerView.setAdapter(adapter);
    }

    private void initBanner(View view) {
        banner = view.findViewById(R.id.simple_banner);
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

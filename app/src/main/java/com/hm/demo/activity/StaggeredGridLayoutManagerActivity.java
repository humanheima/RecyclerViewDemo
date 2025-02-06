package com.hm.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hm.demo.R;
import com.hm.demo.base.BaseActivity;
import com.hm.demo.base.BaseAdapter;
import com.hm.demo.base.BaseViewHolder;
import com.hm.demo.databinding.ActivityTestStaggeredLayoutManagerBinding;
import com.hm.demo.interfaces.OnItemClickListener;
import com.hm.demo.model.TestBean;
import com.hm.demo.widget.AutoScrollRecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dumingwei on 2021/6/10
 * <p>
 * Desc:
 */
public class StaggeredGridLayoutManagerActivity extends BaseActivity<ActivityTestStaggeredLayoutManagerBinding> {

    private static final String TAG = "StaggeredGridLayoutMana";

    private AutoScrollRecyclerView recyclerView;
    //private StaggeredGridLayoutManager linearLayoutManager;

    public static void launch(Context context) {
        Intent starter = new Intent(context, StaggeredGridLayoutManagerActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_test_staggered_layout_manager;
    }

    @Override
    protected void initData() {
        recyclerView = viewBind.recyclerView;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, "onScrollStateChanged: newState = " + newState);
                //对于竖直方向上来说，负数，检查手指从上往下滑动
                Log.d(TAG, "recyclerView.canScrollVertically(-1) = " + recyclerView.canScrollVertically(-1));

                //对于竖直方向上来说，正数，检查手指从下向上滑动
                Log.d(TAG, "recyclerView.canScrollVertically(1) = " + recyclerView.canScrollVertically(1));
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new InfiniteAdapter(this, getTestData()));

        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.start();
            }
        }, 500);
    }

    @Override
    protected List<TestBean> getTestData() {
        List<TestBean> mDatas = new ArrayList<>();
        mDatas.add(new TestBean("dumingwei1", "Android", R.drawable.pic));
        mDatas.add(new TestBean("dumingwei1", "Java", R.drawable.pic_2));
        mDatas.add(new TestBean("dumingwei1", "艰难", R.drawable.pic_3));
        mDatas.add(new TestBean("dumingwei1", "产品", R.drawable.pic_4));
        mDatas.add(new TestBean("dumingwei1", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei1", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei1", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei1", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei1", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei1", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei1", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei1", "最后一个", R.drawable.pic_5));
        return mDatas;
    }

    static class InfiniteAdapter extends BaseAdapter<TestBean> {

        private int[] colorArray = new int[4];


        public InfiniteAdapter(Context context, List<TestBean> data) {
            super(context, data);
            colorArray[0] = context.getResources().getColor(R.color.color_FFF4F4, null);
            colorArray[1] = context.getResources().getColor(R.color.color_FFF9E7, null);
            colorArray[2] = context.getResources().getColor(R.color.color_EEFCF4, null);
            colorArray[3] = context.getResources().getColor(R.color.color_EDF8FC, null);
        }

        @Override
        public int getItemCount() {
            return Integer.MAX_VALUE;
            //return dataList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (headView != null) {
                position = position - 1;
            }
            int realSize = dataList.size();
            int index = position % realSize;
            return getHolderType(position, dataList.get(index));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (headView != null) {
                position = position - 1;
            }
            int realSize = dataList.size();
            int index = position % realSize;

            if (index < realSize && !(holder instanceof SpecialViewHolder)) {
                bindViewHolder(((BaseViewHolder) holder), dataList.get(index), index);
            }
        }

        @Override
        public void bindViewHolder(BaseViewHolder holder, TestBean testBean, int position) {
            holder.setTextViewText(R.id.tv_name, testBean.getName());
            int colorIndex = position % (colorArray.length);
            holder.setViewBg(R.id.tv_name, colorArray[colorIndex]);
            holder.setOnItemClickListener(R.id.tv_name, new OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    int realSize = dataList.size();
                    int index = position % realSize;
                    String name = dataList.get(index).getDesc();
                    Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getHolderType(int position, TestBean testBean) {
            return R.layout.item_diff_staggered_layout;
        }
    }

}

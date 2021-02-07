package com.hm.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.hm.demo.R;
import com.hm.demo.base.BaseActivity;
import com.hm.demo.base.BaseAdapter;
import com.hm.demo.base.BaseViewHolder;
import com.hm.demo.databinding.ActivitySlideDeleteBinding;
import com.hm.demo.interfaces.OnItemClickListener;
import com.hm.demo.model.TestBean;

import java.util.List;

public class SlideDeleteActivity extends BaseActivity<ActivitySlideDeleteBinding> {

    RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private List<TestBean> testData;

    public static void launch(Context context) {
        Intent starter = new Intent(context, SlideDeleteActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_slide_delete;
    }

    @Override
    protected void initData() {
        recyclerView = viewBind.recyclerView;
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        testData = getTestData();
        recyclerView.setAdapter(new BaseAdapter<TestBean>(this, testData) {

            @Override
            public int getHolderType(int position, TestBean testBean) {
                return R.layout.item_slide_delete;
            }

            @Override
            public void bindViewHolder(BaseViewHolder holder, final TestBean testBean, int position) {
                holder.setTextViewText(R.id.tv1, testBean.getDesc());
                holder.setTextViewText(R.id.tv2, testBean.getName());
                holder.setImageViewResource(R.id.iv, testBean.getPicture());
                holder.setOnItemClickListener(R.id.text_delete, new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        testData.remove(position);
                        notifyItemRemoved(position);
                    }
                });
                holder.setOnItemClickListener(R.id.tv1, new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Toast.makeText(SlideDeleteActivity.this, "onItemClick position=" + position, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}

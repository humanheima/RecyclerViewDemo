package com.example.loadmoredemo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.loadmoredemo.R;
import com.example.loadmoredemo.base.BaseActivity;
import com.example.loadmoredemo.base.BaseAdapter;
import com.example.loadmoredemo.base.BaseViewHolder;
import com.example.loadmoredemo.interfaces.OnItemClickListener;
import com.example.loadmoredemo.model.TestBean;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SlideDeleteActivity extends BaseActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    public static void launch(Context context) {
        Intent starter = new Intent(context, SlideDeleteActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_delete);
        ButterKnife.bind(this);
        initData();
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new BaseAdapter<TestBean>(this, mDatas) {

            @Override
            public int getHolderType(int position, TestBean testBean) {
                return R.layout.item_slide_delete;
            }

            @Override
            public void bindViewHolder(BaseViewHolder holder, TestBean testBean,int position) {

                holder.setTextViewText(R.id.tv1, testBean.getDesc());
                holder.setTextViewText(R.id.tv2, testBean.getName());
                holder.setImageViewResource(R.id.iv, testBean.getPicture());
                holder.setOnItemClickListener(R.id.text_delete, new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        mDatas.remove(position);
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

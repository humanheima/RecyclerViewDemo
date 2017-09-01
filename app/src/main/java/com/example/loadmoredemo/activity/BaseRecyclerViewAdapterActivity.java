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

public class BaseRecyclerViewAdapterActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();
    private RecyclerView recyclerView;

    private LinearLayoutManager linearLayoutManager;

    public static void launch(Context context) {
        Intent starter = new Intent(context, BaseRecyclerViewAdapterActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_recyclerview_adapter);
        initData();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new BaseAdapter<TestBean>(this, mDatas) {

            @Override
            public int getHolderType(int position, TestBean testBean) {
                if (testBean.getName().endsWith("1") || testBean.getName().endsWith("0")) {
                    return R.layout.item_diff;
                } else {
                    return R.layout.item_diff_copy;
                }
            }

            @Override
            public void bindViewHolder(BaseViewHolder holder, TestBean testBean,int position) {
                if (holder.getItemViewType() == R.layout.item_diff) {
                    holder.setTextViewText(R.id.tv1, testBean.getName());
                    holder.setTextViewText(R.id.tv2, testBean.getDesc());
                    holder.setImageViewResource(R.id.iv, testBean.getPicture());
                    holder.setOnItemClickListener(R.id.iv, new OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Toast.makeText(BaseRecyclerViewAdapterActivity.this, "onItemClick position=" + position, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (holder.getItemViewType() == R.layout.item_diff_copy) {
                    holder.setTextViewText(R.id.tv1, testBean.getDesc());
                    holder.setTextViewText(R.id.tv2, testBean.getName());
                    holder.setImageViewResource(R.id.iv, testBean.getPicture());
                    holder.setOnItemClickListener(R.id.tv1, new OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Toast.makeText(BaseRecyclerViewAdapterActivity.this, "onItemClick position=" + position, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


}

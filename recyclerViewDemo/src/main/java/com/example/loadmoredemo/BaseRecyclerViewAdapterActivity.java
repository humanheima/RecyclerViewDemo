package com.example.loadmoredemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.loadmoredemo.base.BaseAdapter;
import com.example.loadmoredemo.base.BaseViewHolder;
import com.example.loadmoredemo.listener.OnItemClickListener;
import com.example.loadmoredemo.model.TestBean;

import java.util.ArrayList;
import java.util.List;

public class BaseRecyclerViewAdapterActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private RecyclerView recyclerView;
    private List<TestBean> mDatas;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_recyclerview_adapter);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        initData();
        recyclerView.setAdapter(new BaseAdapter<TestBean>(this, R.layout.item_diff, mDatas) {
            @Override
            public void bindViewHolder(BaseViewHolder holder, TestBean testBean) {
                holder.setTextViewText(R.id.tv1, testBean.getName());
                holder.setTextViewText(R.id.tv2, testBean.getDesc());
                holder.setImageViewResource(R.id.iv, testBean.getPicture());
                holder.setOnItemClickListener(R.id.iv, new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Toast.makeText(BaseRecyclerViewAdapterActivity.this, "onItemClick position=" + position, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initData() {
        mDatas = new ArrayList<>();
        mDatas.add(new TestBean("dumingwei1", "Android", R.drawable.pic));
        mDatas.add(new TestBean("dumingwei2", "Java", R.drawable.pic_2));
        mDatas.add(new TestBean("dumingwei3", "背锅", R.drawable.pic_3));
        mDatas.add(new TestBean("dumingwei4", "产品", R.drawable.pic_4));
        mDatas.add(new TestBean("dumingwei5", "测试", R.drawable.pic_5));
    }

}

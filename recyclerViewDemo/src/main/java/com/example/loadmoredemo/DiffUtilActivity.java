package com.example.loadmoredemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.loadmoredemo.Util.DiffCallBack;
import com.example.loadmoredemo.adapter.DiffAdapter;
import com.example.loadmoredemo.model.TestBean;

import java.util.ArrayList;
import java.util.List;

public class DiffUtilActivity extends AppCompatActivity {

    private List<TestBean> mDatas;
    private RecyclerView mRv;
    private DiffAdapter adapter;
    private List<TestBean> newDatas;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DiffUtil.DiffResult diffResult = (DiffUtil.DiffResult) msg.obj;
            diffResult.dispatchUpdatesTo(adapter);
            mDatas = newDatas;
            adapter.setmDatas(mDatas);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff_util);
        initData();
        mRv = (RecyclerView) findViewById(R.id.recycler_view);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DiffAdapter(mDatas, this);
        mRv.setAdapter(adapter);

    }

    public void refresh(View view) {
        newDatas = new ArrayList<>();
        for (TestBean bean : mDatas) {
            try {
                newDatas.add(bean.clone());//clone一遍旧数据 ，模拟刷新操作
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        newDatas.add(new TestBean("赵子龙", "帅", R.drawable.pic_6));//模拟新增数据
        newDatas.get(0).setDesc("Android+");
        newDatas.get(0).setPicture(R.drawable.pic_7);//模拟修改数据
        TestBean testBean = newDatas.get(1);//模拟数据位移
        newDatas.remove(testBean);
        newDatas.add(testBean);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(mDatas, newDatas), true);
                Message message = handler.obtainMessage();
                message.obj = diffResult;
                message.sendToTarget();
            }
        }).start();

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

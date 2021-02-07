package com.hm.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.hm.demo.R;
import com.hm.demo.Util.DiffCallBack;
import com.hm.demo.adapter.DiffAdapter;
import com.hm.demo.model.TestBean;

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

    public static void launch(Context context) {
        Intent starter = new Intent(context, DiffUtilActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff_util);
        initData();
        mRv = findViewById(R.id.recycler_view);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DiffAdapter(mDatas, this);
        mRv.setAdapter(adapter);
    }

    public void refresh(View view) {
        newDatas = new ArrayList<>();
        newDatas.add(new TestBean("ahahhh", "测试", R.drawable.pic_5));
        newDatas.add(new TestBean("dumingwei2", "Java", R.drawable.pic_2));
        newDatas.add(new TestBean("weisiboluke", "Android", R.drawable.pic));
        newDatas.add(new TestBean("dumingwei4", "产品", R.drawable.pic_4));
        newDatas.add(new TestBean("dumingwei3", "背锅", R.drawable.pic_3));
        /*for (TestBean bean : mDatas) {
            try {
                newDatas.add(bean.clone());//clone一遍旧数据 ，模拟刷新操作
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        //newDatas.add(new TestBean("赵子龙", "帅", R.drawable.pic_6));//模拟新增数据
        newDatas.get(0).setDesc("Android+");
        newDatas.get(0).setPicture(R.drawable.pic_7);//模拟修改数据*/
        //TestBean testBean = newDatas.get(1);//模拟数据位移
        //newDatas.remove(testBean);
        //newDatas.add(testBean);
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
        mDatas.add(new TestBean("dumingwei6", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei7", "飞狐外传", R.drawable.pic_6));
    }
}

package com.example.loadmoredemo.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.loadmoredemo.R;
import com.example.loadmoredemo.model.TestBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dumingwei on 2017/4/28.
 */
public class BaseActivity extends AppCompatActivity {

    protected List<TestBean> mDatas;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatas = new ArrayList<>();
    }

    protected void initData() {
        mDatas = new ArrayList<>();
        mDatas.add(new TestBean("dumingwei1", "Android", R.drawable.pic));
        mDatas.add(new TestBean("dumingwei2", "Java", R.drawable.pic_2));
        mDatas.add(new TestBean("dumingwei3", "beiguo", R.drawable.pic_3));
        mDatas.add(new TestBean("dumingwei4", "产品", R.drawable.pic_4));
        mDatas.add(new TestBean("dumingwei10", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei5", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei6", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei7", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei8", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei9", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei20", "测试", R.drawable.pic_5));
    }
}

package com.hm.demo.base;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hm.demo.R;
import com.hm.demo.model.TestBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dumingwei on 2017/4/28.
 */
public abstract class BaseActivity<V extends ViewDataBinding> extends AppCompatActivity {

    protected V viewBind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBind = DataBindingUtil.setContentView(this, bindLayout());
        initData();
    }

    protected abstract int bindLayout();

    protected abstract void initData();

    protected List<TestBean> getTestData() {
        List<TestBean> mDatas = new ArrayList<>();
        mDatas.add(new TestBean("dumingwei1", "Android", R.drawable.pic));
        mDatas.add(new TestBean("dumingwei2", "Java", R.drawable.pic_2));
        mDatas.add(new TestBean("dumingwei3", "Kotlin", R.drawable.pic_3));
        mDatas.add(new TestBean("dumingwei4", "产品", R.drawable.pic_4));
        mDatas.add(new TestBean("dumingwei10", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei5", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei6", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei7", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei8", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei20", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei20", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei20", "最后一个", R.drawable.pic_5));
        return mDatas;
    }
}

package com.hm.demo.test_diff;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hm.demo.R;
import com.hm.demo.model.TestBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by p_dmweidu on 2023/12/27
 * Desc: DiffUtil的使用
 */
public class MyDiffUtilActivity extends AppCompatActivity {

    private static final String TAG = "MyDiffUtilActivity";

    private List<TestBean> mDatas;
    private RecyclerView mRv;
    private MyDiffAdapter adapter;


    public static void launch(Context context) {
        Intent starter = new Intent(context, MyDiffUtilActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff_util);
        initData();
        mRv = findViewById(R.id.recycler_view);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyDiffAdapter(new MyDiffCallback());
        mRv.setAdapter(adapter);
        adapter.submitList(mDatas);
    }

    public void refresh(View view) {
        List<TestBean> list = new ArrayList<>();

        list.add(new TestBean("ahahhh", "测试", R.drawable.pic_5));
        list.add(0, new TestBean("张无忌", "乾坤大挪移", R.drawable.pic_2));
        list.add(new TestBean("曾阿牛", "九阳神功", R.drawable.pic));
        list.add(new TestBean("赵敏", "倚天剑", R.drawable.pic_4));
        list.add(new TestBean("小昭", "圣火令", R.drawable.pic_4));
        list.add(new TestBean("阿里", "圣火令", R.drawable.pic));
        list.add(new TestBean("张三丰", "太极剑法", R.drawable.pic_2));
        list.add(new TestBean("张翠山", "屠龙刀", R.drawable.pic_2));
        list.add(new TestBean("张松溪", "太极剑法", R.drawable.pic_2));
        list.add(new TestBean("莫声谷", "太极拳", R.drawable.pic_2));
        list.add(new TestBean("殷梨亭", "太极剑法", R.drawable.pic_2));
        list.add(new TestBean("俞岱岩", "太极剑法", R.drawable.pic_2));
        list.add(new TestBean("俞莲舟", "太极剑法", R.drawable.pic_2));
        list.add(new TestBean("宋远桥", "太极剑法", R.drawable.pic_2));

        list.add(new TestBean("周芷若", "屠龙刀", R.drawable.pic_3));
        list.addAll(mDatas);

        view.postDelayed(new Runnable() {
            @Override
            public void run() {

                adapter.submitList(list, new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "submitList 回调，run: ");
                    }
                });
            }
        }, 1000);

    }

    private void initData() {
        mDatas = new ArrayList<>();
        mDatas.add(new TestBean("dumingwei1", "Android", R.drawable.pic));
        mDatas.add(new TestBean("dumingwei2", "Java", R.drawable.pic_2));
        mDatas.add(new TestBean("dumingwei3", "奖励", R.drawable.pic_3));
        mDatas.add(new TestBean("dumingwei4", "产品", R.drawable.pic_4));
        mDatas.add(new TestBean("dumingwei5", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei6", "测试", R.drawable.pic_5));
        mDatas.add(new TestBean("dumingwei7", "飞狐外传", R.drawable.pic_6));
    }
}

package com.hm.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;

import com.hm.demo.R;
import com.hm.demo.adapter.TestEditTextAdapter;
import com.hm.demo.base.BaseActivity;
import com.hm.demo.databinding.ActivityTestEditTextBinding;
import com.hm.demo.model.Goods;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试EditText在RecyclerView中是否乱序
 */
public class TestEditTextActivity extends BaseActivity<ActivityTestEditTextBinding> {

    private final String TAG = getClass().getSimpleName();

    private List<Goods> goodsList;

    public static void launch(Context context) {
        Intent starter = new Intent(context, TestEditTextActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_test_edit_text;
    }

    @Override
    protected void initData() {
        goodsList = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Goods goods = new Goods();
            goods.setName("商品" + i);
            goods.setPrice(i);
            goodsList.add(goods);
        }
        viewBind.rvGoodsList.setLayoutManager(new LinearLayoutManager(this));
        TestEditTextAdapter adapter = new TestEditTextAdapter(this, goodsList);
        adapter.setOnPriceChangeListener(new TestEditTextAdapter.OnPriceChangeListener() {
            @Override
            public void change(int position, double price) {
                Goods goods = goodsList.get(position);
                goods.setPrice(price);
                goodsList.set(position, goods);
            }
        });
        viewBind.rvGoodsList.setAdapter(adapter);
    }
}

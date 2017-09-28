package com.example.loadmoredemo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.loadmoredemo.R;
import com.example.loadmoredemo.adapter.TestEditTextAdapter;
import com.example.loadmoredemo.model.Goods;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 测试EditText在RecyclerView中是否乱序
 */
public class TestEditTextActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private List<Goods> goodsList;
    @BindView(R.id.rv_goods_list)
    RecyclerView rvGoodsList;

    private LinearLayoutManager linearLayoutManager;

    private TestEditTextAdapter adapter;

    public static void launch(Context context) {
        Intent starter = new Intent(context, TestEditTextActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_edit_text);
        ButterKnife.bind(this);
        goodsList = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Goods goods = new Goods();
            goods.setName("商品" + i);
            goods.setPrice(i);
            goodsList.add(goods);
        }
        linearLayoutManager = new LinearLayoutManager(this);
        rvGoodsList.setLayoutManager(linearLayoutManager);
        adapter = new TestEditTextAdapter(this, goodsList);
        adapter.setOnPriceChangeListener(new TestEditTextAdapter.OnPriceChangeListener() {
            @Override
            public void change(int position, double price) {
                Goods goods = goodsList.get(position);
                goods.setPrice(price);
                goodsList.set(position, goods);
            }
        });
        rvGoodsList.setAdapter(adapter);
    }
}

package com.hm.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.hm.demo.R;
import com.hm.demo.adapter.MyLoadMoreAdapter;
import com.hm.demo.base.BaseViewHolder;
import com.hm.demo.interfaces.OnLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

public class HorizontalLoadMoreActivity extends AppCompatActivity {

    private MyLoadMoreAdapter<String> adapter;
    private List<String> data;
    private RecyclerView recyclerView;
    private int page = 1;

    public static void launch(Context context) {
        Intent starter = new Intent(context, HorizontalLoadMoreActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_load_more);
        data = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        getData();
    }

    /**
     * 给recyclerView设置适配器
     */
    public void setAdapter() {
        Log.e("tag", "size" + data.size());
        if (adapter == null) {
            adapter = new MyLoadMoreAdapter<String>(data, recyclerView, new OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    //每次上拉加载更多之前要把page++
                    page++;
                    //每次上拉加载更多之前设置setLoadAll(false)
                    if (adapter != null) {
                        adapter.setLoadAll(false);
                    }
                    getData();
                }
            }) {
                @Override
                public int getDataSize() {
                    return data == null ? 0 : data.size();
                }

                @Override
                protected void bindViewHolder(BaseViewHolder holder, String s, int position) {
                    holder.setTextViewText(R.id.tvName, s);
                }

                @Override
                protected void bindFootView(BaseViewHolder holder) {
                    if (adapter.isLoadAll()) {
                        holder.setVisible(R.id.footer_view_load_now, View.INVISIBLE);
                        holder.setVisible(R.id.footer_view_load_all, View.VISIBLE);
                    } else {
                        holder.setVisible(R.id.footer_view_load_now, View.VISIBLE);
                        holder.setVisible(R.id.footer_view_load_all, View.INVISIBLE);
                    }
                }

                @Override
                public int getHolderType(int position, String s) {
                    return R.layout.item_horizontal_load_morel;
                }
            };
            recyclerView.setAdapter(adapter);
        }
        //更新适配器
        adapter.reset();
        //如果正在下拉刷新的话，结束下拉刷新
       /* if (ptrFrameLayout.isRefreshing()) {
            ptrFrameLayout.refreshComplete();
        }*/
    }

    public void getData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (page == 1) {
                    data.clear();
                    for (int i = 0; i < 25; i++) {
                        data.add("string" + i);
                    }
                    setAdapter();
                } else {
                    if (page > 3) {
                        if (adapter != null) {
                            adapter.setLoadAll(true);
                        }
                    } else {
                        for (int i = 22; i < 33; i++) {
                            data.add("string" + i);
                        }
                        setAdapter();
                    }
                }
            }
        }, 1000);
    }
}

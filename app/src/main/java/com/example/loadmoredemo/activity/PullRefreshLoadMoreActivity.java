package com.example.loadmoredemo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.loadmoredemo.R;
import com.example.loadmoredemo.adapter.MyLoadMoreAdapter;
import com.example.loadmoredemo.base.BaseViewHolder;
import com.example.loadmoredemo.interfaces.OnLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

public class PullRefreshLoadMoreActivity extends AppCompatActivity {

    private MyLoadMoreAdapter<String> adapter;
    private List<String> data;
    private RecyclerView recyclerView;
    private int page = 1;
    private PtrClassicFrameLayout ptrFrameLayout;//下拉刷新控件

    public static void launch(Context context) {
        Intent starter = new Intent(context, PullRefreshLoadMoreActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_refresh_loadmore);
        data = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ptrFrameLayout = (PtrClassicFrameLayout) findViewById(R.id.myPtrFrameLayout);
        ptrFrameLayout.setPtrHandler(new PtrDefaultHandler() {
            //检查是否可以刷新
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            //下拉刷新的时候会调用这个方法，每次下拉刷新都要把page重置为1
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                page = 1;
                if (adapter != null) {
                    adapter.setLoadAll(false);
                }
                getData();
            }
        });
        /**
         * 延迟500毫秒后ptrFrameLayout自动刷新会调用checkCanDoRefresh(PtrFrameLayout frame, View content, View header)
         * 检查是否可以刷新，如果可以，就调用onRefreshBegin(PtrFrameLayout frame)
         */
        ptrFrameLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                ptrFrameLayout.autoRefresh();
            }
        }, 500);
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
                    holder.setTextViewText(R.id.textView, s);
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
                    return R.layout.item;
                }
            };
            recyclerView.setAdapter(adapter);
        }
        //更新适配器
        adapter.reset();
        //如果正在下拉刷新的话，结束下拉刷新
        if (ptrFrameLayout.isRefreshing()) {
            ptrFrameLayout.refreshComplete();
        }
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
        }, 2000);
    }
}

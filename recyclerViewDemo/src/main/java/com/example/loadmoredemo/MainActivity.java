package com.example.loadmoredemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.loadmoredemo.adapter.MyAdapter;
import com.example.loadmoredemo.listener.OnItemClickListener;
import com.example.loadmoredemo.listener.OnLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

public class MainActivity extends AppCompatActivity {

    MyAdapter adapter;
    List<String> dataList = new ArrayList<>();
    RecyclerView recyclerView;
    int page = 1;
    PtrClassicFrameLayout ptrFrameLayout;//下拉刷新控件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        Log.e("tag", "size" + dataList.size());
        if (adapter == null) {
            adapter = new MyAdapter(recyclerView, dataList, new OnLoadMoreListener() {
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
            });
            adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Toast.makeText(MainActivity.this, "position=" + position, Toast.LENGTH_SHORT).show();
                }
            });
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
                    dataList.clear();
                    for (int i = 0; i < 25; i++) {
                        dataList.add("string" + i);
                    }
                    setAdapter();
                } else {
                    if (page > 3) {
                        if (adapter != null) {
                            adapter.setLoadAll(true);
                        }
                    } else {
                        for (int i = 22; i < 33; i++) {
                            dataList.add("string" + i);
                        }
                        setAdapter();
                    }
                }
            }
        }, 2000);


    }
}

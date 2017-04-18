package com.example.loadmoredemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.loadmoredemo.listener.OnLoadMoreListener;
import com.example.loadmoredemo.R;

import java.util.List;

/**
 * Created by Administrator on 2016/8/10.
 */
public class MyAdapter extends MyLoadMoreAdapter {

    List<String> dataList;
    private Context context;

    public MyAdapter(RecyclerView recyclerView, List<String> list, OnLoadMoreListener onloadMoreListener) {
        super(recyclerView, onloadMoreListener);
        this.dataList = list;
        Log.e("tag","dataList.size()"+dataList.size());
        context = recyclerView.getContext();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LOAD_MORE_ITEM) {
            return super.onCreateViewHolder(parent, viewType);
        } else {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
            return new MyHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder,position);
        if (holder instanceof MyHolder && position < dataList.size()) {
            ((MyHolder) holder).textView.setText(dataList.get(position));
        }
    }

    @Override
    int getDataSize() {
        if (dataList == null) {
            return 0;
        } else {
            return dataList.size();
        }
    }

    class MyHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public MyHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);
        }
    }
}

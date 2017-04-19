package com.example.loadmoredemo.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by dumingwei on 2017/4/19.
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {

    protected Context context;
    protected int layoutId;
    protected List<T> datas;
    protected LayoutInflater inflater;

    public BaseAdapter(Context context, int layoutId, List<T> datas) {
        this.context = context;
        this.layoutId = layoutId;
        this.datas = datas;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder baseViewHolder = BaseViewHolder.get(context, parent, layoutId);
        return baseViewHolder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        bindViewHolder(holder, datas.get(position));
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public abstract void bindViewHolder(BaseViewHolder holder, T t);
}

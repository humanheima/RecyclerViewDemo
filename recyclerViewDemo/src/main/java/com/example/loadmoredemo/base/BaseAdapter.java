package com.example.loadmoredemo.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.loadmoredemo.interfaces.ItemTypeCallBack;

import java.util.List;

/**
 * Created by dumingwei on 2017/4/19.
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> implements ItemTypeCallBack<T> {

    protected Context context;
    protected List<T> datas;
    protected LayoutInflater inflater;

    public BaseAdapter(Context context, List<T> datas) {
        this.context = context;
        this.datas = datas;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        return getHolderType(position, datas.get(position));
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return BaseViewHolder.get(context, parent, viewType);
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

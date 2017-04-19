package com.example.loadmoredemo.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.loadmoredemo.listener.OnItemClickListener;

/**
 * Created by dumingwei on 2017/4/19.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {

    private SparseArray<View> mViews;
    private View itemView;
    private Context context;

    public BaseViewHolder(Context context, View itemView) {
        super(itemView);
        this.context = context;
        this.itemView = itemView;
        mViews = new SparseArray<>();
    }

    public static BaseViewHolder get(Context context, ViewGroup parent, int layoutId) {
        View itemView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        BaseViewHolder holder = new BaseViewHolder(context, itemView);
        return holder;
    }

    /**
     * 通过ViewId获取控件
     *
     * @param viewId
     * @param <T>
     * @return
     */
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    public BaseViewHolder setTextViewText(int viewId, String text) {
        TextView textView = getView(viewId);
        textView.setText(text);
        return this;
    }

    public BaseViewHolder setImageViewResource(int viewId, int resId) {
        ImageView imageView = getView(viewId);
        imageView.setImageResource(resId);
        return this;
    }

    public BaseViewHolder setOnItemClickListener(final OnItemClickListener onItemClickListener) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, getAdapterPosition());
                }
            }
        });
        return this;
    }

    public BaseViewHolder setOnItemClickListener(int viewId, final OnItemClickListener onItemClickListener) {
        View view = getView(viewId);
        if (view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, getAdapterPosition());
                    }
                }
            });
        }
        return this;
    }

}

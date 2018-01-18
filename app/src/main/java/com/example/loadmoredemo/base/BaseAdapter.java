package com.example.loadmoredemo.base;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.loadmoredemo.interfaces.ItemTypeCallBack;

import java.util.List;

/**
 * Created by dumingwei on 2017/4/19.
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTypeCallBack<T> {

    private static final int HEAD_TYPE = 101;
    private static final int FOOT_TYPE = 102;
    protected Context context;
    private List<T> data;

    private View headView;
    private View footerView;

    private String TAG = "BaseAdapter";

    public BaseAdapter(Context context, List<T> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getItemCount() {
        int count = data.size();
        if (headView != null) {
            count++;
        }
        if (footerView != null) {
            count++;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (headView != null && position == 0) {
            return HEAD_TYPE;
        }
        if (footerView != null && position >= getItemCount()) {
            return FOOT_TYPE;
        }
        if (headView == null) {
            return getHolderType(position, data.get(position));
        } else {
            return getHolderType(position - 1, data.get(position - 1));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEAD_TYPE) {
            Log.e(TAG, "createHeadViewHolder");
            return new SpecialViewHolder(headView);
        } else if (viewType == FOOT_TYPE) {
            return new SpecialViewHolder(footerView);
        }
        return BaseViewHolder.get(context, parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (headView != null) {
            position = position - 1;
        }
        if (position < data.size() && !(holder instanceof SpecialViewHolder)) {
            bindViewHolder(((BaseViewHolder) holder), data.get(position), position);
        }
    }

    public abstract void bindViewHolder(BaseViewHolder holder, T t, int position);

    public void addHeadView(View view) {
        if (view == null) {
            throw new NullPointerException("HeadView is null!");
        }
        if (headView != null) {
            return;
        }
        headView = view;
        headView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT));
        notifyItemInserted(0);
    }

    public void removeHeadView() {
        if (headView != null) {
            headView = null;
            notifyItemRemoved(getItemCount());
        }
    }

    public void addFootView(View view) {
        if (view == null) {
            throw new NullPointerException("FooterView is null!");
        }
        if (footerView != null) {
            return;
        }
        footerView = view;
        footerView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT));
        notifyItemInserted(getItemCount());
    }

    public void removeFootView() {
        if (footerView != null) {
            footerView = null;
            notifyItemRemoved(getItemCount());
        }
    }

    /**
     * 设置网格布局footView占据一整行
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = ((GridLayoutManager) manager);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getItemViewType(position) == FOOT_TYPE ? gridLayoutManager.getSpanCount() : 1;
                }
            });
        }
    }

    /**
     * 设置瀑布流布局footView占据一整行
     */
    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int lastItemPosition = holder.getAdapterPosition();
        if (getItemViewType(lastItemPosition) == FOOT_TYPE) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = ((StaggeredGridLayoutManager.LayoutParams) lp);
                p.setFullSpan(true);
            }
        }
    }

    static class SpecialViewHolder extends RecyclerView.ViewHolder {

        public SpecialViewHolder(View itemView) {
            super(itemView);
        }
    }


}

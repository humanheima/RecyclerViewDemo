package com.example.loadmoredemo.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.loadmoredemo.interfaces.OnItemClickListener;
import com.example.loadmoredemo.interfaces.OnLoadMoreListener;
import com.example.loadmoredemo.R;

/**
 * Created by Administrator on 2016/8/10.
 */
public abstract class MyLoadMoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected static final int LOAD_MORE_ITEM = -100;
    private RecyclerView mRecyclerView;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean loading = true;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    //正在加载中item position
    private int loadMorePos = -1;

    //是否全部加载完成
    private boolean isLoadAll = false;

    private FootViewHolder footViewHolder;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public MyLoadMoreAdapter(RecyclerView recyclerView, final OnLoadMoreListener onloadMoreListener) {
        this.mRecyclerView = recyclerView;
        this.onLoadMoreListener = onloadMoreListener;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)
                            && dy > 0 && !isLoadAll) {
                        loading = true;
                        loadMorePos = getItemCount() - 1;
                        notifyItemInserted(getItemCount() - 1);
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                    }
                }
            });
        } else {
            //其他的layoutManger
        }

    }

    public final void reset() {
        loading = false;
        if (loadMorePos != -1) {
            notifyItemRemoved(loadMorePos);
        } else {
            notifyDataSetChanged();
        }
    }

    /**
     * 判读数据是否加载完毕，显示不同的布局
     * @param loadAll
     */
    public final void setLoadAll(boolean loadAll) {
        if (footViewHolder == null) {
            View view = LayoutInflater.from(mRecyclerView.getContext()).inflate(R.layout.footer_view_load_more, null);
            footViewHolder = new FootViewHolder(view);
        }
        isLoadAll = loadAll;
        if (isLoadAll && footViewHolder != null) {
            footViewHolder.mLLLoadNow.setVisibility(View.INVISIBLE);
            footViewHolder.mTxtLoadMore.setVisibility(View.VISIBLE);
        } else if (footViewHolder != null) {
            footViewHolder.mLLLoadNow.setVisibility(View.VISIBLE);
            footViewHolder.mTxtLoadMore.setVisibility(View.INVISIBLE);
        }
        if (!isLoadAll) {
            loadMorePos = -1;
        }
    }

    /**
     * 如果position大于List的大小，说明正在加载
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if (position >= getDataSize()) {
            return LOAD_MORE_ITEM;
        }
        return super.getItemViewType(position);
    }

    //抽象出一个方法用来返回和adapter绑定的List的size的大小
    abstract int getDataSize();

    @Override
    public int getItemCount() {
        if (loading) {
            //如果正在加载更多 返回List.size()+1
            return getDataSize() + 1;
        }
        return getDataSize();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (footViewHolder == null) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_view_load_more, parent, false);
            footViewHolder = new FootViewHolder(view);
        }
        return footViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        //在这里处理RecyclerView的item的点击事件
        if (!(holder instanceof FootViewHolder) && onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(view, position);
                }
            });
        }
    }

    /**
     * 底部加载的布局
     */
    protected class FootViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLLLoadNow;
        private TextView mTxtLoadMore;

        public FootViewHolder(View itemView) {
            super(itemView);
            mLLLoadNow = (LinearLayout) itemView.findViewById(R.id.footer_view_load_now);
            mTxtLoadMore = (TextView) itemView.findViewById(R.id.footer_view_load_all);
        }
    }
}

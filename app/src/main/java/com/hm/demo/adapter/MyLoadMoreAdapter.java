package com.hm.demo.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.hm.demo.R;
import com.hm.demo.base.BaseViewHolder;
import com.hm.demo.interfaces.ItemTypeCallBack;
import com.hm.demo.interfaces.OnLoadMoreListener;

import java.util.List;

/**
 * Created by dumingwei on 2016/8/10.
 */
public abstract class MyLoadMoreAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> implements ItemTypeCallBack<T> {

    private List<T> data;
    private Context context;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean loading = true;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    //正在加载中item position
    private int loadMorePos = -1;

    //是否全部加载完成
    private boolean isLoadAll = false;

    public MyLoadMoreAdapter(List<T> data, RecyclerView recyclerView, final OnLoadMoreListener onloadMoreListener) {
        context = recyclerView.getContext();
        this.data = data;
        this.onLoadMoreListener = onloadMoreListener;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    /*if (linearLayoutManager.getOrientation() == RecyclerView.VERTICAL) {
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
                    } else if (linearLayoutManager.getOrientation() == RecyclerView.HORIZONTAL) {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)
                                && dx > 0 && !isLoadAll) {
                            loading = true;
                            loadMorePos = getItemCount() - 1;
                            notifyItemInserted(getItemCount() - 1);
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore();
                            }
                        }
                    }*/

                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (linearLayoutManager.getOrientation() == RecyclerView.VERTICAL) {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold) && !isLoadAll) {
                            loading = true;
                            //loadMorePos = getItemCount() - 1;
                            //notifyItemInserted(getItemCount() - 1);
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore();
                            }
                        }
                    } else if (linearLayoutManager.getOrientation() == RecyclerView.HORIZONTAL) {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold) && !isLoadAll) {
                            loading = true;
                            //loadMorePos = getItemCount() - 1;
                            //notifyItemInserted(getItemCount() - 1);
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore();
                            }
                        }
                    }
                }
            });
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
     *
     * @param loadAll
     */
    public final void setLoadAll(boolean loadAll) {
        isLoadAll = loadAll;
        if (!isLoadAll) {
            loadMorePos = -1;
        }
        if (loadAll) {
            notifyDataSetChanged();
        }
    }

    public boolean isLoadAll() {
        return isLoadAll;
    }

    /**
     * 如果position大于List的大小，说明正在加载
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if (position >= getDataSize()) {
            return R.layout.footer_view_load_more;
        } else {
            return getHolderType(position, data.get(position));
        }
    }

    //抽象出一个方法用来返回和adapter绑定的List的size的大小
    public abstract int getDataSize();

    @Override
    public int getItemCount() {
       /* if (loading) {
            //如果正在加载更多 返回List.size()+1
            return getDataSize() + 1;
        }*/
        return getDataSize();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return BaseViewHolder.get(context, parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, final int position) {
        if (position < getDataSize()) {
            bindViewHolder(holder, data.get(position), position);
        } else {
            //bindFootView(holder);
        }
    }

    protected abstract void bindViewHolder(BaseViewHolder holder, T t, int position);

    protected abstract void bindFootView(BaseViewHolder holder);

}

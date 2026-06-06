# 关于RecyclerView 嵌套 RecyclerView，内部RecyclerView 的复用问题


不会复用。

```java
public void setAdapter(@Nullable Adapter adapter) {
    // bail out if layout is frozen
    setLayoutFrozen(false);
    //注意这两个参数
    setAdapterInternal(adapter, false, true);
    processDataSetCompletelyChanged(false);
    requestLayout();
}

```
```java

private void setAdapterInternal(@Nullable Adapter<?> adapter, boolean compatibleWithPrevious,
                                boolean removeAndRecycleViews) {
    if (mAdapter != null) {
        mAdapter.unregisterAdapterDataObserver(mObserver);
        mAdapter.onDetachedFromRecyclerView(this);
    }
    if (!compatibleWithPrevious || removeAndRecycleViews) {
        removeAndRecycleViews();
    }
    mAdapterHelper.reset();
    final Adapter<?> oldAdapter = mAdapter;
    mAdapter = adapter;
    if (adapter != null) {
        adapter.registerAdapterDataObserver(mObserver);
        adapter.onAttachedToRecyclerView(this);
    }
    if (mLayout != null) {
        mLayout.onAdapterChanged(oldAdapter, mAdapter);
    }
    //注释1处，这里会把Reycler的缓存清空，compatibleWithPrevious为false
    mRecycler.onAdapterChanged(oldAdapter, mAdapter, compatibleWithPrevious);
    mState.mStructureChanged = true;
}
```


```java
void onAdapterChanged(Adapter<?> oldAdapter, Adapter<?> newAdapter,
                boolean compatibleWithPrevious) {
    //清空mAttachedScrap，把 mCachedViews 中的ViewHolder 加入到 RecycledViewPool
    clear();
    poolingContainerDetach(oldAdapter, true);
    //清空 RecycledViewPool,compatibleWithPrevious 为 false
    getRecycledViewPool().onAdapterChanged(oldAdapter, newAdapter,
            compatibleWithPrevious);
    maybeSendPoolingContainerAttach();
}
```

RecycledViewPool 的 onAdapterChanged 方法。
```java
void onAdapterChanged(Adapter<?> oldAdapter, Adapter<?> newAdapter,
                boolean compatibleWithPrevious) {
            if (oldAdapter != null) {
                detach();
            }
            //注释1处，这里条件满足，会清空RecycledViewPool
            if (!compatibleWithPrevious && mAttachCountForClearing == 0) {
                clear();
            }
            if (newAdapter != null) {
                attach();
            }
        }
```

### 如果这种情况下想复用怎么解决？


1. new 一个 RecycledViewPool，内部的多个RecyclerView ，使用同一个 RecycledViewPool。

RecycledView 的 setRecycledViewPool 方法.
```java

private val recyclerViewPool = RecyclerView.RecycledViewPool().apply {
    //设置RecyclerViewPool中最大缓存数量，默认每个ItemViewType 5个。//setMaxRecycledViews(0, 10)
}

        
public void setRecycledViewPool(@Nullable RecycledViewPool pool) {
    mRecycler.setRecycledViewPool(pool);
}
```
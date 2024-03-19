
源码版本：`androidx1.3.2`

**分析场景：**

使用线性布局，方向为竖直方向，布局从上到下。宽高都是MATCH_PARENT。首次设置适配器以后，更改数据(注意，数据的数量没有变化)，然后调用`notifyDataSetChanged`。没有手动去滚动RecyclerView。

**先说下结论：**

1. notifyDataSetChanged 的时候，会将所有 ViewHolder 直接回收到 RecycledViewPool 中，每种 ItemViewType 默认缓存5个，超过5个直接丢弃。
2. 不会放入 mCachedViews中，因为我们给 ViewHolder 添加了标记位 FLAG_INVALID | FLAG_UPDATE。
4. 复用的时候，是从 RecyclerViewPool 中获取的。 如果 RecyclerViewPool 中的ViewHolder 数量不够，还会新创建ViewHolder。 无论是从 RecyclerViewPool 中获取的，还有新创建的ViewHolder，对应的 View 返回之前，都会执行 ViewHolder 的 onBindViewHolder 方法。
5. notifyDataSetChanged 的时候，没有动画效果。可以认为 dispatchLayoutStep1  和 dispatchLayoutStep3 方法 都没有作用。只有 dispatchLayoutStep2 方法做了回收和复用的工作。

## 调用 Adapter 的 notifyDataSetChanged 方法

```kotlin
binding.btnNotifyItemChanged.setOnClickListener {
    //还是8条数据
    for (i in 0 until 8) {
        arrayList.add(CheckBoxModel("notifyDataSetChanged 新的数据$i", false))
    }
    rv.adapter?.notifyDataSetChanged()
}
```

调用 notifyDataSetChanged 方法后，会触发 RecyclerView 的 mObserver 的 onChanged 方法。

```java
// RecyclerView.java
private final RecyclerViewDataObserver mObserver = new RecyclerViewDataObserver();
```

RecyclerView.RecyclerViewDataObserver 的 onChanged 方法。

```java
@Override
public void onChanged() {
    assertNotInLayoutOrScroll(null);
    //注释1处，将mStructureChanged置为true。
    mState.mStructureChanged = true;
    //注释2处，调用processDataSetCompletelyChanged方法。
    processDataSetCompletelyChanged(true);
    if(!mAdapterHelper.hasPendingUpdates()) {
        //注释3处，requestLayout
        requestLayout();
    }
}
```

```java
void processDataSetCompletelyChanged(boolean dispatchItemsChanged) {
    //置为true
    mDispatchItemsChangedEvent |= dispatchItemsChanged;
    //将mDataSetHasChangedAfterLayout置为true。
    mDataSetHasChangedAfterLayout = true;
    //注释1处，将所有ViewHolder标记为invalid。
    markKnownViewsInvalid();
}
```

注释1处，将所有ViewHolder标记为invalid。

```java
/**
 * Mark all known views as invalid. Used in response to a, "the whole world might have changed"
 * data change event.
 */
void markKnownViewsInvalid() {
    final int childCount = mChildHelper.getUnfilteredChildCount();
    for(int i = 0; i < childCount; i++) {
        final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
        if(holder != null && !holder.shouldIgnore()) {
            //注释0处，给ViewHolder设置标记 FLAG_INVALID | FLAG_UPDATE
            holder.addFlags(ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID);
        }
    }
    
    markItemDecorInsetsDirty();
    //注释1处，调用RecyclerView的mRecycler.markKnownViewsInvalid方法。
    mRecycler.markKnownViewsInvalid();
}
```
注释0处，给ViewHolder设置标记 **FLAG_INVALID | FLAG_UPDATE** 。

注释1处，调用RecyclerView的mRecycler.markKnownViewsInvalid方法。

```java
void markKnownViewsInvalid() {
    final int cachedCount = mCachedViews.size();
    for(int i = 0; i < cachedCount; i++) {
        final ViewHolder holder = mCachedViews.get(i);
        if(holder != null) {
            //如果 mCachedViews 中存在缓存的 ViewHolder ，就将 ViewHolder 添加标记 FLAG_INVALID | FLAG_UPDATE
            holder.addFlags(ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID);
            holder.addChangePayload(null);
        }
    }

    if(mAdapter == null || !mAdapter.hasStableIds()) {
        // we cannot re-use cached views in this case. Recycle them all
        //将 mCachedViews 中的 ViewHolder 回收到 RecycledViewPool 中，并将 mCachedViews 清空
        recycleAndClearCachedViews();
    }
}
```
如果 mCachedViews 中存在缓存的 ViewHolder ，就将 ViewHolder 添加标记 FLAG_INVALID | FLAG_UPDATE。

然后将 mCachedViews 中的 ViewHolder 回收到 RecycledViewPool 中，并将 mCachedViews 清空。(在我们这个分析场景中，没有发生滚动，所以其实 mCachedViews 中是没有缓存 ViewHolder 的。)

回到 RecyclerView.RecyclerViewDataObserver 的 onChanged 方法注释3处，调用 requestLayout() 后，会触发  onMeasure 和 onLayout 方法。

在我们的分析场景中， RecyclerView 宽高都是 **MATCH_PARENT** ,可以认为onMeasure 方法会直接 return。

onLayout 方法内部会调用dispatchLayout方法。

```java
void dispatchLayout() {
    //...
    mState.mIsMeasuring = false;
    if (mState.mLayoutStep == State.STEP_START) {
        //注释1处，调用dispatchLayoutStep1方法。
        dispatchLayoutStep1();
        mLayout.setExactMeasureSpecsFrom(this);
        //注释2处，调用dispatchLayoutStep2方法。
        dispatchLayoutStep2();
    } else if (mAdapterHelper.hasUpdates() || mLayout.getWidth() != getWidth()
            || mLayout.getHeight() != getHeight()) {
        // First 2 steps are done in onMeasure but looks like we have to run again due to
        // changed size.
        mLayout.setExactMeasureSpecsFrom(this);
        dispatchLayoutStep2();
    } else {
        mLayout.setExactMeasureSpecsFrom(this);
    }
    //注释3处，调用dispatchLayoutStep3方法。
    dispatchLayoutStep3();
}
```

注释1处，调用dispatchLayoutStep1方法。在 notifyDataSetChanged 的时候，不会有动画，可以认为 dispatchLayoutStep1 方法只是将 mState.mLayoutStep 置为了 State.STEP_LAYOUT。

```java
private void dispatchLayoutStep1() {
    //...
    //将当前布局步骤赋值为State.STEP_LAYOUT
    mState.mLayoutStep = State.STEP_LAYOUT;
}
```

注释2处，调用dispatchLayoutStep2方法。会调用LinearLayoutManager的onLayoutChildren方法。

```java
@Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    //...
    //注释1处，如果当前存在attach到RecyclerView的View，则临时detach，后面再复用。
    detachAndScrapAttachedViews(recycler);
    //...
    //注释2处，调用fill方法。填充RecyclerView    
    fill(recycler, mLayoutState, state, false);
    
}
```

注释1处， 调用RecyclerView.LayoutManager的detachAndScrapAttachedViews方法。
```java
public void detachAndScrapAttachedViews(@NonNull Recycler recycler) {
    final int childCount = getChildCount();
    for (int i = childCount - 1; i >= 0; i--) {
        final View v = getChildAt(i);
        //调用scrapOrRecycleView方法
        scrapOrRecycleView(recycler, i, v);
    }
}
```

RecyclerView.LayoutManager的scrapOrRecycleView方法。

```java
private void scrapOrRecycleView(Recycler recycler, int index, View view) {
    final ViewHolder viewHolder = getChildViewHolderInt(view);
    if (viewHolder.shouldIgnore()) {
        return;
    }
    //注释0处，notifyDataSetChanged 的时候，
    //我们前面给 ViewHolder 添加了标记位 FLAG_INVALID | FLAG_UPDATE，这里条件满足。
    if (viewHolder.isInvalid() && !viewHolder.isRemoved()
            && !mRecyclerView.mAdapter.hasStableIds()) {
        //注释1处，移除View
        removeViewAt(index);
        //注释2处，回收ViewHolder
        recycler.recycleViewHolderInternal(viewHolder);
    } else {
        //注释3处，detachView
        detachViewAt(index);
        //注释4处，回收View
        recycler.scrapView(view);
        mRecyclerView.mViewInfoStore.onViewDetached(viewHolder);
    }
}
```

注释0处，notifyDataSetChanged 的时候，我们前面给 ViewHolder 添加了标记位 FLAG_INVALID | FLAG_UPDATE，这里条件满足。

注释1处，移除View，这里会真正把子View从RecyclerView中移除 **RecyclerView.this.removeViewAt(index);**。

注释2处，回收ViewHolder，调用Recycler的recycleViewHolderInternal方法。

```java
void recycleViewHolderInternal(ViewHolder holder) {
    //...
    if(forceRecycle || holder.isRecyclable()) {
        //注释0处，notifyDataSetChanged 的时候，我们前面给 ViewHolder 添加了标记位 FLAG_INVALID | FLAG_UPDATE，这里条件不满足。
        if(mViewCacheMax > 0 && !holder.hasAnyOfTheFlags(ViewHolder.FLAG_INVALID 
                                | ViewHolder.FLAG_REMOVED 
                                | ViewHolder.FLAG_UPDATE 
                                | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN)) {
            //...
            //添加到 mCachedViews
            mCachedViews.add(targetCacheIndex, holder);
            cached = true;
        }
        if(!cached) {
            //注释1处，直接加入到RecycledViewPool中。
            addViewHolderToRecycledViewPool(holder, true);
            recycled = true;
        }
    } 
    //...
    mViewInfoStore.removeViewHolder(holder);
    
}
```

注释0处，notifyDataSetChanged 的时候，我们前面给 ViewHolder 添加了标记位 FLAG_INVALID | FLAG_UPDATE，这里条件不满足。**不会把 ViewHolder 缓存到  mCachedViews**。

注释1处，直接回收到到RecycledViewPool，每种 ItemViewType 默认缓存5个，超过5个直接丢弃。

**回到 dispatchLayoutStep2 方法注释2处**，调用 LinearLayoutManager 的 fill方法。填充RecyclerView。

```java
/**
 * @param recycler        当前关联到RecyclerView的recycler。
 * @param layoutState     该如何填充可用空间的配置信息。
 * @param state           Context passed by the RecyclerView to control scroll steps.
 * @param stopOnFocusable 如果为true的话，遇到第一个可获取焦点的View则停止填充。
 * @return 返回添加的像素。
 */
int fill(RecyclerView.Recycler recycler, LayoutState layoutState,
        RecyclerView.State state, boolean stopOnFocusable) {
    // max offset we should set is mFastScroll + available
    final int start = layoutState.mAvailable;
    //...
    //注释0处，获取可以填充的空间 remainingSpace。
    int remainingSpace = layoutState.mAvailable + layoutState.mExtraFillSpace;
    LayoutChunkResult layoutChunkResult = mLayoutChunkResult;
    //注释1处，如果还有可用空间并且还有更多的数据的话，就继续循环填充。
    while ((layoutState.mInfinite || remainingSpace > 0) && layoutState.hasMore(state)) {
        layoutChunkResult.resetInternal();
        //注释2处，调用 layoutChunk 方法 获取并添加子View，然后测量、布局子View并将分割线考虑在内。
        layoutChunk(recycler, state, layoutState, layoutChunkResult);
        //条件满足的话，跳出循环
        if (layoutChunkResult.mFinished) {
            break;
        }
        layoutState.mOffset += layoutChunkResult.mConsumed * layoutState.mLayoutDirection;
        /**
         * 消耗可用的填充空间remainingSpace。
         * Consume the available space if:
         * * layoutChunk did not request to be ignored
         * * OR we are laying out scrap children
         * * OR we are not doing pre-layout
         * 
         * 注意一下，被notifyItemRemoved的View的空间是不会被消耗的。`layoutChunkResult.mIgnoreConsumed = true`。不过在我们这个例子中用不到。
         * 
         */
        if (!layoutChunkResult.mIgnoreConsumed || layoutState.mScrapList != null
            || !state.isPreLayout()) {
                layoutState.mAvailable -= layoutChunkResult.mConsumed;
                //注释3处，减去填充的View消耗的空间
                remainingSpace -= layoutChunkResult.mConsumed;
        }
        if (stopOnFocusable && layoutChunkResult.mFocusable) {
            break;
        }
    }
    
    return start - layoutState.mAvailable;
}
```
注释0处，获取可以填充的空间 remainingSpace。
注释1处，如果还有可用空间并且还有更多的数据的话，就继续循环填充。
注释2处，调用  layoutChunk 方法 获取并添加子View，然后测量、布局子View并将分割线考虑在内。

```java
void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state,
    LayoutState layoutState, LayoutChunkResult result) {
    //注释1处，获取下一个View。可能是从缓存获取的，也可能是从新创建的。
    View view = layoutState.next(recycler);
    
    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
    if(layoutState.mScrapList == null) {
        if(mShouldReverseLayout == (layoutState.mLayoutDirection == LayoutState.LAYOUT_START)) {
            //添加到RecyclerView中 RecyclerView.this.addView(child, index);
            addView(view);
        } else {
            addView(view, 0);
        }
    } else {
        if(mShouldReverseLayout == (layoutState.mLayoutDirection == LayoutState.LAYOUT_START)) {
            addDisappearingView(view);
        } else {
            addDisappearingView(view, 0);
        }
    }
    //测量子View
    measureChildWithMargins(view, 0, 0);
    //记录消耗的空间，就是子View的高度包括decorationd等。
    result.mConsumed = mOrientationHelper.getDecoratedMeasurement(view);
    int left, top, right, bottom;
    if(mOrientation == VERTICAL) {
        if(isLayoutRTL()) {
            right = getWidth() - getPaddingRight();
            left = right - mOrientationHelper.getDecoratedMeasurementInOther(view);
        } else {
            left = getPaddingLeft();
            right = left + mOrientationHelper.getDecoratedMeasurementInOther(view);
        }
        if(layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
            bottom = layoutState.mOffset;
            top = layoutState.mOffset - result.mConsumed;
        } else {
            top = layoutState.mOffset;
            bottom = layoutState.mOffset + result.mConsumed;
        }
    } 
    // 布局子View
    layoutDecoratedWithMargins(view, left, top, right, bottom);
    if(params.isItemRemoved() || params.isItemChanged()) {
        //如果是 remove 或者 change 的话，就标记为true，表示不消耗空间。
        result.mIgnoreConsumed = true;
    }
    result.mFocusable = view.hasFocusable();
}
```

注释1处，获取下一个View。在我们这个例子中，是从 RecyclerViewPool 中获取的，还有新创建的。无论是从 RecyclerViewPool 中获取的，还有新创建的ViewHolder，对应的 View 返回之前，会执行 ViewHolder 的 onBindViewHolder 方法。

**回到dispatchLayout方法的注释3处**，调用dispatchLayoutStep3方法。在 notifyDataSetChanged 的时候， 可以认为 dispatchLayoutStep3 方法只是将 mState.mLayoutStep 置为了 State.STEP_START，并标记布局完成。

然后：就完了！



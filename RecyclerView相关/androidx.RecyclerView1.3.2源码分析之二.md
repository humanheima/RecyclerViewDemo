写在前面：今天2021年2月12日，农历正月初一，2021年春节。一个人在上海没有回家。从家里下了几个水饺吃了，然后来公司了，学会习，下午再跑个5公里。拍张照纪念一下。

![我的样子.jpg](https://upload-images.jianshu.io/upload_images/3611193-321b3b45c4563d0a.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

进入正题

 上篇文章：[RecyclerView源码分析之一](https://www.jianshu.com/p/9279774b3e80)

源码版本：`androidx1.3.2`


为什么预布局的时候也要 dispatchLayoutStep1 也要 mLayout.onLayoutChildren(mRecycler, mState) ？ 感觉这篇文章说的应该是对的。

* [详解RecyclerView的预布局](https://www.cnblogs.com/ZhaoxiCheung/p/17745376.html)

本文要旨：RecyclerView的回收和复用机制。

###草稿开始
###草稿结束



我们就以RecyclerView最简单的使用方式为例进行分析。使用线性布局，方向为竖直方向，布局从上到下。

先引用这篇文章中[RecyclerView 源码分析(三) - RecyclerView的缓存机制](https://www.jianshu.com/p/efe81969f69d)对RecyclerView缓存的总结，感觉非常清晰非常好。


|缓存级别|实际变量|含义|
|--------|-----|------------|
|一级缓存|`mAttachedScrap`和`mChangedScrap`|优先级最高的缓存，RecyclerView在获取ViewHolder时,优先会到这两个缓存来找。其中mAttachedScrap存储的是当前还在屏幕中的ViewHolder，mChangedScrap存储的是数据被更新的ViewHolder,比如说调用了Adapter的notifyItemChanged方法。|
|二级缓存|`mCachedViews`|默认大小为2，在滚动的时候会存储一些ViewHolder。|
|三级缓存|`ViewCacheExtension`|这个是自定义缓存，一般用不到。|
|四级缓存|`RecyclerViewPool`|根据ViewType来缓存ViewHolder，每个ViewType的数组大小为5，可以动态的改变。|


### RecyclerView的回收

#### 在滚动时候的回收

在RecyclerView滚动的时候，会调用LinearLayoutManager的fill方法。fill方法内部会发生View的回收和复用。

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
    //注释0处，如果layoutState.mScrollingOffset不为SCROLLING_OFFSET_NaN的话，调用recycleByLayoutState方法，从这个方法名，我们可以看出来，这是一个回收View的方法。
    if (layoutState.mScrollingOffset != LayoutState.SCROLLING_OFFSET_NaN) {
        //...
        recycleByLayoutState(recycler, layoutState);
    }
    int remainingSpace = layoutState.mAvailable + layoutState.mExtraFillSpace;
    LayoutChunkResult layoutChunkResult = mLayoutChunkResult;
    while ((layoutState.mInfinite || remainingSpace > 0) && layoutState.hasMore(state)) {
        layoutChunkResult.resetInternal();
        //注释1处，获取并添加子View，然后测量、布局子View并将分割线考虑在内。
        layoutChunk(recycler, state, layoutState, layoutChunkResult);
        //条件满足的话，跳出循环
        if (layoutChunkResult.mFinished) {
            break;
        }
        layoutState.mOffset += layoutChunkResult.mConsumed * layoutState.mLayoutDirection;
        //注释2处，这里也会判断是否要调用recycleByLayoutState方法。
        if (layoutState.mScrollingOffset != LayoutState.SCROLLING_OFFSET_NaN) {
                layoutState.mScrollingOffset += layoutChunkResult.mConsumed;
                if (layoutState.mAvailable < 0) {
                    layoutState.mScrollingOffset += layoutState.mAvailable;
                }
                recycleByLayoutState(recycler, layoutState);
            }
        if (stopOnFocusable && layoutChunkResult.mFocusable) {
            break;
        }
    }
    
    return start - layoutState.mAvailable;
}
```

注释0处和注释2处，如果layoutState.mScrollingOffset不为SCROLLING_OFFSET_NaN的话，调用recycleByLayoutState方法，从这个方法名可以看出来，这是一个回收View的方法。接下来我们看看其中的细节。

LinearLayoutManager的recycleByLayoutState方法。

```java
private void recycleByLayoutState(RecyclerView.Recycler recycler, LayoutState layoutState) {
    if (!layoutState.mRecycle || layoutState.mInfinite) {
        return;
    }
    int scrollingOffset = layoutState.mScrollingOffset;
    int noRecycleSpace = layoutState.mNoRecycleSpace;
    if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
        //注释1处
        recycleViewsFromEnd(recycler, scrollingOffset, noRecycleSpace);
    } else {
        //注释2处
        recycleViewsFromStart(recycler, scrollingOffset, noRecycleSpace);
    }
}
```

注释1处，以默认竖直方向的LinearLayoutManager来说，就是手指从上向下滑动的时候，回收从下面滑出屏幕的View。

注释2处，以默认竖直方向的LinearLayoutManager来说，就是手指从下向上滑动的时候，回收从上面滑出屏幕的View。

LinearLayoutManager的recycleViewsFromEnd方法。

```java
private void recycleViewsFromEnd(RecyclerView.Recycler recycler, int scrollingOffset,
            int noRecycleSpace) {
    final int childCount = getChildCount();
    if (scrollingOffset < 0) {
        return;
    }
    final int limit = mOrientationHelper.getEnd() - scrollingOffset + noRecycleSpace;
    if (mShouldReverseLayout) {
        //...
    } else {
        //从后向前遍历
        for (int i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (mOrientationHelper.getDecoratedStart(child) < limit
                    || mOrientationHelper.getTransformedStartWithDecoration(child) < limit) {
                //注释1处，遇到第一个View的top坐标小于limit就停止。然后调用recycleChildren方法，回收从childCount - 1到i之间的所有View。
                recycleChildren(recycler, childCount - 1, i);
                return;
            }
        }
    }
}
```

注释1处，遇到第一个View的top坐标小于limit就停止。然后调用recycleChildren方法，回收从`childCount - 1`到`i`之间的所有View。

LinearLayoutManager的recycleViewsFromStart方法。

```java
private void recycleViewsFromStart(RecyclerView.Recycler recycler, int scrollingOffset,
            int noRecycleSpace) {
    if (scrollingOffset < 0) {
        return;
    }
    // ignore padding, ViewGroup may not clip children.
    final int limit = scrollingOffset - noRecycleSpace;
    final int childCount = getChildCount();
    if (mShouldReverseLayout) {
        //...
    } else {
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (mOrientationHelper.getDecoratedEnd(child) > limit
                    || mOrientationHelper.getTransformedEndWithDecoration(child) > limit) {
                //注释1处，遇到第一个View的bottom坐标大于limit就停止。然后调用recycleChildren方法，回收从0到i之间的所有View。
                recycleChildren(recycler, 0, i);
                return;
            }
        }
    }
}
```
注释1处，遇到第一个View的bottom坐标大于limit就停止。然后调用recycleChildren方法，回收从 **[0,i)** 左闭右开之间的所有View。

接下来我们看看recycleChildren方法。

LinearLayoutManager的recycleChildren方法。

```java
/**
 * 回收指定索引之间的子View。
 *
 * @param startIndex 包括
 * @param endIndex   不包括
 */
private void recycleChildren(RecyclerView.Recycler recycler, int startIndex, int endIndex) {
    //startIndex == endIndex的话，直接return。比如滚动距离很短，没有View需要回收。
    if (startIndex == endIndex) {
        return;
    }
    if (endIndex > startIndex) {
        for (int i = endIndex - 1; i >= startIndex; i--) {
            removeAndRecycleViewAt(i, recycler);
        }
    } else {
        for (int i = startIndex; i > endIndex; i--) {
            removeAndRecycleViewAt(i, recycler);
        }
    }
}
```

调用父类RecyclerView.LayoutManager的removeAndRecycleViewAt方法。

```java
public void removeAndRecycleViewAt(int index, @NonNull Recycler recycler) {
    final View view = getChildAt(index);
    //注释1处，调用RecyclerView.LayoutManager的removeViewAt方法移除View。
    removeViewAt(index);
    //注释2处，回收View。
    recycler.recycleView(view);
}
```

注释1处，调用RecyclerView.LayoutManager的removeViewAt方法移除View。 最终会调用 RecyclerView.this.removeViewAt(index); 

注释2处，回收View。

```java
recycler.recycleView(view);
```

Recycler的recycleView方法。

```java
public void recycleView(@NonNull View view) {
    // This public recycle method tries to make view recycle-able since layout manager
    // intended to recycle this view (e.g. even if it is in scrap or change cache)
   //获取ViewHolder
    ViewHolder holder = getChildViewHolderInt(view);
    if (holder.isTmpDetached()) {
        removeDetachedView(view, false);
    }
    //注释1处，将ViewHolder从mChangedScrap或者mAttachedScrap中移除。
    if (holder.isScrap()) {
        holder.unScrap();
    } else if (holder.wasReturnedFromScrap()) {
        holder.clearReturnedFromScrapFlag();
    }
    //注释2处，调用recycleViewHolderInternal方法。
    recycleViewHolderInternal(holder);
           
    if (mItemAnimator != null && !holder.isRecyclable()) {
        mItemAnimator.endAnimation(holder);
    }
}
```

注释1处，如果ViewHolder在mChangedScrap中或者mAttachedScrap中，则将ViewHolder从mChangedScrap或者mAttachedScrap中移除。也就是说滑出屏幕的ViewHolder不会缓存在mChangedScrap中或者mAttachedScrap中。

注释2处，调用recycleViewHolderInternal方法。

Recycler的recycleViewHolderInternal方法。

```java
void recycleViewHolderInternal(ViewHolder holder) {
    //这里判断了在mChangedScrap中或者mAttachedScrap中的ViewHolder不会被回收，没有被移除的子View对应的ViewHolder也不能被回收。
    if (holder.isScrap() || holder.itemView.getParent() != null) {
        throw new IllegalArgumentException(
                "Scrapped or attached views may not be recycled. isScrap:"
                    + holder.isScrap() + " isAttached:"
                    + (holder.itemView.getParent() != null) + exceptionLabel());
    }
    //临时从屏幕上detach的ViewHolder也不能被回收。
    if (holder.isTmpDetached()) {
        throw new IllegalArgumentException("Tmp detached view should be removed "
                + "from RecyclerView before it can be recycled: " + holder
                + exceptionLabel());
    }

    if (holder.shouldIgnore()) {
        throw new IllegalArgumentException("Trying to recycle an ignored view holder. You"
                + " should first call stopIgnoringView(view) before calling recycle."
                + exceptionLabel());
    }
    //是否阻止回收
    final boolean transientStatePreventsRecycling = holder.doesTransientStatePreventRecycling();
    //是否强制回收
    final boolean forceRecycle = mAdapter != null
            && transientStatePreventsRecycling
            && mAdapter.onFailedToRecycleView(holder);
    boolean cached = false;
    boolean recycled = false;
    if (DEBUG && mCachedViews.contains(holder)) {
        throw new IllegalArgumentException("cached view received recycle internal? "
                + holder + exceptionLabel());
    }
    if (forceRecycle || holder.isRecyclable()) {
        if (mViewCacheMax > 0
                && !holder.hasAnyOfTheFlags(ViewHolder.FLAG_INVALID
                | ViewHolder.FLAG_REMOVED
                | ViewHolder.FLAG_UPDATE
                | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN)) {
            int cachedViewSize = mCachedViews.size();
            if (cachedViewSize >= mViewCacheMax && cachedViewSize > 0) {
                //注释1处，如果mCachedView缓存已达上限，从mCachedViews中移除最老的ViewHolder到RecyclerViewPool中
                recycleCachedViewAt(0);
                cachedViewSize--;
            }

            int targetCacheIndex = cachedViewSize;
            if (ALLOW_THREAD_GAP_WORK
                    && cachedViewSize > 0
                    && !mPrefetchRegistry.lastPrefetchIncludedPosition(holder.mPosition)) {
                // when adding the view, skip past most recently prefetched views
                int cacheIndex = cachedViewSize - 1;
                while (cacheIndex >= 0) {
                    int cachedPos = mCachedViews.get(cacheIndex).mPosition;
                    if (!mPrefetchRegistry.lastPrefetchIncludedPosition(cachedPos)) {
                        break;
                    }
                    cacheIndex--;
                }
                targetCacheIndex = cacheIndex + 1;
            }
            //注释2处，将要回收的ViewHolder加入mCachedViews
            mCachedViews.add(targetCacheIndex, holder);
            cached = true;
        }
        if (!cached) {
            //注释3处，没有成功缓存到mCachedViews，则加入到RecycledViewPool中。测试下来这里不会调用
            addViewHolderToRecycledViewPool(holder, true);
            recycled = true;
        }
    } else {
        //...
    }
    // even if the holder is not removed, we still call this method so that it is removed
    // from view holder lists.
    //跟动画相关的ViewHolder也从mViewInfoStore移除。
    mViewInfoStore.removeViewHolder(holder);
    if (!cached && !recycled && transientStatePreventsRecycling) {
        holder.mOwnerRecyclerView = null;
    }
}
``` 

注释1处，如果mCachedView缓存已达上限，从mCachedViews中移除最老的ViewHolder到RecyclerViewPool中。

Recycler的recycleCachedViewAt方法。

```java
void recycleCachedViewAt(int cachedViewIndex) {
    //从mCachedViews中获取ViewHolder
    ViewHolder viewHolder = mCachedViews.get(cachedViewIndex);
    //加入到RecycledViewPool
    addViewHolderToRecycledViewPool(viewHolder, true);
    //mCachedViews移除对应位置上的ViewHolder
    mCachedViews.remove(cachedViewIndex);
}
```

内部调用了Recycler的addViewHolderToRecycledViewPool方法。


```java
void addViewHolderToRecycledViewPool(@NonNull ViewHolder holder, boolean dispatchRecycled) {
    clearNestedRecyclerViewIfNotNested(holder);
    View itemView = holder.itemView;
    //...
    if (dispatchRecycled) {
        dispatchViewRecycled(holder);
    }
    holder.mOwnerRecyclerView = null;
    //注释1处，调用RecycledViewPool的putRecycledView方法。
    getRecycledViewPool().putRecycledView(holder);
}
```

注释1处，调用RecycledViewPool的putRecycledView方法。

```java
/**
 * 将废弃的ViewHolder加入到缓存池。
 * 如果ViewHolder对应的ViewType类型的缓存池已经满了，就直接将ViewHolder丢弃。
 *
 * @param scrap ViewHolder to be added to the pool.
 */
public void putRecycledView(ViewHolder scrap) {
    final int viewType = scrap.getItemViewType();
    //根据viewType获取缓存池，就是一个ArrayList<ViewHolder>
    final ArrayList<ViewHolder> scrapHeap = getScrapDataForType(viewType).mScrapHeap;
    //缓存池已经满了，不回收，直接return。
    if (mScrap.get(viewType).mMaxScrap <= scrapHeap.size()) {
        return;
    }
    //ViewHolder做一下清除Flag,position 等操作。
    scrap.resetInternal();
    //缓存ViewHolder
    scrapHeap.add(scrap);
}
```


Recycler的recycleViewHolderInternal方法注释2处，先将要回收的ViewHolder加入mCachedViews中。

注释3处，没有成功缓存到mCachedViews，则加入到RecycledViewPool中。测试下来这里好像不会调用。
因为在注释1处，如果mCachedView缓存已达上限，会调用 addViewHolderToRecycledViewPool 把从mCachedViews中移除最老的ViewHolder到RecyclerViewPool中。
所以这里再次调用 addViewHolderToRecycledViewPool 是为了兜底？

小结：在滚动的时候，RecyclerView会回收滑出屏幕的View，然后加入到mCachedViews中。
如果mCachedView缓存已达上限(默认是2)，会调用 addViewHolderToRecycledViewPool 把从mCachedViews中移除最老的ViewHolder到RecyclerViewPool中(每种ItemViewType的缓存池大小默认是5)。


#### 数据变化的时候的回收

当我们调用Adapter的 notifyDataSetChanged、notifyItemChanged、notifyItemRemoved、notifyItemInserted、等方法的时候，会调用RecyclerView的requestLayout方法，然后会调用RecyclerView的onLayout方法，然后会调用RecyclerView的dispatchLayout方法。

#### 调用 Adapter 的 notifyDataSetChanged 方法

```kotlin
binding.btnNotifyItemChanged.setOnClickListener {
    rv.adapter?.notifyDataSetChanged()
}
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
        requestLayout();
    }
}
```

```java
/**
 * Processes the fact that, as far as we can tell, the data set has completely changed.
 *
 * <ul>
 *   <li>Once layout occurs, all attached items should be discarded or animated.
 *   <li>Attached items are labeled as invalid.
 *   <li>Because items may still be prefetched between a "data set completely changed"
 *       event and a layout event, all cached items are discarded.
 * </ul>
 *
 * @param dispatchItemsChanged Whether to call
 *                             {@link LayoutManager#onItemsChanged(RecyclerView)} during
 *                             measure/layout.
 */
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
            //给ViewHolder设置标记 FLAG_INVALID | FLAG_UPDATE
            holder.addFlags(ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID);
        }
    }
    
    markItemDecorInsetsDirty();
    //注释1处，调用RecyclerView的mRecycler.markKnownViewsInvalid方法。
    mRecycler.markKnownViewsInvalid();
}
```

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


调用 requestLayout() 后， 会触发调用dispatchLayout方法。

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
    //注释0处，notifyDataSetChanged 的时候，我们前面给 ViewHolder 添加了标记位 FLAG_INVALID | FLAG_UPDATE，这里条件满足。
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

注释1处，移除View，这里会真正把子View从RecyclerView中移除。
注释2处，回收ViewHolder，调用Recycler的recycleViewHolderInternal方法。

```java
void recycleViewHolderInternal(ViewHolder holder) {
    //...
    final boolean transientStatePreventsRecycling = holder
        .doesTransientStatePreventRecycling();
    final boolean forceRecycle = mAdapter != null && transientStatePreventsRecycling && mAdapter.onFailedToRecycleView(holder);
    boolean cached = false;
    boolean recycled = false;
    if(sDebugAssertionsEnabled && mCachedViews.contains(holder)) {
        throw new IllegalArgumentException("cached view received recycle internal? " + holder + exceptionLabel());
    }
    if(forceRecycle || holder.isRecyclable()) {
        //notifyDataSetChanged 的时候，我们前面给 ViewHolder 添加了标记位 FLAG_INVALID | FLAG_UPDATE，这里条件不满足。
        if(mViewCacheMax > 0 && !holder.hasAnyOfTheFlags(ViewHolder.FLAG_INVALID | ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN)) {
            //...
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

注释1处，直接加入到RecycledViewPool中。

注释3处，调用dispatchLayoutStep3方法。在 notifyDataSetChanged 的时候， 可以认为 dispatchLayoutStep3 方法只是将 mState.mLayoutStep 置为了 State.STEP_START，并标记布局完成。

**小结：** notifyDataSetChanged 的时候，会将所有 ViewHolder 直接回收到RecycledViewPool中。 mAttachedScrap、mChangedScrap、mCachedViews 中是没有缓存 ViewHolder 的。都会被回收到RecycledViewPool中。


#### 调用 Adapter 的 notifyItemChanged 方法

参考 RecyclerView.Adapter 调用 notifyItemChanged 之后 动画是怎样执行的?.md 的分析。这里只说结论。

小结： 

* 在 dispatchLayoutStep1 预布局的时候， 调用 mLayout.onLayoutChildren(mRecycler, mState)。在这个过程中：

1.会执行一次 detachAndScrapAttachedViews 方法，会把所有的 itemView detachViewFromParent，同时回收itemView对应的 ViewHolder。没有变化的 ViewHolder 添加到 mAttachedScrap 中，变化的（notifyItemChanged 的 item）ViewHolder 添加到 mChangedScrap 中。


2.然后又会执行 fill 方法，会把所有的 itemView attachViewToParent（mChildHelper.attachViewToParent(child, index, child.getLayoutParams(), false);）。没有变化的 ViewHolder 是从 mAttachedScrap 中取出来复用的，变化的 ViewHolder 是从 mChangedScrap 中取出来复用的。 然后把取出来的 ViewHolder 从  mAttachedScrap  或者 mChangedScrap 移除。

疑问：为啥要在预布局的时候，做 mLayout.onLayoutChildren(mRecycler, mState); 这个操作呢？

对 onItemRemoved() 的 场景有用。这个时候 remove的View不会占据空间，remainingSpace,会继续布局。把屏幕之外的View布局进来，并记录动画信息。

* 在 dispatchLayoutStep2 布局的时候， 调用 mLayout.onLayoutChildren(mRecycler, mState)。在这个过程中：

会执行一次 detachAndScrapAttachedViews 方法，会把所有的 itemView detachViewFromParent，同时回收itemView对应的 ViewHolder。没有变化的 ViewHolder 添加到 mAttachedScrap 中，变化的（notifyItemChanged 的 item）ViewHolder 添加到 mChangedScrap 中。

然后又会执行 fill 方法，会把所有没有变化的 itemView 重新  attachViewToParent（mChildHelper.attachViewToParent(child, index, child.getLayoutParams(), false);）。没有变化的 ViewHolder 是从 mAttachedScrap 中取出来复用的。

注意：这个时候，不是预布局阶段了，是不会从 mChangedScrap 中 查找ViewHolder 来复用的。 在我们这里例子中，是创建了一个新的 ViewHolder，然后 调用 bindViewHolder 方法。然后返回新创建的ViewHolder。

另外，这个时候，新创建的 ViewHolder 对应的View是添加到 RecyclerView 中的 mChildHelper.addView(child, index, false);。 这个时候老的View还没有移除，只是 detachViewFromParent 了。 mChangedScrap 也还存在改变了的老的ViewHolder。

* 在 dispatchLayoutStep3 布局的时候， 
动画开始前，这里会把 mChangedScrap 中存储的ViewHolder 从 mChangedScrap 中移除，然后重新把对应的View attach 到 RecyclerView, `mChildHelper.attachViewToParent(child, index, child.getLayoutParams(), false);` ，并把对应的View添加到 ChildHelper.mHiddenViews 中。
动画结束后，会把 ViewHolder 回收到 RecycledViewPool 中。


#### 调用 notifyItemInserted

RecyclerView.RecyclerViewDataObserver 的 onItemRangeInserted 方法。

```java
@Override
public void onItemRangeInserted(int positionStart, int itemCount) {
    assertNotInLayoutOrScroll(null);
    if(mAdapterHelper.onItemRangeInserted(positionStart, itemCount)) {
        triggerUpdateProcessor();
    }
}
```

AdapterHelper 的 onItemRangeInserted 方法。

```java
/**
 * @return True if updates should be processed.
 */
boolean onItemRangeInserted(int positionStart, int itemCount) {
    if(itemCount < 1) {
        return false;
    }
    //添加了一个 UpdateOp.ADD 操作
    mPendingUpdates.add(obtainUpdateOp(UpdateOp.ADD, positionStart, itemCount, null));
    mExistingUpdateTypes |= UpdateOp.ADD;
    //返回true，触发 requestLayout 
    return mPendingUpdates.size() == 1;
}
```

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
    //调用dispatchLayoutStep3方法。
    dispatchLayoutStep3();
}
```

dispatchLayoutStep1 方法
处理动画标记位

```java
@Override
public void offsetPositionsForAdd(int positionStart, int itemCount) {
    //偏移所有的ViewHolder的位置为插入留出位置
    offsetPositionRecordsForInsert(positionStart, itemCount);
    mItemsAddedOrRemoved = true;
}
```

```java
void offsetPositionRecordsForInsert(int positionStart, int itemCount) {
    final int childCount = mChildHelper.getUnfilteredChildCount();
    for(int i = 0; i < childCount; i++) {
        //比如我们在position=1 的位置插入1条数据，那么从position=2开始的所有的ViewHolder的位置都要加1。
        final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
        if(holder != null && !holder.shouldIgnore() && holder.mPosition >= positionStart) {
            if(sVerboseLoggingEnabled) {
                Log.d(TAG, "offsetPositionRecordsForInsert attached child " + i + " holder " + holder + " now at position " + (holder.mPosition + itemCount));
            }
            holder.offsetPosition(itemCount, false);
            mState.mStructureChanged = true;
        }
    }
    mRecycler.offsetPositionRecordsForInsert(positionStart, itemCount);
    requestLayout();
}
```
detachAndScrapAttachedViews 的时候，都放在 mAttachedScrap 中。

fill 方法的时候，会从 mAttachedScrap 中取出来复用的。重新 attachToParent 之后会从  mAttachedScrap 中移除。

dispatchLayoutStep2 方法

detachAndScrapAttachedViews 的时候，都放在 mAttachedScrap 中。

fill 方法的时候，老的位置，会从 mAttachedScrap 中取出来复用的。重新 attachToParent 之后会从  mAttachedScrap 中移除。

新插入的数据位置，会创建新ViewHolder  mAdapter.createViewHolder(RecyclerView.this, type)。然后会 addView，不是 attachToParent。

注意，在我们的例子中，fill 结束的时候，mAttachedScrap 中，会有一个 ViewHolder 存在(因为我们插入了一个数据，可以理解为 这个ViewHolder 被挤出屏幕了，没有被布局)。

存在这种情况，当我们插入一个数据的时候，本在屏幕可见的ViewHolder被挤出屏幕了，没有被布局。
mAttachedScrap 中还存在这个ViewHolder  这个 View 已经 detachToParent 了，那么我们还需要再布局这个ViewHolder attachViewToParent ，实现从屏幕中移动到屏幕外的(translationY)动画。

在动画之前还会把这个 ItemView 通过调用 addAnimatingView  加入到 ChildHelper.hiddenViews 中。
然后动画结束后，会把这个屏幕外的View ChildHelper.hiddenViews 移除，也会从RecyclerView中移除。 RecyclerView.this.removeViewAt(index); 并把这个  ViewHolder 缓存到  mCachedViews

然后会调用 layoutForPredictiveAnimations 方法

这个时候，recycler.getScrapList() 不为空。

```java
@NonNull
public List<ViewHolder> getScrapList() {
    return mUnmodifiableAttachedScrap;
}

 private final List<ViewHolder>
mUnmodifiableAttachedScrap = Collections.unmodifiableList(mAttachedScrap);

```

内会还会再调用一次 fill 方法。 在 layoutChunk 方法中，会调用 addDisappearingView(view); 方法。 
把从 recycler.getScrapList() 取出来的对应的ViewHolder 的ItemView，添加到RecyclerView 中。 addDisappearingView(view);

然后会从 mAttachedScrap 中移除。
最后将 mLayoutState.mScrapList = null;

这个被挤出去的ViewHolder 会执行 animateDisappearance 方法。真正执行的是移动动画。

```java
@Override
public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
        
    int oldLeft = preLayoutInfo.left;
    //比如开始坐标是2100
    int oldTop = preLayoutInfo.top;
    View disappearingItemView = viewHolder.itemView;
    int newLeft = postLayoutInfo == null ? disappearingItemView.getLeft() : postLayoutInfo.left;
    //插入一条数据之后，插入View的高度是300，那么这个被挤出去的 View新的位置是2400
    int newTop = postLayoutInfo == null ? disappearingItemView.getTop() : postLayoutInfo.top;
    if(!viewHolder.isRemoved() && (oldLeft != newLeft || oldTop != newTop)) {
        //
        disappearingItemView.layout(newLeft, newTop,
            newLeft + disappearingItemView.getWidth(),
            newTop + disappearingItemView.getHeight());
        if(DEBUG) {
            Log.d(TAG, "DISAPPEARING: " + viewHolder + " with view " + disappearingItemView);
        }
        //注释1处，移动动画
        return animateMove(viewHolder, oldLeft, oldTop, newLeft, newTop);
    } else {
        if(DEBUG) {
            Log.d(TAG, "REMOVED: " + viewHolder + " with view " + disappearingItemView);
        }
        return animateRemove(viewHolder);
    }
}
```
移动的动画，要移动的View(有多个，插入View之下的所有RecyclerView的子View)，注意： 在 dispatchLayoutStep2 的时候，已经把view 布局到 正确的位置了（也就是老的位置+300像素）。
这里 先设置 translationY 设置为-300，然后让它移动到  translationY 为0 的位置（就是移动到布局后正确的位置上）。就会向下移动300像素。
这300像素，就是新插入的View 的高度。



### notifyItemRemoved


参考 [详解RecyclerView的预布局](https://www.cnblogs.com/ZhaoxiCheung/p/17745376.html)

开始item的 ViewHodler0, ViewHodler1

notifyItemRemoved(1)

### 进入 dispatchLayoutStep1 方法

* processAdapterUpdatesAndSetAnimationFlags 方法：

ViewHolder0: ViewHolder{867dd3 position=0 id=-1, oldPos=-1, pLpos:-1}  没有变化。

ViewHolder1: ViewHolder{79954e6 position=0 id=-1, oldPos=1, pLpos:1 removed}  **这里注意一下：position 变成0了**。

* 调用 saveOldPositions() 之后，

ViewHolder0: ViewHolder{867dd3 position=0 id=-1, oldPos=0, pLpos:-1}

ViewHolder: ViewHolder{79954e6 position=0 id=-1, oldPos=1, pLpos:1 removed}

fill 的时候

ViewHolder0: ViewHolder{7cc0d10 position=0 id=-1, oldPos=0, pLpos:0 scrap [attachedScrap] tmpDetached no parent}
mPreLayoutPosition 改为0了

ViewHolder1: ViewHolder{40b6a27 position=0 id=-1, oldPos=1, pLpos:1 scrap [attachedScrap] removed tmpDetached no parent}

ViewHolder1 标记为 removed，在 layoutChunk 中不会减去 remainingSpace。

```java
if (params.isItemRemoved() || params.isItemChanged()) {
    result.mIgnoreConsumed = true;
}
```

fill 方法会继续布局。把 Item3 的 ViewHolder 布局出来。这个时候，ViewHolder 的 position =2，
 offsetPosition 是1(有一个移除的数量) 注意下面的注释1处。

```
else if (!holder.isBound() || holder.needsUpdate() || holder.isInvalid()) {
                if (sDebugAssertionsEnabled && holder.isRemoved()) {
                    throw new IllegalStateException("Removed holder should be bound and it should"
                            + " come here only in pre-layout. Holder: " + holder
                            + exceptionLabel());
                }
                //注释1处，尝试绑定ViewHolder，这个时候  offsetPosition =1
                //会从 offsetPosition =1 获取数据，这是正常的，因为我们 开始已经把 数据位置1上的数据删除了。这时候获取的其实是位置2上的数据。
                final int offsetPosition = mAdapterHelper.findPositionOffset(position);
                bound = tryBindViewHolderByDeadline(holder, offsetPosition, position, deadlineNs);
            }

```

ViewHolder2: ViewHolder{2ca2088 position=1 id=-1, oldPos=-1, pLpos:2 no parent}


下面的逻辑 注意，在预布局的时候，屏幕中的被移除的 ViewHolder 会添加 ViewHolder.FLAG_REMOVED 标记。
然后不消耗 remainingSpace(mLayoutState.mOffset 会累加)。
因为还有剩余空间，就会创建新的ViewHolder(或者从缓存里取)，然后布局到屏幕中。

新创建的 ViewHolder 会添加标记位  flags |= ItemAnimator.FLAG_APPEARED_IN_PRE_LAYOUT; 


```java
private void dispatchLayoutStep1() {
    //...
    
    if(mState.mRunPredictiveAnimations) {
        // Step 1: run prelayout: This will use the old positions of items. The layout manager
        // is expected to layout everything, even removed items (though not to add removed
        // items back to the container). This gives the pre-layout position of APPEARING views
        // which come into existence as part of the real layout.

        // Save old positions so that LayoutManager can run its mapping logic.
        saveOldPositions();
        final boolean didStructureChange = mState.mStructureChanged;
        mState.mStructureChanged = false;
        // temporarily disable flag because we are asking for previous layout
        mLayout.onLayoutChildren(mRecycler, mState);
        mState.mStructureChanged = didStructureChange;

        for(int i = 0; i < mChildHelper.getChildCount(); ++i) {
            final View child = mChildHelper.getChildAt(i);
            final ViewHolder viewHolder = getChildViewHolderInt(child);
            if(viewHolder.shouldIgnore()) {
                continue;
            }
            if(!mViewInfoStore.isInPreLayout(viewHolder)) {
                int flags = ItemAnimator.buildAdapterChangeFlagsForAnimations(viewHolder);
                boolean wasHidden = viewHolder
                    .hasAnyOfTheFlags(ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST);
                if(!wasHidden) {
                    //注释1处，新创建的ViewHolder，添加标记位 FLAG_APPEARED_IN_PRE_LAYOUT
                    flags |= ItemAnimator.FLAG_APPEARED_IN_PRE_LAYOUT;
                }
                //Note: 注释2处，在预布局过程中出现的新的ViewHolder，记录其动画信息
                final ItemHolderInfo animationInfo = mItemAnimator.recordPreLayoutInformation(
                    mState, viewHolder, flags, viewHolder.getUnmodifiedPayloads());
                if(wasHidden) {
                    recordAnimationInfoIfBouncedHiddenView(viewHolder, animationInfo);
                } else {
                    //注释3处，将ViewHolder加入到mViewInfoStore中
                    mViewInfoStore.addToAppearedInPreLayoutHolders(viewHolder, animationInfo);
                }
            }
        }
        // we don't process disappearing list because they may re-appear in post layout pass.
        clearOldPositions();
    } else {
        clearOldPositions();
    }
    onExitLayoutOrScroll();
    stopInterceptRequestLayout(false);
    mState.mLayoutStep = State.STEP_LAYOUT;
}
```

然后 clearOldPosition

ViewHolder0: ViewHolder{7cc0d10 position=0 id=-1, oldPos=-1, pLpos:-1}
ViewHolder1: ViewHolder{40b6a27 position=0 id=-1, oldPos=-1, pLpos:-1 removed}
ViewHolder2: ViewHolder{45b542b position=1 id=-1, oldPos=-1, pLpos:-1}

** dispatchLayoutStep1 结束。

总结一下

* 在预布局阶段，有一个新创建的 ViewHolder2，是新插入的。 
* 现在有3个 Item。 有标记位为 removed 的 ViewHolder1，是被移除的。

dispatchLayoutStep2 方法

开始 fill 的时候，获取ViewHolder  getScrapOrHiddenOrCachedHolderForPosition


注意 `holder.getLayoutPosition() == position`  和 `(mState.mInPreLayout || !holder.isRemoved())` 这两个条件。

```java
ViewHolder getScrapOrHiddenOrCachedHolderForPosition(int position, boolean dryRun) {
    final int scrapCount = mAttachedScrap.size();

    // Try first for an exact, non-invalid match from scrap.
    for(int i = 0; i < scrapCount; i++) {
        final ViewHolder holder = mAttachedScrap.get(i);
        //注释1处，关注条件判断  (mState.mInPreLayout || !holder.isRemoved())
        if(!holder.wasReturnedFromScrap() && holder.getLayoutPosition() == position && !holder.isInvalid() 
        && (mState.mInPreLayout || !holder.isRemoved())) {
            holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
            return holder;
        }
    }
    //...
}
```

现在 mAttachedScrap 中有3个 ViewHolder。

ViewHolder0: ViewHolder{7cc0d10 position=0 id=-1, oldPos=-1, pLpos:-1}
ViewHolder1: ViewHolder{40b6a27 position=0 id=-1, oldPos=-1, pLpos:-1 removed}
ViewHolder2: ViewHolder{45b542b position=1 id=-1, oldPos=-1, pLpos:-1}

position = 0 的时候，会从 mAttachedScrap 中取  ViewHolder0 出来复用的。 `holder.getLayoutPosition() == position` = 0
position = 1 的时候，会从 mAttachedScrap 中取  ViewHolder2 出来复用的。 `holder.getLayoutPosition() == position` = 1

然后这个时候，fill 没有剩余空间 `remainingSpace = 0` ，就不会再继续布局了。这个时候RecyclerView中就只有2个Item。这个时候

mAttachedScrap 还是有一个ViewHolder的，就是被移除的那个。

接下来会执行 layoutForPredictiveAnimations 。但是 mRecycler.getScrapList() 中的item是 removed的，没做什么操作。

** dispatchLayoutStep2 结束。


* dispatchLayoutStep3 阶段。

新创建的 ViewHolder2 现在已经在 position=1的位置上了。会执行 translationY 动画。 

先把 translationY 设置为-1200，然后让它移动到  translationY 为0 的位置（就是移动到布局后正确的位置上）。 
就会向上移动300像素。实现从屏幕下方进入的效果。

被移除的 ViewHolder1 会执行 animateDisappearance 方法。是一个透明度渐出动画。

被移除的 ViewHolder 会在第二布局阶段 detachViewFromParent以后，在 fill 方法中，不会重新 attachViewToParent。

在透明度渐出动画的开始之前的时候，会调用 addAnimatingView， 会重新 attachViewToParent 上的。

然后动画结束之后，会把这个ViewHolder 从 RecyclerView 中移除。并且会把这个 ViewHolder 缓存到 RecycledViewPool





### RecyclerView的复用

```java
void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state,
        LayoutState layoutState, LayoutChunkResult result) {
    //注释1处，获取子View，可能是从缓存中或者新创建的View。后面分析缓存相关的点的时候再看。
    View view = layoutState.next(recycler);
    if (view == null) {
        //注释2处，如果获取到的子View为null，将LayoutChunkResult的mFinished置为true，用于跳出循环然后直接return。
        result.mFinished = true;
        return;
    }
    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
    if (layoutState.mScrapList == null) {
        if (mShouldReverseLayout == (layoutState.mLayoutDirection
                == LayoutState.LAYOUT_START)) {
            //注释3处
            addView(view);  
        } else {
            //注释4处
            addView(view, 0);
        }
    } 
    //...
   
}
```

上篇文章我们分析了layoutChunk方法。在注释1处，获取子View，可能是从缓存中或者新创建的View。我们现在就看一看其中细节。

```java
View view = layoutState.next(recycler);
```

LayoutState的next方法。

```java
View next(RecyclerView.Recycler recycler) {
    if (mScrapList != null) {
        return nextViewFromScrapList();
    }
    //注释1处，调用Recycler的getViewForPosition方法获取View
    final View view = recycler.getViewForPosition(mCurrentPosition);
    mCurrentPosition += mItemDirection;
    return view;
}
```

注释1处，调用Recycler的getViewForPosition方法获取View。RecyclerView.Recycler就是处理RecyclerView的缓存复用相关逻辑的类。


```java
@NonNull
public View getViewForPosition(int position) {
    return getViewForPosition(position, false);
}

View getViewForPosition(int position, boolean dryRun) {
    return tryGetViewHolderForPositionByDeadline(position, dryRun, FOREVER_NS).itemView;
}
```

tryGetViewHolderForPositionByDeadline方法里面的逻辑就是先从缓存里面取ViewHolder进行复用，如果没有可复用的ViewHolder，则进行创建。


```java
@Nullable
ViewHolder tryGetViewHolderForPositionByDeadline(int position,
        boolean dryRun, long deadlineNs) {
    if (position < 0 || position >= mState.getItemCount()) {
        throw new IndexOutOfBoundsException("Invalid item position " + position
                + "(" + position + "). Item count:" + mState.getItemCount()
                + exceptionLabel());
    }
    boolean fromScrapOrHiddenOrCache = false;
    ViewHolder holder = null;
    // 0) If there is a changed scrap, try to find from there
    //注释0处，从mChangedScrap中获取ViewHolder，注意只有LinearLayoutManager没有开启自动测量的情况下，mState.isPreLayout()才可能是是true，我们这里就忽略mState.isPreLayout()为true的情况。
    if (mState.isPreLayout()) {
        holder = getChangedScrapViewForPosition(position);
        fromScrapOrHiddenOrCache = holder != null;
    }
    // 1) Find by position from scrap/hidden list/cache
    //注释1处，从mAttachedScrap、mHiddenViews、mCachedViews中获取ViewHolder
    if (holder == null) {
        holder = getScrapOrHiddenOrCachedHolderForPosition(position, dryRun);
        if (holder != null) {
            if (!validateViewHolderForOffsetPosition(holder)) {
                // recycle holder (and unscrap if relevant) since it can't be used
                if (!dryRun) {
                    // we would like to recycle this but need to make sure it is not used by
                    // animation logic etc.
                    holder.addFlags(ViewHolder.FLAG_INVALID);
                    if (holder.isScrap()) {
                        removeDetachedView(holder.itemView, false);
                        holder.unScrap();
                    } else if (holder.wasReturnedFromScrap()) {
                        holder.clearReturnedFromScrapFlag();
                    }
                    recycleViewHolderInternal(holder);
                }
                holder = null;
            } else {
                fromScrapOrHiddenOrCache = true;
            }
        }
    }
    if (holder == null) {
        final int offsetPosition = mAdapterHelper.findPositionOffset(position);
        if (offsetPosition < 0 || offsetPosition >= mAdapter.getItemCount()) {
            throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item "
                    + "position " + position + "(offset:" + offsetPosition + ")."
                    + "state:" + mState.getItemCount() + exceptionLabel());
        }
       
        //获取ViewType
        final int type = mAdapter.getItemViewType(offsetPosition);
        // 2) Find from scrap/cache via stable ids, if exists
        //注释2处，通过stable ids从mAttachedScrap、mCachedViews中获取ViewHolder。
        if (mAdapter.hasStableIds()) {
            holder = getScrapOrCachedViewForId(mAdapter.getItemId(offsetPosition),
                    type, dryRun);
            if (holder != null) {
                // update position
                holder.mPosition = offsetPosition;
                fromScrapOrHiddenOrCache = true;
            }
        }
        //注释3处，从我们自定义的缓存扩展mViewCacheExtension中获取ViewHolder
        if (holder == null && mViewCacheExtension != null) {
            // We are NOT sending the offsetPosition because LayoutManager does not
            // know it.
            final View view = mViewCacheExtension
                    .getViewForPositionAndType(this, position, type);
            if (view != null) {
                holder = getChildViewHolder(view);
                if (holder == null) {
                    throw new IllegalArgumentException("getViewForPositionAndType returned"
                            + " a view which does not have a ViewHolder"
                            + exceptionLabel());
                } else if (holder.shouldIgnore()) {
                    throw new IllegalArgumentException("getViewForPositionAndType returned"
                            + " a view that is ignored. You must call stopIgnoring before"
                            + " returning this view." + exceptionLabel());
                }
            }
        }
        if (holder == null) { // fallback to pool
            if (DEBUG) {
                Log.d(TAG, "tryGetViewHolderForPositionByDeadline("
                        + position + ") fetching from shared pool");
            }
            //注释4处，从RecycledViewPool中获取ViewHolder
            holder = getRecycledViewPool().getRecycledView(type);
            if (holder != null) {
                holder.resetInternal();
                if (FORCE_INVALIDATE_DISPLAY_LIST) {
                    invalidateDisplayListInt(holder);
                }
            }
        }
        if (holder == null) {
            long start = getNanoTime();
            if (deadlineNs != FOREVER_NS
                    && !mRecyclerPool.willCreateInTime(type, start, deadlineNs)) {
                // abort - we have a deadline we can't meet
                return null;
            }
            //注释5处，从适配器中创建一个新的ViewHolder
            holder = mAdapter.createViewHolder(RecyclerView.this, type);
            if (ALLOW_THREAD_GAP_WORK) {
                // only bother finding nested RV if prefetching
                RecyclerView innerView = findNestedRecyclerView(holder.itemView);
                if (innerView != null) {
                    holder.mNestedRecyclerView = new WeakReference<>(innerView);
                }
            }

            long end = getNanoTime();
            mRecyclerPool.factorInCreateTime(type, end - start);
            if (DEBUG) {
                Log.d(TAG, "tryGetViewHolderForPositionByDeadline created new ViewHolder");
            }
        }
    }

    // This is very ugly but the only place we can grab this information
    // before the View is rebound and returned to the LayoutManager for post layout ops.
    // We don't need this in pre-layout since the VH is not updated by the LM.
    if (fromScrapOrHiddenOrCache && !mState.isPreLayout() && holder
            .hasAnyOfTheFlags(ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST)) {
        holder.setFlags(0, ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST);
        if (mState.mRunSimpleAnimations) {
            int changeFlags = ItemAnimator
                    .buildAdapterChangeFlagsForAnimations(holder);
            changeFlags |= ItemAnimator.FLAG_APPEARED_IN_PRE_LAYOUT;
            final ItemHolderInfo info = mItemAnimator.recordPreLayoutInformation(mState,
                    holder, changeFlags, holder.getUnmodifiedPayloads());
            recordAnimationInfoIfBouncedHiddenView(holder, info);
        }
    }

    boolean bound = false;
    if (mState.isPreLayout() && holder.isBound()) {
        // do not update unless we absolutely have to.
        holder.mPreLayoutPosition = position;
    } else if (!holder.isBound() || holder.needsUpdate() || holder.isInvalid()) {
        if (DEBUG && holder.isRemoved()) {
            throw new IllegalStateException("Removed holder should be bound and it should"
                    + " come here only in pre-layout. Holder: " + holder
                    + exceptionLabel());
        }
        final int offsetPosition = mAdapterHelper.findPositionOffset(position);
        //注释6处，绑定ViewHolder
        bound = tryBindViewHolderByDeadline(holder, offsetPosition, position, deadlineNs);
    }

    final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
    final LayoutParams rvLayoutParams;
    if (lp == null) {
        rvLayoutParams = (LayoutParams) generateDefaultLayoutParams();
        holder.itemView.setLayoutParams(rvLayoutParams);
    } else if (!checkLayoutParams(lp)) {
        rvLayoutParams = (LayoutParams) generateLayoutParams(lp);
        holder.itemView.setLayoutParams(rvLayoutParams);
    } else {
        rvLayoutParams = (LayoutParams) lp;
    }
    rvLayoutParams.mViewHolder = holder;
    rvLayoutParams.mPendingInvalidate = fromScrapOrHiddenOrCache && bound;
    //最终返回ViewHolder对象。
    return holder;
}
```

注释0处，从mChangedScrap中获取ViewHolder。

```java
if (mState.isPreLayout()) {//开启了预布局
    holder = getChangedScrapViewForPosition(position);
    fromScrapOrHiddenOrCache = holder != null;
}
```

注意只有LinearLayoutManager没有开启自动测量的情况下，mState.isPreLayout()才可能是是true，并不是常规行为，我们这里就忽略这种情况。


注释1处，从mAttachedScrap、mHiddenViews、mCachedViews中获取ViewHolder。

```java
holder = getScrapOrHiddenOrCachedHolderForPosition(position, dryRun);
```

RecyclerView.Recycler的getScrapOrHiddenOrCachedHolderForPosition方法。

```java
ViewHolder getScrapOrHiddenOrCachedHolderForPosition(int position, boolean dryRun) {

    final int scrapCount = mAttachedScrap.size();
    //注释1处，先从mAttachedScrap查找
    for (int i = 0; i < scrapCount; i++) {
        final ViewHolder holder = mAttachedScrap.get(i);
        if (!holder.wasReturnedFromScrap() && holder.getLayoutPosition() == position
                    && !holder.isInvalid() && (mState.mInPreLayout || !holder.isRemoved())) {
                //添加标志位FLAG_RETURNED_FROM_SCRAP
                holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
             return holder;
        }
    }

    if (!dryRun) {
        //注释2处，从mHiddenViews中查找View，然后根据View获取ViewHolder
        View view = mChildHelper.findHiddenNonRemovedView(position);
        if (view != null) {
         // This View is good to be used. We just need to unhide, detach and move to the
         // scrap list.
         final ViewHolder vh = getChildViewHolderInt(view);
         //将获取到的View从mHiddenViews移除。
         mChildHelper.unhide(view);
         int layoutIndex = mChildHelper.indexOfChild(view);
         if (layoutIndex == RecyclerView.NO_POSITION) {
             throw new IllegalStateException("layout index should not be -1 after "
                        + "unhiding a view:" + vh + exceptionLabel());
         }
         mChildHelper.detachViewFromParent(layoutIndex);
         //注释3处，将View对应的ViewHolder加入到mAttachedScrap或者mChangedScrap中
         scrapView(view);
         //添加标记位，多了一个FLAG_BOUNCED_FROM_HIDDEN_LIST的标记位
         vh.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP
                    | ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST);
         return vh;
        }
    }

    //注释4处，从mCachedViews中获取
    final int cacheSize = mCachedViews.size();
    for (int i = 0; i < cacheSize; i++) {
        final ViewHolder holder = mCachedViews.get(i);
        // invalid view holders may be in cache if adapter has stable ids as they can be
        // retrieved via getScrapOrCachedViewForId
        if (!holder.isInvalid() && holder.getLayoutPosition() == position
                && !holder.isAttachedToTransitionOverlay()) {
            if (!dryRun) {
                //找到ViewHolder后，从mCachedViews中移除。
                mCachedViews.remove(i);
            }
            if (DEBUG) {
                Log.d(TAG, "getScrapOrHiddenOrCachedHolderForPosition(" + position
                        + ") found match in cache: " + holder);
            }
            return holder;
        }
    }
    return null;
}
```

注释1处，先从mAttachedScrap查找。

注释2处，从mHiddenViews中查找View，然后根据View获取ViewHolder。获取到的View会从mHiddenViews移除，然后注释3处，将View对应的ViewHolder加入到mAttachedScrap或者mChangedScrap中。

注释4处，从mCachedViews中获取。

然后我们回到RecyclerView.Recycler的tryGetViewHolderForPositionByDeadline方法的注释2处。通过`stable ids`从mAttachedScrap、mCachedViews中获取ViewHolder。

RecyclerView.Recycler的tryGetViewHolderForPositionByDeadline方法的注释3处，从我们自定义的缓存扩展mViewCacheExtension中获取ViewHolder，这个自定义的ViewCacheExtension就先忽略了。
（你自定义过ViewCacheExtension吗？，没有。你自定义过？我也没有？正常人谁自定义ViewCacheExtension呀。是，自定义ViewCacheExtension的叫正常人吗？哈哈，纯属搞笑。）

RecyclerView.Recycler的tryGetViewHolderForPositionByDeadline方法的注释4处，从RecycledViewPool中获取ViewHolder。

```java
holder = getRecycledViewPool().getRecycledView(type);
```

RecycledView.RecycledViewPool类，这里提一下，RecycledViewPool可以用来在多个RecyclerView之间来复用View。

```java
/**
 * RecycledViewPool可以让你在多个RecyclerViews之间复用Views。 lets you share Views between multiple RecyclerViews.
 * <p>
 * 如果你想在多个RecyclerView之间复用Views，可以创建一个RecycledViewPool实例，然后调用 RecyclerView的 setRecycledViewPool(RecycledViewPool) 方法设置。
 * <p>
 * 如果你不提供RecycledViewPool实例，RecyclerView会自动创建一个。
 */
public static class RecycledViewPool {
    //每个viewType类型的ViewHolder默认缓存5个
    private static final int DEFAULT_MAX_SCRAP = 5;
    
    
    static class ScrapData {
        final ArrayList<ViewHolder> mScrapHeap = new ArrayList<>();
        int mMaxScrap = DEFAULT_MAX_SCRAP;
        long mCreateRunningAverageNs = 0;
        long mBindRunningAverageNs = 0;
    }
    
    //使用SparseArray缓存不ViewType类型的ViewHolder，SparseArray的key就是ViewType类
    SparseArray<ScrapData> mScrap = new SparseArray<>();


    @Nullable
    public ViewHolder getRecycledView(int viewType) {
        //注释1处，先根据ViewType类获取ScrapData，然后从ScrapData获取ViewHolder。
        final ScrapData scrapData = mScrap.get(viewType);
        if (scrapData != null && !scrapData.mScrapHeap.isEmpty()) {
            final ArrayList<ViewHolder> scrapHeap = scrapData.mScrapHeap;
            for (int i = scrapHeap.size() - 1; i >= 0; i--) {
                if (!scrapHeap.get(i).isAttachedToTransitionOverlay()) {
                    return scrapHeap.remove(i);
                }
            }
        }
        return null;
    }
    //...


}

```

注释1处，从RecycledViewPool中获取ViewHolder，先根据ViewType类获取ScrapData，然后从ScrapData获取ViewHolder。

RecyclerView.Recycler的tryGetViewHolderForPositionByDeadline方法的注释5处，无法从缓存中获取ViewHolder，就使用适配器中创建一个新的ViewHolder。


RecyclerView.Recycler的tryGetViewHolderForPositionByDeadline方法的注释6处，绑定ViewHolder

```java
bound = tryBindViewHolderByDeadline(holder, offsetPosition, position, deadlineNs);
```


Recycler的tryBindViewHolderByDeadline方法。

```java
private boolean tryBindViewHolderByDeadline(@NonNull ViewHolder holder, int offsetPosition,
                int position, long deadlineNs) {
    holder.mOwnerRecyclerView = RecyclerView.this;
    final int viewType = holder.getItemViewType();
    long startBindNs = getNanoTime();
    if (deadlineNs != FOREVER_NS
            && !mRecyclerPool.willBindInTime(viewType, startBindNs, deadlineNs)) {
        // abort - we have a deadline we can't meet
        return false;
    }
    //适配器绑定ViewHolder
    mAdapter.bindViewHolder(holder, offsetPosition);
    long endBindNs = getNanoTime();
    mRecyclerPool.factorInBindTime(holder.getItemViewType(), endBindNs - startBindNs);
    attachAccessibilityDelegateOnBind(holder);
    if (mState.isPreLayout()) {
        holder.mPreLayoutPosition = position;
    }
    return true;
}
```


结尾：关于RecyclerView的动画相关的内容，并没有进行分析。主要还是关注RecyclerView的回收和复用的大致逻辑。

参考链接：

* [RecyclerView源码分析之一](https://www.jianshu.com/p/9279774b3e80)
* [RecyclerView 源码分析(三) - RecyclerView的缓存机制](https://www.jianshu.com/p/efe81969f69d)

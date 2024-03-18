源码版本：`androidx1.3.2`

本文要旨：RecyclerView的回收和复用机制。

先说下结论：

1. 在正常滚动过程中，RecyclerView ViewHolder 的回收和复用只涉及 Recycler.mCachedViews、Recycler.mRecyclerPool。
2. RecyclerView会回收滑出屏幕的View，当回收一个ViewHolder **A** 的时候，如果如果mCachedView缓存已达上限(默认是2)，会调用 addViewHolderToRecycledViewPool 从mCachedViews中移除最老的ViewHolder，添加到RecyclerViewPool中(每种ItemViewType的缓存池大小默认是5)。然后把A加入到Recycler.mCachedViews。
3. 从 Recycler.mCachedViews 返回的 ViewHolder 是不会重新 onBindViewHolder 的。
4. 从 RecyclerViewPool 返回的 ViewHolder 是会重新 onBindViewHolder 的。

其他注意的点：在调试过程中发现，GapWorker 的 prefetchPositionWithDeadline 方法干扰到 RecyclerView 的回收和复用过程。有时候会导致 导致 Recycler.mCachedViews的 size变成了3(默认是2)。这个不是主要流程，我们先忽略，有兴趣可以仔细研究研究。

分析场景：

RecyclerView 使用线性布局，方向为竖直方向，布局从上到下。第一次设置 LayoutManager 和 Adapter ，正常显示数据以后，滚动 RecyclerView。

RecyclerView的缓存的 ViewHolder 的地方有 Recycler.mAttachedScrap、Recycler.mChangedScrap、Recycler.mCachedViews、Recycler.mRecyclerPool。
缓存 View  的地方有 Recycler.mViewCacheExtension、ChildHelper.mHiddenViews。 如下所示：

```java
public final class Recycler {

    final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
    ArrayList<ViewHolder> mChangedScrap = null;

    final ArrayList<ViewHolder> mCachedViews = new ArrayList<ViewHolder>();

    private final List<ViewHolder>
            mUnmodifiableAttachedScrap = Collections.unmodifiableList(mAttachedScrap);

    RecycledViewPool mRecyclerPool;

    private ViewCacheExtension mViewCacheExtension;
    //...
}
```

```java
class ChildHelper {
    private final List<View> mHiddenViews = new ArrayList<View>();
    //...
}
```


本篇文章只分析在滚动的时候的回收和复用。只涉及 Recycler.mCachedViews、Recycler.mRecyclerPool 。在后续的文章中，会进行一个总结说明每一个缓存的作用。


#### 在滚动时候的回收

1. 在滚动的时候，RecyclerView会回收滑出屏幕的View，然后加入到mCachedViews中。
2. 如果mCachedView缓存已达上限(默认是2)，会调用 addViewHolderToRecycledViewPool 把从mCachedViews中移除最老的ViewHolder到RecyclerViewPool中(每种ItemViewType的缓存池大小默认是5)。

在RecyclerView滚动的时候，会调用 LinearLayoutManager 的fill方法。fill方法内部会发生View的回收和复用。

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
        //在滚动的时候，会把滑出屏幕的View回收。
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

注释0处和注释2处，调用 recycleByLayoutState 方法，从这个方法名可以看出来，这是一个回收View的方法。接下来我们看看其中的细节。

LinearLayoutManager 的 recycleByLayoutState 方法。

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

注释1处，遇到第一个View的top坐标小于limit就停止。然后调用recycleChildren方法，回收从 **[childCount - 1,i)** 左闭右开之间的所有View。

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
    if (holder.isScrap()) {
        //注释1处，将ViewHolder从mChangedScrap或者mAttachedScrap中移除。
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

注释1处，如果ViewHolder是从 mChangedScrap中或者mAttachedScrap 返回的，则将ViewHolder从mChangedScrap或者mAttachedScrap中移除。也就是说滑出屏幕的ViewHolder不会缓存在mChangedScrap中或者mAttachedScrap中。
对于我们分析的这个场景，mChangedScrap中或者mAttachedScrap 是没有 ViewHolder 的。条件不满足。

注释2处，调用 Recycler 的recycleViewHolderInternal方法。

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
    //可以回收
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
    //...
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
    final ArrayList < ViewHolder > scrapHeap = getScrapDataForType(viewType).mScrapHeap;
    if(mScrap.get(viewType).mMaxScrap <= scrapHeap.size()) {
        //注释1处，如果超过最大限制(默认是5)，直接丢弃。
        PoolingContainer.callPoolingContainerOnRelease(scrap.itemView);
        return;
    }
    if(sDebugAssertionsEnabled && scrapHeap.contains(scrap)) {
        throw new IllegalArgumentException("this scrap item already exists");
    }
    scrap.resetInternal();
    //加入到缓存池
    scrapHeap.add(scrap);
}

```

Recycler的 recycleViewHolderInternal 方法注释2处，先将要回收的ViewHolder加入mCachedViews中。

注释3处，没有成功缓存到 mCachedViews，则加入到RecycledViewPool中。比如说，我们设置 mCachedViews 的大小为0的时候，那么就不会缓存到 mCachedViews 中，直接加入到 RecycledViewPool 中。


小结：在滚动的时候，RecyclerView会回收滑出屏幕的View，然后加入到 mCachedViews 中。 如果mCachedView缓存已达上限(默认是2)，会调用 addViewHolderToRecycledViewPool 把从mCachedViews中移除最老的ViewHolder到RecyclerViewPool中(每种ItemViewType的缓存池大小默认是5)。

### RecyclerView的在滚动状态下复用

LinearLayoutManager 的 layoutChunk 方法。

```java
void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state,
        LayoutState layoutState, LayoutChunkResult result) {
    //注释1处，获取子View，可能是从缓存中或者新创建的View。
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

LayoutState 的 next 方法。

```java
View next(RecyclerView.Recycler recycler) {
    if (mScrapList != null) {
        //在我们的分析场景中为null
        return nextViewFromScrapList();
    }
    //注释1处，调用Recycler的getViewForPosition方法获取View，mCurrentPosition是当前的位置。
    final View view = recycler.getViewForPosition(mCurrentPosition);
    mCurrentPosition += mItemDirection;
    return view;
}
```

注释1处，调用Recycler的getViewForPosition方法获取View，mCurrentPosition是当前的位置。

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
    // 注释0处，预布局的时候，从mChangedScrap中获取ViewHolder。 正常滚动的时候，不是预布局状态，不会从这里找。
    if (mState.isPreLayout()) {
        holder = getChangedScrapViewForPosition(position);
        fromScrapOrHiddenOrCache = holder != null;
    }
    // 1) Find by position from scrap/hidden list/cache
    //注释1处，从Recycler.mAttachedScrap、ChildHelper.mHiddenViews、Recycler.mCachedViews中获取ViewHolder
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

注释0处，预布局的时候，从mChangedScrap中获取ViewHolder。 正常滚动的时候，不是预布局状态，不会从这里找。

注释1处，从Recycler.mAttachedScrap、ChildHelper.mHiddenViews、Recycler.mCachedViews中获取ViewHolder。

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

注释1处，先从mAttachedScrap查找。正常在滚动状态的时候， mAttachedScrap 是空的。

注释2处，从mHiddenViews中查找View，然后根据View获取ViewHolder。正常在滚动状态的时候 mHiddenViews 也是空的。

注释4处，从mCachedViews中获取。正常在这里可以找到。从mCachedViews中获取的ViewHolder 不需要重新绑定。

RecyclerView.Recycler的tryGetViewHolderForPositionByDeadline方法的注释3处，从我们自定义的缓存扩展mViewCacheExtension中获取ViewHolder，这个自定义的ViewCacheExtension就先忽略了。
（你自定义过ViewCacheExtension吗？，没有。你自定义过？我也没有？正常人谁自定义ViewCacheExtension呀。是，自定义ViewCacheExtension的叫正常人吗？哈哈，纯属搞笑。）

RecyclerView.Recycler的tryGetViewHolderForPositionByDeadline方法的注释4处，从RecycledViewPool中获取ViewHolder。 从 RecycledViewPool 中获取的 ViewHolder，需要重新 bindViewHolder。

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

回到 RecyclerView.Recycler的tryGetViewHolderForPositionByDeadline方法的注释5处，无法从缓存中获取ViewHolder，就使用适配器中创建一个新的ViewHolder。

RecyclerView.Recycler的tryGetViewHolderForPositionByDeadline方法的注释6处，绑定ViewHolder。

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

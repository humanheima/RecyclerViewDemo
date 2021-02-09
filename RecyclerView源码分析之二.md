这篇文章主要分析一下RecyclerView的复用机制。

我们从哪里开始看起呢？我们先从我们自定义的适配器的onCreateViewHolder方法开始。

```java
public class CheckBoxAdapter extends RecyclerView.Adapter<CheckBoxAdapter.ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //...
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //...
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
           //...
        }
    }
}
```

RecyclerView.Adapter的createViewHolder方法内部调用了我们实现的onBindViewHolder方法。

```java
@NonNull
public final VH createViewHolder(@NonNull ViewGroup parent, int viewType) {
    try {
        TraceCompat.beginSection(TRACE_CREATE_VIEW_TAG);
        //注释1处，调用onCreateViewHolder方法
        final VH holder = onCreateViewHolder(parent, viewType);
        if (holder.itemView.getParent() != null) {
            throw new IllegalStateException("ViewHolder views must not be attached when"
                    + " created. Ensure that you are not passing 'true' to the attachToRoot"
                    + " parameter of LayoutInflater.inflate(..., boolean attachToRoot)");
        }
        holder.mItemViewType = viewType;
        return holder;
    } finally {
        TraceCompat.endSection();
    }
}
```

那么接下来我们就是哪里调用了RecyclerView.Adapter的createViewHolder方法。

我们发现**只有**RecyclerView.Recycler的tryGetViewHolderForPositionByDeadline方法里面调用了。

RecyclerView.Recycler就是处理RecyclerView的缓存复用相关逻辑的类。而tryGetViewHolderForPositionByDeadline方法里面的逻辑就是先从缓存里面取ViewHolder进行复用，如果没有可复用的ViewHolder，则进行创建。

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

然后我们回到RecyclerView.Recycler的tryGetViewHolderForPositionByDeadline方法的注释2处。通过stable ids从mAttachedScrap、mCachedViews中获取ViewHolder。

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












源码版本：`androidx1.3.2`

我们就以RecyclerView最简单的使用方式为例进行分析。使用线性布局，方向为竖直方向，布局从上到下。

开始就2条数据。然后新插入的1条数据会插入到position=1的位置上。把原来的position=1的数据挤到屏幕之外。

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

```java
private void dispatchLayoutStep1() {
    mState.assertLayoutStep(State.STEP_START);
    fillRemainingScrollValues(mState);
    mState.mIsMeasuring = false;
    startInterceptRequestLayout();
    mViewInfoStore.clear();
    onEnterLayoutOrScroll();
    //注释0处，处理动画标志位
    processAdapterUpdatesAndSetAnimationFlags();
    //...    
    if(mState.mRunSimpleAnimations) {
        // Step 0: Find out where all non-removed items are, pre-layout
        int count = mChildHelper.getChildCount();
        for(int i = 0; i < count; ++i) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getChildAt(i));
            if(holder.shouldIgnore() || (holder.isInvalid() && !mAdapter.hasStableIds())) {
                continue;
            }
            //注释1处，记录动画之前的所有ViewHolder信息 animationInfo，保存到 mViewInfoStore 中
            // 动画信息包括 itemView 的top、left、right、bottom 等
            final ItemHolderInfo animationInfo = mItemAnimator
                .recordPreLayoutInformation(mState, holder,
                    ItemAnimator.buildAdapterChangeFlagsForAnimations(holder),
                    holder.getUnmodifiedPayloads());
            //保存到 mViewInfoStore 中
            mViewInfoStore.addToPreLayout(holder, animationInfo);
            if(mState.mTrackOldChangeHolders && holder.isUpdated() && !holder.isRemoved() && !holder.shouldIgnore() && !holder.isInvalid()) {
                long key = getChangedHolderKey(holder);
                //注释2处，注意这里只会添加变化的viewHolder 会 添加到 oldChangeHolders 中
                mViewInfoStore.addToOldChangeHolders(key, holder);
            }
        }
    }
    if(mState.mRunPredictiveAnimations) {
        // onLayoutChildren
        //注释3处，预布局的时候调用 mLayout.onLayoutChildren(mRecycler, mState);
        mLayout.onLayoutChildren(mRecycler, mState);
        mState.mStructureChanged = didStructureChange;
        //...
        clearOldPositions();
    } else {
        clearOldPositions();
    }
    onExitLayoutOrScroll();
    stopInterceptRequestLayout(false);
    mState.mLayoutStep = State.STEP_LAYOUT;
}
```


注释0处，处理动画标志位

```java
private void processAdapterUpdatesAndSetAnimationFlags() {
    if (predictiveItemAnimationsEnabled()) {
        //注释1处，对于LinearLayoutManager来说条件满足，调用预处理方法。
        mAdapterHelper.preProcess();
    } else {
        mAdapterHelper.consumeUpdatesInOnePass();
    }
    boolean animationTypeSupported = mItemsAddedOrRemoved || mItemsChanged;
    //在我们这个场景中为true
    mState.mRunSimpleAnimations = mFirstLayoutComplete
            && mItemAnimator != null
            && (mDataSetHasChangedAfterLayout
            || animationTypeSupported
            || mLayout.mRequestedSimpleAnimations)
            && (!mDataSetHasChangedAfterLayout
            || mAdapter.hasStableIds());
    //在我们这个场景中为true
    mState.mRunPredictiveAnimations = mState.mRunSimpleAnimations
            && animationTypeSupported
            && !mDataSetHasChangedAfterLayout
            && predictiveItemAnimationsEnabled();
}
```

注释1处，对于LinearLayoutManager来说条件满足，调用预处理方法。最终会调用到 AdapterHelper.Callback 的 offsetPositionsForAdd 方法。

```java
@Override
public void offsetPositionsForAdd(int positionStart, int itemCount) {
    //注释1处，调用 RecyclerView 的 offsetPositionRecordsForInsert 方法。
    offsetPositionRecordsForInsert(positionStart, itemCount);
    mItemsAddedOrRemoved = true;
}
```

注释1处，调用 RecyclerView 的 offsetPositionRecordsForInsert 方法。 偏移所有的ViewHolder的位置为插入留出位置。

```java
void offsetPositionRecordsForInsert(int positionStart, int itemCount) {
    final int childCount = mChildHelper.getUnfilteredChildCount();
    for(int i = 0; i < childCount; i++) {
        //注释1处，从插入位置开始，到最后一个ViewHolder的位置，老的ViewHolder position 都要加上 itemCount。
        //比如我们在position=1 的位置插入1条数据，那么从position =1 开始的所有的ViewHolder的位置都要加1。
        final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
        if(holder != null && !holder.shouldIgnore() && holder.mPosition >= positionStart) {
            
            holder.offsetPosition(itemCount, false);
            mState.mStructureChanged = true;
        }
    }
    mRecycler.offsetPositionRecordsForInsert(positionStart, itemCount);
    requestLayout();
}
```
注释1处，从插入位置开始，到最后一个ViewHolder的位置，老的ViewHolder position 都要加上 itemCount。
比如我们在position=1 的位置插入1条数据，那么从position =1 开始的所有的ViewHolder的位置都要加1。改变的是
ViewHolder的  `mPosition` 字段。

回到 dispatchLayoutStep1 方法的注释1处，记录动画之前的所有ViewHolder信息 animationInfo，保存到 mViewInfoStore 中。

dispatchLayoutStep1 方法，注释3处，预布局的时候调用 mLayout.onLayoutChildren(mRecycler, mState); 布局一次。

在 mLayout.onLayoutChildren(mRecycler, mState); 方法内部。

* 先回收ViewHolder 。detachAndScrapAttachedViews 的时候，View detachFromParent，ViewHolder 都回收到 mAttachedScrap 中。

fill 方法的时候，ViewHolder 会从 mAttachedScrap 中取出来复用的，不会重新 onBindViewHolder。重新 attachToParent 之后，会从  mAttachedScrap 中移除。

dispatchLayoutStep2 方法

detachAndScrapAttachedViews 的时候，都放在 mAttachedScrap 中。

fill 方法的时候，老的位置，会从 mAttachedScrap 中取出来复用的，不会重新 onBindViewHolder。重新 attachToParent 之后会从  mAttachedScrap 中移除。

新插入的数据位置，会创建新ViewHolder  mAdapter.createViewHolder(RecyclerView.this, type)，会 onBindViewHolder。然后会 addView，不是 attachToParent。

注意，在我们的例子中，fill 结束的时候，mAttachedScrap 中，会有一个 ViewHolder 存在(因为我们插入了一个数据，可以理解为 这个ViewHolder 被挤出屏幕了，没有被布局)。
mAttachedScrap 中还存在这个ViewHolder  这个 View 已经 detachToParent 了，那么我们还需要再布局这个ViewHolder attachViewToParent ，实现从屏幕中移动到屏幕外的(translationY)动画。

这个逻辑是通过 layoutForPredictiveAnimations 方法 来执行的。

```java
/**
 * If necessary, layouts new items for predictive animations
 * 如果必要的话，布局新的ItemView 用于预测动画
 */
private void layoutForPredictiveAnimations(RecyclerView.Recycler recycler,
    RecyclerView.State state, int startOffset,
    int endOffset) {
    // If there are scrap children that we did not layout, we need to find where they did go
    // and layout them accordingly so that animations can work as expected.
    // This case may happen if new views are added or an existing view expands and pushes
    // another view out of bounds.
    if(!state.willRunPredictiveAnimations() || getChildCount() == 0 || state.isPreLayout() || !supportsPredictiveItemAnimations()) {
        return;
    }
    // to make the logic simpler, we calculate the size of children and call fill.
    int scrapExtraStart = 0, scrapExtraEnd = 0;
    //注释1处，这个时候，recycler.getScrapList() 不为空。
    final List < RecyclerView.ViewHolder > scrapList = recycler.getScrapList();
    final int scrapSize = scrapList.size();
    final int firstChildPos = getPosition(getChildAt(0));
    for(int i = 0; i < scrapSize; i++) {
        RecyclerView.ViewHolder scrap = scrapList.get(i);
        if(scrap.isRemoved()) {
            continue;
        }
        final int position = scrap.getLayoutPosition();
        final int direction = position < firstChildPos != mShouldReverseLayout ? LayoutState.LAYOUT_START : LayoutState.LAYOUT_END;
        if(direction == LayoutState.LAYOUT_START) {
            scrapExtraStart += mOrientationHelper.getDecoratedMeasurement(scrap.itemView);
        } else {
            scrapExtraEnd += mOrientationHelper.getDecoratedMeasurement(scrap.itemView);
        }
    }

    if(DEBUG) {
        //对于没有用到的 scrap，决定向哪个方向添加
        Log.d(TAG, "for unused scrap, decided to add " + scrapExtraStart + " towards start and " + scrapExtraEnd + " towards end");
    }
    //赋值给 mLayoutState.mScrapList
    mLayoutState.mScrapList = scrapList;
    if(scrapExtraStart > 0) {
        View anchor = getChildClosestToStart();
        updateLayoutStateToFillStart(getPosition(anchor), startOffset);
        mLayoutState.mExtraFillSpace = scrapExtraStart;
        mLayoutState.mAvailable = 0;
        mLayoutState.assignPositionFromScrapList();
        fill(recycler, mLayoutState, state, false);
    }

    if(scrapExtraEnd > 0) {
        View anchor = getChildClosestToEnd();
        updateLayoutStateToFillEnd(getPosition(anchor), endOffset);
        mLayoutState.mExtraFillSpace = scrapExtraEnd;
        mLayoutState.mAvailable = 0;
        mLayoutState.assignPositionFromScrapList();
        fill(recycler, mLayoutState, state, false);
    }
    mLayoutState.mScrapList = null;
}
```

注释1处，这个时候，recycler.getScrapList() 不为空。

```java
@NonNull
public List<ViewHolder> getScrapList() {
    return mUnmodifiableAttachedScrap;
}

 private final List<ViewHolder>
mUnmodifiableAttachedScrap = Collections.unmodifiableList(mAttachedScrap);

```

内会还会再调用一次 fill 方法。 在 layoutChunk 方法中，会调用 addDisappearingView(view); 方法。 
把从 recycler.getScrapList() 取出来的对应的ViewHolder， 然后 ViewHolder 会从 mAttachedScrap 中移除。 对应的ItemView， attachViewToParent 到RecyclerView 中。 
注意，这个ViewHolder 已经在新的位置上了。 最后将 mLayoutState.mScrapList = null;


dispatchLayoutStep3 方法

新插入的 ViewHolder 执行 animateAppearance 方法。是一个透明度渐入动画。初始透明度设置为0。

```java
public boolean animateAdd(final RecyclerView.ViewHolder holder) {
    resetAnimation(holder);
    //新插入的ViewHolder，透明度设置为0
    holder.itemView.setAlpha(0);
    mPendingAdditions.add(holder);
    return true;
}
```

插入的 ViewHolder 之前的 ViewHolder 没有动画效果。
插入的 ViewHolder 之后的 ViewHolder 会执行 animateDisappearance 方法。是一个移动动画。


这个被挤出去的ViewHolder 会执行 animateDisappearance 方法。真正执行的是移动动画。

RecyclerView 的 animateDisappearance 方法。

```java
void animateDisappearance(@NonNull ViewHolder holder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
    //注释1处，在动画开始之前还会把这个 ItemView 通过调用 addAnimatingView  加入到 ChildHelper.hiddenViews 中。
    addAnimatingView(holder);
    holder.setIsRecyclable(false);
    //注释2处，DefaultItemAnimator.animateDisappearance 方法
    if(mItemAnimator.animateDisappearance(holder, preLayoutInfo, postLayoutInfo)) {
        postAnimationRunner();
    }
}
```

注释1处，在动画开始之前还会把这个 ItemView 通过调用 addAnimatingView  加入到 ChildHelper.hiddenViews 中。

注释2处，DefaultItemAnimator.animateDisappearance 方法

```java
@Override
public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
    int oldLeft = preLayoutInfo.left;
    //比如开始坐标是1200
    int oldTop = preLayoutInfo.top;
    View disappearingItemView = viewHolder.itemView;
    int newLeft = postLayoutInfo == null ? disappearingItemView.getLeft() : postLayoutInfo.left;
    //插入一条数据之后，插入View的高度是1200，那么这个被挤出去的 View新的位置是2400
    int newTop = postLayoutInfo == null ? disappearingItemView.getTop() : postLayoutInfo.top;
    if(!viewHolder.isRemoved() && (oldLeft != newLeft || oldTop != newTop)) {
        //把这个ItemView layout到新的位置
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

移动的动画，要移动的View(有多个，插入View之下的所有RecyclerView的子View)，注意： 在 dispatchLayoutStep2 的时候，已经把view 布局到正确的位置了


```java
public boolean animateMove(final RecyclerView.ViewHolder holder, int fromX, int fromY,
    int toX, int toY) {
    final View view = holder.itemView;
    fromX += (int) holder.itemView.getTranslationX();
    fromY += (int) holder.itemView.getTranslationY();
    resetAnimation(holder);
    int deltaX = toX - fromX;
    int deltaY = toY - fromY;
    if(deltaX == 0 && deltaY == 0) {
        dispatchMoveFinished(holder);
        return false;
    }
    if(deltaX != 0) {
        view.setTranslationX(-deltaX);
    }
    if(deltaY != 0) {
        //注释1处，先给这个View设置一个负的translationY
        view.setTranslationY(-deltaY);
    }
    mPendingMoves.add(new MoveInfo(holder, fromX, fromY, toX, toY));
    return true;
}
```

注释1处，先给这个View设置一个负的translationY = -1200 (回到老的位置上)。然后在动画执行过程中 会变化到 translationY = 0 (回到正确的位置上。)
```java
void animateMoveImpl(final RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
    final View view = holder.itemView;
    final int deltaX = toX - fromX;
    final int deltaY = toY - fromY;
    if(deltaX != 0) {
        view.animate().translationX(0);
    }
    if(deltaY != 0) {
        //注释1处，动画结束后，设置translationY = 0
        view.animate().translationY(0);
    }
    final ViewPropertyAnimator animation = view.animate();
    mMoveAnimations.add(holder);
    animation.setDuration(getMoveDuration()).setListener(new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animator) {
            animation.setListener(null);
            //动画结束后，会把这个屏幕外的View ChildHelper.hiddenViews 移除，也会从RecyclerView中移除。 RecyclerView.this.removeViewAt(index); 并把这个  ViewHolder 缓存到  mCachedViews。
            dispatchMoveFinished(holder);
            mMoveAnimations.remove(holder);
            dispatchFinishedWhenDone();
        }
    }).start();
}

```

动画结束后，会把这个屏幕外的View ChildHelper.hiddenViews 移除，也会从RecyclerView中移除。 RecyclerView.this.removeViewAt(index); 并把这个  ViewHolder 缓存到  mCachedViews。
注意：屏幕之外的ViewHolder会存储到 Recycler.mCachedViews 中。

添加的ViewHolder 执行的动画。

```java
void animateAddImpl(final RecyclerView.ViewHolder holder) {
    final View view = holder.itemView;
    final ViewPropertyAnimator animation = view.animate();
    mAddAnimations.add(holder);
    //透明度，从0变化到1。从不可见到可见。
    animation.alpha(1).setDuration(getAddDuration())
        .setListener(new AnimatorListenerAdapter() {
            //...            
        }).start();
}
```



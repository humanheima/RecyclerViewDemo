
源码版本：`androidx1.3.2`

分析场景：
RecyclerView使用线性布局，方向为竖直方向，布局从上到下，宽高都是 MATCH_PARENT。开始就2条数据。然后新插入的1条数据会插入到position=1的位置上。把原来的position=1的数据挤到屏幕之外。如下图所示：

![在这里插入图片描述](https://img-blog.csdnimg.cn/direct/0fd132f0a4b140908b4fc37947c39149.gif#pic_center)


### 流程图

![在这里插入图片描述](https://img-blog.csdnimg.cn/direct/a9b172b5456d4503b4fc33f92e3a8634.png#pic_center)


### 先说下结论

1. 插入1条数据。
2. 新插入的ViewHolder会布局到正确的位置上。
3.  position >= 插入位置的 ViewHolder 的 position都会偏移1。
4. 在 dispatchLayoutStep1 预布局阶段，记录ViewHolder的动画信息。
5. 在 dispatchLayoutStep2阶段，将ViewHolder布局到正确位置上。
6. 在 dispatchLayoutStep3阶段，执行动画，新插入的ViewHolder 执行 透明度渐入动画。position >= 插入位置的 ViewHolder 执行移动(translationY)动画，向下偏移。

###  示例代码

```kotlin
rv.layoutManager = LinearLayoutManager(this)
val arrayList = arrayListOf<CheckBoxModel>()
for (i in 0 until 2) {
    arrayList.add(CheckBoxModel("Hello$i", false))
}
rv.adapter = TestRvTheoryAdapter(this, arrayList)

binding.btnNotifyItemChanged.setOnClickListener {
    testNotifyItemInserted(arrayList)
}

private fun testNotifyItemInserted(arrayList: ArrayList<CheckBoxModel>) {
    arrayList.add(1, CheckBoxModel("插入进来的数据", true))
    rv.adapter?.notifyItemInserted(1)
}
```

调用 Adapter 的 notifyItemInserted 方法，内部会调用到RecyclerView.RecyclerViewDataObserver 的 onItemRangeInserted 方法。

```java
@Override
public void onItemRangeInserted(int positionStart, int itemCount) {
    assertNotInLayoutOrScroll(null);
    if(mAdapterHelper.onItemRangeInserted(positionStart, itemCount)) {
        //注释1处，会调用 requestLayout 方法。
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

RecyclerView.RecyclerViewDataObserver 的 onItemRangeInserted 方法注释1处，会调用 requestLayout 方法，触发 onMeasure 和 onLayout 方法。在我们的分析场景中， RecyclerView 宽高都是 MATCH_PARENT ,可以认为onMeasure 方法会直接 return。onLayout 方法内部会调用 dispatchLayout 方法。

RecyclerView 的 dispatchLayout 方法。
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

注释1处，调用dispatchLayoutStep1方法。

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

注释1处，对于LinearLayoutManager来说条件满足，调用 AdapterHelper 的 preProcess 方法。最终会调用到 AdapterHelper.Callback 的 offsetPositionsForAdd 方法。

```java
@Override
public void offsetPositionsForAdd(int positionStart, int itemCount) {
    //注释1处，调用 RecyclerView 的 offsetPositionRecordsForInsert 方法。
    offsetPositionRecordsForInsert(positionStart, itemCount);
    mItemsAddedOrRemoved = true;
}
```

注释1处，调用 RecyclerView 的 offsetPositionRecordsForInsert 方法。 偏移所有**position>=插入位置**的ViewHolder的位置。

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

注释1处，从插入位置开始，到最后一个ViewHolder的位置，老的ViewHolder position 都要加上 itemCount。比如我们在position=1 的位置插入1条数据，那么从position =1 开始的所有的ViewHolder的位置都要加1。改变的是ViewHolder的  `mPosition` 字段。

回到 dispatchLayoutStep1 方法的注释1处，记录动画之前的所有ViewHolder信息 animationInfo，保存到 mViewInfoStore 中。

dispatchLayoutStep1 方法，注释3处，预布局的时候调用 mLayout.onLayoutChildren(mRecycler, mState); 布局一次。在 mLayout.onLayoutChildren(mRecycler, mState); 方法内部逻辑：

1. 先回收ViewHolder 。detachAndScrapAttachedViews 的时候，View detachFromParent，ViewHolder 都回收到 mAttachedScrap 中。

2. fill 方法的时候，ViewHolder 会从 mAttachedScrap 中取出来复用的，不会重新 onBindViewHolder。重新 attachToParent 之后，会从  mAttachedScrap 中移除。

RecyclerView 的 dispatchLayout 方法注释2处，调用 dispatchLayoutStep2 方法内部逻辑：

1. 先回收ViewHolder 。detachAndScrapAttachedViews 的时候，View detachFromParent，ViewHolder 都回收到 mAttachedScrap 中。
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/direct/5a924bc408b24edeb3a89186c09cc541.png#pic_center)
   这时候要注意一下哟，上图中的第一个ViewHolder原本在position=1的位置，现在position变成 **2** 了。

2. fill 方法的时候，这里要注意一下：
   2.1.  position = 0 ，会从 mAttachedScrap 中取出来复用的，重新 attachToParent 之后会从  mAttachedScrap 中移除。
   2.2. position = 1 , 不会从 mAttachedScrap 中取出来复用的。会新创建 ViewHolder。为什么呢？

```java
ViewHolder getScrapOrHiddenOrCachedHolderForPosition(int position, boolean dryRun) {
            final int scrapCount = mAttachedScrap.size();

            // Try first for an exact, non-invalid match from scrap.
            for (int i = 0; i < scrapCount; i++) {
                final ViewHolder holder = mAttachedScrap.get(i);
                //注释1处，此时要找position=1 的 ViewHolder，但是holder.getLayoutPosition() = 2 ，对应上图中的ViewHolder。
                if (!holder.wasReturnedFromScrap() && holder.getLayoutPosition() == position
                        && !holder.isInvalid() && (mState.mInPreLayout || !holder.isRemoved())) {
                    holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                    return holder;
                }
            }
            //...
}
```

注释1处，此时要找 position=1 的 ViewHolder，但是holder.getLayoutPosition() = 2 ，对应上图中的ViewHolder。
`holder.getLayoutPosition() == position`，条件不满足。无法复用。此时其他缓存里面也没有可以复用的ViewHolder，所以会创建新的ViewHolder。新创建的ViewHolder 会 调用 onBindViewHolder 方法。对应的 View会添加到 RecyclerView  index =1 的位置上 `RecyclerView.this.addView(child, index);`。

注意：在我们的例子中，布局完  position=1 的 ViewHolder 以后，屏幕上没有剩余的布局空间了。fill 方法就结束了。但此时 mAttachedScrap 中，还有一个 ViewHolder 存在(`ViewHolder{dcce95f position=2 id=-1, oldPos=-1, pLpos:-1 scrap [attachedScrap] tmpDetached no parent}`)。

这个ViewHolder  对应的 View 已经 detachToParent 了，那么我们还需要再布局这个ViewHolder，将其对应的 View 重新 attachViewToParent ，实现从屏幕中移动到屏幕外的(translationY)动画。

这个逻辑是通过 layoutForPredictiveAnimations 方法来执行的。

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

    //赋值给 mLayoutState.mScrapList
    mLayoutState.mScrapList = scrapList;
    //...
    if(scrapExtraEnd > 0) {
        View anchor = getChildClosestToEnd();
        updateLayoutStateToFillEnd(getPosition(anchor), endOffset);
        mLayoutState.mExtraFillSpace = scrapExtraEnd;
        mLayoutState.mAvailable = 0;
        mLayoutState.assignPositionFromScrapList();
        //注释2处，再次调用 fill 方法
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

注释2处，再次调用 fill 方法。

```java
void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state,
            LayoutState layoutState, LayoutChunkResult result) {
        //注释0处，这时候，是从 layoutState.mScrapList 中 获取的 View
        View view = layoutState.next(recycler);
       
        if (layoutState.mScrapList == null) {
        } else {
            if (mShouldReverseLayout == (layoutState.mLayoutDirection
                    == LayoutState.LAYOUT_START)) {
                //注释1处，调用 addDisappearingView 方法。
                addDisappearingView(view);
            } else {
                addDisappearingView(view, 0);
            }
        }
        measureChildWithMargins(view, 0, 0);
        //...
        layoutDecoratedWithMargins(view, left, top, right, bottom);
        //...
        result.mFocusable = view.hasFocusable();
    }
```

在 layoutChunk 方法中：

注释0处，这时候，是从 layoutState.mScrapList  中 获取的 View。

注释1处，调用 addDisappearingView 方法。ViewHolder 会从 mAttachedScrap 中移除。 ViewHolder 对应的ItemView，会重新 attachViewToParent 到RecyclerView 中(`RecyclerView.this.attachViewToParent(child, index, layoutParams);`)。**注意，此时 index =2 呦。**

最后在layoutForPredictiveAnimations方法最后，将 mLayoutState.mScrapList 置为null。

**回到 dispatchLayout 方法注释3处**，调用dispatchLayoutStep3 方法。内部会执行动画。

插入的 ViewHolder 之前的 ViewHolder 没有动画效果。

新插入的 ViewHolder 调用 SimpleItemAnimator 的 animateAppearance 方法。是一个透明度渐入动画。初始透明度设置为0，渐变到1。默认是 DefaultItemAnimator 的 animateAdd 和 animateAddImpl 方法实现的。

```java
public boolean animateAdd(final RecyclerView.ViewHolder holder) {
    resetAnimation(holder);
    //初始透明度设置为0
    holder.itemView.setAlpha(0);
    mPendingAdditions.add(holder);
    return true;
}
void animateAddImpl(final RecyclerView.ViewHolder holder) {
    final View view = holder.itemView;
    final ViewPropertyAnimator animation = view.animate();
    mAddAnimations.add(holder);
    //渐变到透明度为1
    animation.alpha(1).setDuration(getAddDuration()).setListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animator) {
            dispatchAddStarting(holder);
        }
        @Override
        public void onAnimationCancel(Animator animator) {
            view.setAlpha(1);
        }
        @Override
        public void onAnimationEnd(Animator animator) {
            animation.setListener(null);
            dispatchAddFinished(holder);
            mAddAnimations.remove(holder);
            dispatchFinishedWhenDone();
        }
    }).start();
}
```


插入的 ViewHolder 之后的 ViewHolder 会调用 RecyclerView 的 animateDisappearance 方法。

RecyclerView 的 animateDisappearance 方法。

```java
void animateDisappearance(@NonNull ViewHolder holder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
    //注释1处，在动画开始之前还会把这个 ItemView 通过调用 addAnimatingView  加入到 ChildHelper.hiddenViews 中。
    addAnimatingView(holder);
    holder.setIsRecyclable(false);
    //注释2处，调用 SimpleItemAnimator 的 animateDisappearance 方法
    if(mItemAnimator.animateDisappearance(holder, preLayoutInfo, postLayoutInfo)) {
        postAnimationRunner();
    }
}
```

注释1处，在动画开始之前还会把这个 ItemView 通过调用 addAnimatingView  加入到 ChildHelper.hiddenViews 中。

注释2处，调用 SimpleItemAnimator 的 animateDisappearance 方法

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
        //把这个ItemView layout到新的位置，这里有必要吗？
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

注释1处，移动动画。DefaultItemAnimator 的 animateMove方法 ，animateMoveImpl 方法

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


void animateMoveImpl(final RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
    final View view = holder.itemView;
    final int deltaX = toX - fromX;
    final int deltaY = toY - fromY;
    if(deltaX != 0) {
        view.animate().translationX(0);
    }
    if(deltaY != 0) {
        //注释2处，设置translationY = 0，移动到 translationY = 0 的位置
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

注释1处，先给这个View设置一个负的translationY = -1200 (在动画开始回到老的位置上，在我们的例子中就是postion = 1 )。注释2处，设置translationY = 0，移动到 translationY = 0 的位置。

然后在动画执行过程中 translationY从-1200变化到 translationY = 0 (回到正确的位置上，在我们的例子中就是 position = 2)。这样就实现从屏幕中移动到屏幕外的动画效果了。

动画结束后，会把这个屏幕外的View 从 **ChildHelper.hiddenViews** 移除，也会从RecyclerView中移除。 **RecyclerView.this.removeViewAt(index);** 并把这个  ViewHolder 缓存到  **mCachedViews** 中。

**注意：屏幕之外的ViewHolder会存储到 Recycler.mCachedViews 中。**


参考链接：

* [RecyclerView notifyDataSetChanged 之后的源码分析](https://blog.csdn.net/leilifengxingmw/article/details/136818773)

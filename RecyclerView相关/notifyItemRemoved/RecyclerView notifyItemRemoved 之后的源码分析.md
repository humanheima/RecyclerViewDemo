
源码版本：`androidx1.3.2`


为什么预布局的时候也要 dispatchLayoutStep1 也要 mLayout.onLayoutChildren(mRecycler, mState) ？ 感觉这篇文章说的应该是对的。

我们就以RecyclerView最简单的使用方式为例进行分析，使用线性布局，方向为竖直方向，布局从上到下。开始有3条数据。然后移除 position = 1 的数据。示例代码如下所示：

```kotlin
rv.layoutManager = LinearLayoutManager(this)
val arrayList = arrayListOf <CheckBoxModel> ()
for(i in 0 until 3) {
    arrayList.add(CheckBoxModel("Hello$i", false))
}
rv.adapter = TestRvTheoryAdapter(this, arrayList)
binding.btnNotifyItemChanged.setOnClickListener {
    testNotifyItemRemoved(arrayList)
}

private fun testNotifyItemRemoved(arrayList: ArrayList < CheckBoxModel > ) {
    arrayList.removeAt(1)
    rv.adapter?.notifyItemRemoved(1)
}
```

当我们调用Adapter的 notifyItemRemoved 方法的时候，会调用RecyclerView的 requestLayout 方法，然后会调用RecyclerView的 onLayout 方法，然后会调用RecyclerView的 dispatchLayout 方法。


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
    //注释1处，调用processAdapterUpdatesAndSetAnimationFlags方法。处理动画标记位
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
            final ItemHolderInfo animationInfo = mItemAnimator
                .recordPreLayoutInformation(mState, holder,
                    ItemAnimator.buildAdapterChangeFlagsForAnimations(holder),
                    holder.getUnmodifiedPayloads());
            //注释2处，保存ViewHolder的动画信息。
            mViewInfoStore.addToPreLayout(holder, animationInfo);
            //...
        }
    }
    if(mState.mRunPredictiveAnimations) {
        // Save old positions so that LayoutManager can run its mapping logic.
        //保存ViewHolder的位置信息
        saveOldPositions();
        final boolean didStructureChange = mState.mStructureChanged;
        mState.mStructureChanged = false;
        //注释3处，布局子View
        mLayout.onLayoutChildren(mRecycler, mState);
        mState.mStructureChanged = didStructureChange;

        for(int i = 0; i < mChildHelper.getChildCount(); ++i) {
            final View child = mChildHelper.getChildAt(i);
            final ViewHolder viewHolder = getChildViewHolderInt(child);
            if(viewHolder.shouldIgnore()) {
                continue;
            }
            //注释4处，新创建的ViewHolder，满足条件，记录新创建的ViewHolder的动画信息。
            if(!mViewInfoStore.isInPreLayout(viewHolder)) {
                int flags = ItemAnimator.buildAdapterChangeFlagsForAnimations(viewHolder);
                boolean wasHidden = viewHolder
                    .hasAnyOfTheFlags(ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST);
                if(!wasHidden) {
                    flags |= ItemAnimator.FLAG_APPEARED_IN_PRE_LAYOUT;
                }
                final ItemHolderInfo animationInfo = mItemAnimator.recordPreLayoutInformation(
                    mState, viewHolder, flags, viewHolder.getUnmodifiedPayloads());
                if(wasHidden) {
                    recordAnimationInfoIfBouncedHiddenView(viewHolder, animationInfo);
                } else {
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
注释1处，调用processAdapterUpdatesAndSetAnimationFlags方法。处理动画标记位。


```java
private void processAdapterUpdatesAndSetAnimationFlags() {
    if(mDataSetHasChangedAfterLayout) {
        // Processing these items have no value since data set changed unexpectedly.
        // Instead, we just reset it.
        mAdapterHelper.reset();
        if(mDispatchItemsChangedEvent) {
            mLayout.onItemsChanged(this);
        }
    }
    // simple animations are a subset of advanced animations (which will cause a
    // pre-layout step)
    // If layout supports predictive animations, pre-process to decide if we want to run them
    if(predictiveItemAnimationsEnabled()) {
        mAdapterHelper.preProcess();
    } else {
        mAdapterHelper.consumeUpdatesInOnePass();
    }
    boolean animationTypeSupported = mItemsAddedOrRemoved || mItemsChanged;
    mState.mRunSimpleAnimations = mFirstLayoutComplete && mItemAnimator != null && (mDataSetHasChangedAfterLayout || animationTypeSupported || mLayout.mRequestedSimpleAnimations) && (!mDataSetHasChangedAfterLayout || mAdapter.hasStableIds());
    mState.mRunPredictiveAnimations = mState.mRunSimpleAnimations && animationTypeSupported && !mDataSetHasChangedAfterLayout && predictiveItemAnimationsEnabled();
}
```

Evaluate ViewHolder1

```java
ViewHolder1: ViewHolder{40b6a27 position=0 id=-1, oldPos=1, pLpos:1 removed}

```

这里我们说一下这个方法的逻辑：

* 给要被移出的 ViewHolder 添加标记位  `ViewHolder.FLAG_REMOVED` 。
* 保存旧的位置。 `oldPos=1, pLpos:1` 3. 保存新的位置 `position=0`。

回到 dispatchLayoutStep1 方法注释2处，保存ViewHolder的动画信息。

注释3处，调用 LayoutManager 的 onLayoutChildren 方法布局子View。

```java
@Override
public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    //...
    //注释1处，detachAndScrapAttachedViews
    detachAndScrapAttachedViews(recycler);
    //...
    mLayoutState.mNoRecycleSpace = 0;
    if (mAnchorInfo.mLayoutFromEnd) {//正常情况为该条件不满足。我们分析else的情况。
        //...
    } else {
        updateLayoutStateToFillEnd(mAnchorInfo);
        mLayoutState.mExtraFillSpace = extraForEnd;
        //注释2处，从锚点开始向end方向填充
        fill(recycler, mLayoutState, state, false);
        //...
    }
    //...
}
```

注释1处，detachAndScrapAttachedViews。所有的子View 会被 detachFromParent ，缓存在 mAttachedScrap 中。

注释2处，从锚点开始向end方向填充。

```java
int fill(RecyclerView.Recycler recycler, LayoutState layoutState,
        RecyclerView.State state, boolean stopOnFocusable) {
    //记录开始填充的时候，可用的空间
    final int start = layoutState.mAvailable;
    //...
    // 剩余的空间
    int remainingSpace = layoutState.mAvailable + layoutState.mExtraFillSpace;
    LayoutChunkResult layoutChunkResult = mLayoutChunkResult;
    //循环填充子View，只要还有剩余空间并且还有数据
    while ((layoutState.mInfinite || remainingSpace > 0) && layoutState.hasMore(state)) {
        //这里会把 layoutChunkResult.mIgnoreConsumed 重置为 false
        layoutChunkResult.resetInternal();
        //获取并添加子View，然后测量、布局子View并将分割线考虑在内。
        layoutChunk(recycler, state, layoutState, layoutChunkResult);
        //如果没有更多View了，布局结束，跳出循环
        if (layoutChunkResult.mFinished) {
            break;
        }
        //增加偏移量，加上已经填充的像素
        layoutState.mOffset += layoutChunkResult.mConsumed * layoutState.mLayoutDirection;
        //注释1处，注意这里的逻辑，如果填充的View是被移除的，就不减去remainingSpace。
        //注释1处，注意这里的逻辑，如果填充的View是被移除的，就不减去remainingSpace。
        //注释1处，注意这里的逻辑，如果填充的View是被移除的，就不减去remainingSpace。
        if (!layoutChunkResult.mIgnoreConsumed || layoutState.mScrapList != null
                || !state.isPreLayout()) {
            //可用空间减去已经填充的像素        
            layoutState.mAvailable -= layoutChunkResult.mConsumed;
            // we keep a separate remaining space because mAvailable is important for recycling
            //剩余空间，减去已经填充的像素
            remainingSpace -= layoutChunkResult.mConsumed;
        }
    }
    // 返回已经填充的空间，比如开始可用空间 start 是1920，填充完毕，可用空间 layoutState.mAvailable 是120，就返回 1800 。填充了1800像素。
    //返回结果有可能大于start，因为最后一个填充的View有一部分在屏幕外面。
    return start - layoutState.mAvailable;
}
```


注释1处，注意这里的逻辑，如果填充的View是被移除的，就不减去remainingSpace。 

ViewHolder1 标记为 removed，在 layoutChunk 中不会减去 remainingSpace。

```java
//layoutChunk方法部分逻辑
if (params.isItemRemoved() || params.isItemChanged()) {
    result.mIgnoreConsumed = true;
}
```

所以，fill 方法会继续布局。这个时候 position = 2，会创建一个新的ViewHolder，onBindViewHolder 然后返回。
```java
//新创建的ViewHolder，
// 我们可以看到一些信息，在预布局的时候，pLpos:2，真正的位置 position=1
// 说明在后期需要执行一个动画 从 position=2 的位置，移动到 position=1 的位置。
ViewHolder{2390807 position=1 id=-1, oldPos=-1, pLpos:2 no parent}
```

回到 dispatchLayoutStep1 方法，注释4处，新创建的ViewHolder，满足条件，记录新创建的ViewHolder的动画信息。

**dispatchLayoutStep1 结束，总结一下：**

* 在预布局阶段，有一个新创建的 ViewHolder2，这个时候 RecyclerView 是有3个子 View 的。
* 记录新创建的 ViewHolder2 的动画信息。
* 现在有3个Item。有标记位为 removed 的 ViewHolder1，是被移除的。

然后，进入 dispatchLayoutStep2 方法，内部再次调用 `mLayout.onLayoutChildren(mRecycler, mState);`。

回收ViewHolder的时候，还是会都放进 Recycler.mAttachedScrap 中。这个时候，缓存了3个ViewHolder。

fill 的时候，获取ViewHolder，调用 Recycler 的 getScrapOrHiddenOrCachedHolderForPosition 方法。

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

现在 Recycler.mAttachedScrap 中有3个 ViewHolder。

注意：这个时候，ViewHolder1的 `mPosition = 0` && `holder.isRemoved() = 0`，不会被复用的。

position = 0 的时候，会从 mAttachedScrap 中取  ViewHolder0 出来复用的。 `holder.getLayoutPosition() == position` = 0
position = 1 的时候，会从 mAttachedScrap 中取  ViewHolder2 出来复用的。 `holder.getLayoutPosition() == position` = 1

然后这个时候，fill 没有剩余空间 `remainingSpace = 0` ，就不会再继续布局了。这个时候RecyclerView中就只有2个子View。
这个时候 Recycler.mAttachedScrap 还是有一个ViewHolder的，就是被移除的那个。

dispatchLayoutStep2 结束。

* dispatchLayoutStep3 阶段。

被移除的 ViewHolder1 会执行 animateDisappearance 方法。是一个透明度渐出动画，透明度从1变化到0。
最后是在 DefaultItemAnimator 的 animateRemoveImpl 方法中执行的。

被移除的 ViewHolder 会在第二布局阶段 detachViewFromParent以后，在 fill 方法中，不会重新 attachViewToParent。
在透明度渐出动画的开始之前的时候，会调用 addAnimatingView， 会重新 attachViewToParent 上的。
然后动画结束之后，会把这个ViewHolder 从 RecyclerView 中移除。并且会把这个 ViewHolder 缓存到 RecycledViewPool(有 ViewHolder.FLAG_REMOVED 是不会被缓存到 Recycler.mCacheViews 中的)。



新创建的 ViewHolder2 现在已经在 position = 1 的位置上了。会执行 translationY 动画。 
最后是在 DefaultItemAnimator 的 animateMoveImpl 方法中执行的。

先把 translationY 设置为1200，然后让它移动到  translationY 为0 的位置，就会向上移动1200像素。实现从屏幕下方进入屏幕的效果。


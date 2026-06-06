
源码版本：`androidx1.3.2`

分析场景：

RecyclerView使用线性布局，方向为竖直方向，布局从上到下，宽高都是 MATCH_PARENT。开始有3条数据。然后移除 `position = 1` 的数据。

![在这里插入图片描述](https://img-blog.csdnimg.cn/direct/7cea5e31928c43df8bdc51cfab466ce0.gif#pic_center)

流程图
![在这里插入图片描述](https://img-blog.csdnimg.cn/direct/b592560274fe4168b5010c1f4a9599e0.png#pic_center)


**先说下结论：**

在 dispatchLayoutStep1 预布局阶段：

* 给要被移出的 ViewHolder1 添加标记位  `ViewHolder.FLAG_REMOVED` 。
  ViewHolder1 标记为 removed，在 fill 方法中不会减去 remainingSpace。所以，fill 方法会继续布局。这个时候 position = 2，会创建一个新的ViewHolder，onBindViewHolder 然后返回。ViewHolder对应的 ItemView 会添加到RecyclerView。                `RecyclerView.this.addView(child, index);`

* 现在有3个ViewHolder，RecyclerView 有3个子 View。

在 dispatchLayoutStep2 真正的布局阶段：

* 在 detachAndScrapAttachedViews 回收 ViewHolder 的时候，Recycler.mAttachedScrap 回收了3个ViewHolder。
* ViewHolder0 被布局到 position = 0 的位置。
* ViewHolder2 被布局到 position = 1 的位置。
* 缓存Recycler.mAttachedScrap 中还有一个 ViewHolder1，就是被移除的。

在 dispatchLayoutStep3 动画阶段：

* 没有变化的ViewHolder0，没有动画效果。
* 新创建的ViewHolder2 会执行一个移动动画，从屏幕底部进入到屏幕中。
* 被移除的ViewHolder1 会执行一个透明度渐出动画，透明度从1变化到0。在动画开始之前，会重新把ViewHolder 对应的 ItemView 重新 attachViewToParent 到 RecyclerView 上 。在动画结束后，会把 这个 ItemView 真正移除，对应的ViewHolder 缓存到 RecycledViewPool(有 ViewHolder.FLAG_REMOVED 是不会被缓存到 Recycler.mCacheViews 中的)。

**示例代码如下所示：**

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

当我们调用Adapter的 notifyItemRemoved 方法的时候，会调用RecyclerView的 requestLayout 方法，然后会调用RecyclerView的 onLayout 方法，然后会调用 RecyclerView 的 dispatchLayout 方法。


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

RecyclerView 的  dispatchLayoutStep1 方法

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
    //...
    if(predictiveItemAnimationsEnabled()) {
        //注释1处
        mAdapterHelper.preProcess();
    } else {
        mAdapterHelper.consumeUpdatesInOnePass();
    }
    boolean animationTypeSupported = mItemsAddedOrRemoved || mItemsChanged;
    
    // mState.mRunSimpleAnimations = true
    mState.mRunSimpleAnimations = mFirstLayoutComplete && mItemAnimator != null && (mDataSetHasChangedAfterLayout || animationTypeSupported || mLayout.mRequestedSimpleAnimations) && (!mDataSetHasChangedAfterLayout || mAdapter.hasStableIds());
    
    // mState.mRunPredictiveAnimations = true
    mState.mRunPredictiveAnimations = mState.mRunSimpleAnimations && animationTypeSupported && !mDataSetHasChangedAfterLayout && predictiveItemAnimationsEnabled();
}
```

这里我们说一下这个方法的做的一些事情，就不一步一步跟了：

首先会改变 position =1 位置上的 ViewHolder，我我们看一下改变之后的 ViewHolder1 的信息。Evaluate ViewHolder1：

```java
ViewHolder1: ViewHolder{40b6a27 position=0 id=-1, oldPos=1, pLpos:1 removed}

```

* 给要被移出的 ViewHolder1 添加标记位  `ViewHolder.FLAG_REMOVED` 。
* 保存旧的位置。 `oldPos=1, pLpos:1` ，保存新的位置 `position=0`。

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

注释1处，detachAndScrapAttachedViews。所有的子View 会被 detachFromParent ，缓存在 Recycler.mAttachedScrap 中。

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


**注释1处，注意这里的逻辑，如果填充的View是被移除的，就不减去remainingSpace。**因为在layoutChunk 方法中，将        `result.mIgnoreConsumed` 置为true了。

layoutChunk方法部分逻辑

```java
//layoutChunk方法部分逻辑
if (params.isItemRemoved() || params.isItemChanged()) {
    result.mIgnoreConsumed = true;
}
```

ViewHolder1 标记为 removed，在 fill 方法中不会减去 remainingSpace。所以，fill 方法会继续布局。这个时候 position = 2，会创建一个新的ViewHolder，onBindViewHolder 然后返回。ViewHolder对应的 ItemView 会添加到RecyclerView。                `RecyclerView.this.addView(child, index);`


```java
//新创建的ViewHolder，
// 我们可以看到一些信息，在预布局的时候，pLpos:2，真正的位置 position=1
// 说明在后期需要执行一个动画 从 position=2 的位置，移动到 position=1 的位置。
ViewHolder{2390807 position=1 id=-1, oldPos=-1, pLpos:2 no parent}
```

回到 dispatchLayoutStep1 方法，注释4处，新创建的ViewHolder，满足条件，记录新创建的ViewHolder的动画信息。

**dispatchLayoutStep1 结束，总结一下：**

* 在预布局阶段，有一个新创建的 ViewHolder2，对应的 ItemView 会添加到RecyclerView。                `RecyclerView.this.addView(child, index);`。这个时候 RecyclerView 是有3个子 View 的。
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

**dispatchLayoutStep3 阶段**

没有变化的ViewHolder，没有动画效果。

被移除的ViewHolder 的动画。是一个透明度渐出动画，透明度从1变化到0。

**首先看添加移除动画的逻辑。**

RecyclerView 的 animateDisappearance 方法。

```java
void animateDisappearance(@NonNull ViewHolder holder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
    //注释1处，调用addAnimatingView 方法
    addAnimatingView(holder);
    holder.setIsRecyclable(false);
    //注释2处，调用 SimpleItemAnimator 的 animateDisappearance 方法。添加消失动画。
    if(mItemAnimator.animateDisappearance(holder, preLayoutInfo, postLayoutInfo)) {
        postAnimationRunner();
    }
}
```

注释1处，调用addAnimatingView 方法。被移除的 ViewHolder 在 dispatchLayoutStep2 阶段 detachViewFromParent以后，在 fill 方法中，不会重新 attachViewToParent。这里在移除动画的开始之前，会调用 addAnimatingView， 把ViewHolder 对应的 ItemView 重新 attachViewToParent 到 RecyclerView 上 **RecyclerView.this.attachViewToParent(child, index, layoutParams);** 。**注意，这里的index是1哟。**

然后动画结束之后，会把这个ViewHolder 从 RecyclerView 中移除。并且会把这个 ViewHolder 缓存到 RecycledViewPool(有 ViewHolder.FLAG_REMOVED 是不会被缓存到 Recycler.mCacheViews 中的)。

注释2处，调用 SimpleItemAnimator 的 animateDisappearance 方法。添加消失动画。

```java
@Override
public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
    int oldLeft = preLayoutInfo.left;
    int oldTop = preLayoutInfo.top;
    View disappearingItemView = viewHolder.itemView;
    int newLeft = postLayoutInfo == null ? disappearingItemView.getLeft() : postLayoutInfo.left;
    int newTop = postLayoutInfo == null ? disappearingItemView.getTop() : postLayoutInfo.top;
    if(!viewHolder.isRemoved() && (oldLeft != newLeft || oldTop != newTop)) {
        
        disappearingItemView.layout(newLeft, newTop,
            newLeft + disappearingItemView.getWidth(),
            newTop + disappearingItemView.getHeight());
        //注释1处，不是被移出的ViewHolder才会执行 animateMove
        return animateMove(viewHolder, oldLeft, oldTop, newLeft, newTop);
    } else {
        //注释2处，被移出的ViewHolder才会执行 animateRemove
        return animateRemove(viewHolder);
    }
}
```

注释2处，被移出的ViewHolder才会执行 animateRemove。


DefaultItemAnimator 的 animateRemove 方法。

```java
public boolean animateRemove(final RecyclerView.ViewHolder holder) {
    resetAnimation(holder);
    mPendingRemovals.add(holder);
    return true;
}
```

这个方法，就是向 mPendingRemovals 添加了一个等待执行的移除动画，返回true。

**新创建的ViewHolder会执行一个 移动动画**，从屏幕底部进入到屏幕中。

**新创建的 ViewHolder2 现在已经在 position = 1 的位置上了**。为了实现从屏幕外移动到屏幕中的 translationY 动画。 在动画开始之初，给 ViewHolder2 对应的 ItemView 设置 **translationY >0** 。在我们的例子中就是一个ItemView的高度，例如1200px。

RecyclerView 的 animateAppearance 方法。

```java
void animateAppearance(@NonNull ViewHolder itemHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
    itemHolder.setIsRecyclable(false);
    //注释1处，调用 SimpleItemAnimator 的 animateAppearance 方法。
    if(mItemAnimator.animateAppearance(itemHolder, preLayoutInfo, postLayoutInfo)) {
        postAnimationRunner();
    }
}
```

注释1处，调用 SimpleItemAnimator 的 animateAppearance 方法。

```java
@Override
public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
    if(preLayoutInfo != null && (preLayoutInfo.left != postLayoutInfo.left || preLayoutInfo.top != postLayoutInfo.top)) {
        //注释1处，调用 DefaultItemAnimator 的 animateAppearance 方法。
        return animateMove(viewHolder, preLayoutInfo.left, preLayoutInfo.top,
            postLayoutInfo.left, postLayoutInfo.top);
    } else {
        return animateAdd(viewHolder);
    }
}
```
注释1处，调用 DefaultItemAnimator 的 animateAppearance 方法。

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
    //注释1处，给View设置 translationY >0 
    if(deltaY != 0) {
        view.setTranslationY(-deltaY);
    }
    mPendingMoves.add(new MoveInfo(holder, fromX, fromY, toX, toY));
    return true;
}
```

注释1处，给View设置 translationY >0 。注意，这里 deltaY小于0，所以 **-deltaY >0 **，在我们的例子中是1200。

然后在动画过程中，从 translationY = 1200 移动到 translationY 为0 的位置，就会向上移动1200像素。实现从屏幕下方进入屏幕的效果。最后是在 DefaultItemAnimator 的 animateMoveImpl 方法中执行的。

现在动画添加完毕，**看看动画的执行过程。**

```java
@Override
public void runPendingAnimations() {
    //移除动画
    boolean removalsPending = !mPendingRemovals.isEmpty();
    boolean movesPending = !mPendingMoves.isEmpty();
    boolean changesPending = !mPendingChanges.isEmpty();
    boolean additionsPending = !mPendingAdditions.isEmpty();
    
    // First, remove stuff
    for(RecyclerView.ViewHolder holder: mPendingRemovals) {
        //注释1处，执行移除动画
        animateRemoveImpl(holder);
    }
    mPendingRemovals.clear();
    // 注释2处，执行移动动画
    if(movesPending) {
        final ArrayList < MoveInfo > moves = new ArrayList < > ();
        moves.addAll(mPendingMoves);
        mMovesList.add(moves);
        mPendingMoves.clear();
        Runnable mover = new Runnable() {
        
            @Override
            public void run() {
                for(MoveInfo moveInfo: moves) {
                    animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY,
                        moveInfo.toX, moveInfo.toY);
                }
                moves.clear();
                mMovesList.remove(moves);
            }
        };
        if(removalsPending) {
            View view = moves.get(0).holder.itemView;
            ViewCompat.postOnAnimationDelayed(view, mover, getRemoveDuration());
        } else {
            mover.run();
        }
    }
    // Next, change stuff, to run in parallel with move animations
    if(changesPending) {
        final ArrayList < ChangeInfo > changes = new ArrayList < > ();
        changes.addAll(mPendingChanges);
        mChangesList.add(changes);
        mPendingChanges.clear();
        Runnable changer = new Runnable() {@
            Override
            public void run() {
                for(ChangeInfo change: changes) {
                    animateChangeImpl(change);
                }
                changes.clear();
                mChangesList.remove(changes);
            }
        };
        if(removalsPending) {
            RecyclerView.ViewHolder holder = changes.get(0).oldHolder;
            ViewCompat.postOnAnimationDelayed(holder.itemView, changer, getRemoveDuration());
        } else {
            changer.run();
        }
    }
    //...
}
```

注释1处，执行移除动画

```java
private void animateRemoveImpl(final RecyclerView.ViewHolder holder) {
        final View view = holder.itemView;
        final ViewPropertyAnimator animation = view.animate();
        mRemoveAnimations.add(holder);
        //注释1处，这里透明度变化到0，变为不可见。
        animation.setDuration(getRemoveDuration()).alpha(0).setListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        dispatchRemoveStarting(holder);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        animation.setListener(null);
                        view.setAlpha(1);
                        //注释2处，动画结束
                        dispatchRemoveFinished(holder);
                        mRemoveAnimations.remove(holder);
                        dispatchFinishedWhenDone();
                    }
                }).start();
    }
```

注释1处，移除动画，这里透明度变化到0，变为不可见。

注释2处，动画结束。会把这个ViewHolder2 从 RecyclerView 中移除。并且会把这个 ViewHolder 缓存到 RecycledViewPool(有 ViewHolder.FLAG_REMOVED 是不会被缓存到 Recycler.mCacheViews 中的)。

回到 runPendingAnimations 方法的注释2处，执行移动动画。


```java
void animateMoveImpl(final RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
    final View view = holder.itemView;
    final int deltaX = toX - fromX;
    final int deltaY = toY - fromY;
    if(deltaX != 0) {
        view.animate().translationX(0);
    }
    if(deltaY != 0) {
        //注释1处，移动动画的结束的时候的translationY设置为0，回到原来的位置上。
        view.animate().translationY(0);
    }
    // TODO: make EndActions end listeners instead, since end actions aren't called when
    // vpas are canceled (and can't end them. why?)
    // need listener functionality in VPACompat for this. Ick.
    final ViewPropertyAnimator animation = view.animate();
    mMoveAnimations.add(holder);
    animation.setDuration(getMoveDuration()).setListener(new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animator) {
            animation.setListener(null);
            dispatchMoveFinished(holder);
            mMoveAnimations.remove(holder);
            dispatchFinishedWhenDone();
        }
    }).start();
}
```

注释1处，移动动画的结束的时候的translationY设置为0，回到原来的位置上。然后执行动画。移动动画结束后在本例中没有做额外的操作。






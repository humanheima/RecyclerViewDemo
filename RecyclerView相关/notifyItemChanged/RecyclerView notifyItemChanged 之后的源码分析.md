

### 注意：本文是基于 androidx.RecyclerView 1.3.2 版本的源码分析。默认使用 DefaultItemAnimator，如果使用了其他的 ItemAnimator，可能会有不同的表现。

**效果图：**

![在这里插入图片描述](https://img-blog.csdnimg.cn/direct/7e9fabf5e0324f5a992652ea9e2d277e.gif#pic_center)

##  示例代码如下：

```java
binding.btnNotifyItemChanged.setOnClickListener {
    //对position=1的item调用 notifyItemChanged
    rv.adapter?.notifyItemChanged(1)
}
```

## 在开始之前提两个问题：


1.RecyclerView 怎么 去掉 notifyItemChanged 时候的闪一下的动画？

2.如上代码所示，position=1的位置上，在 notifyItemChanged 之前和之后，使用的同一个ViewHolder 吗？


adapter 的 notifyItemChanged 方法会调用

```java
public final void notifyItemChanged(int position) {
    mObservable.notifyItemRangeChanged(position, 1);
}
```


RecyclerView.AdapterDataObservable 的 notifyItemRangeChanged 方法

```java
public void notifyItemRangeChanged(int positionStart, int itemCount) {
    notifyItemRangeChanged(positionStart, itemCount, null);
}
```


```java
public void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
    // since onItemRangeChanged() is implemented by the app, it could do anything, including
    // removing itself from {@link mObservers} - and that could cause problems if
    // an iterator is used on the ArrayList {@link mObservers}.
    // to avoid such problems, just march thru the list in the reverse order.
    for(int i = mObservers.size() - 1; i >= 0; i--) {
        //这里从后向前遍历，避免在遍历过程中，可能把observer删掉，可能会导致IndexoutException 的问题
        mObservers.get(i).onItemRangeChanged(positionStart, itemCount, payload);
    }
}
```

RecyclerView.RecyclerViewDataObserver 的 onItemRangeChanged 方法

```java
@Override
public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
    assertNotInLayoutOrScroll(null);
    //调用 AdapterHelper 的 onItemRangeChanged 方法。 如果条件满足，触发更新
    if(mAdapterHelper.onItemRangeChanged(positionStart, itemCount, payload)) {
        triggerUpdateProcessor();
    }
}
```

AdapterHelper 的 onItemRangeChanged 方法

```java
boolean onItemRangeChanged(int positionStart, int itemCount, Object payload) {
    if(itemCount < 1) {
        return false;
    }
    //AdapterHelper 添加一个延迟更新
    mPendingUpdates.add(obtainUpdateOp(UpdateOp.UPDATE, positionStart, itemCount, payload));
    mExistingUpdateTypes |= UpdateOp.UPDATE;
    //返回true
    return mPendingUpdates.size() == 1;
}
```


RecyclerView.RecyclerViewDataObserver 的 triggerUpdateProcessor 方法

```java
void triggerUpdateProcessor() {
    if(POST_UPDATES_ON_ANIMATION && mHasFixedSize && mIsAttached) {
        ViewCompat.postOnAnimation(RecyclerView.this, mUpdateChildViewsRunnable);
    } else {
        //注释1处
        mAdapterUpdateDuringMeasure = true;
        requestLayout();
    }
}
```

注释1处，调用 RecyclerView 的 requestLayout 方法。该方法导致 onMeasure 方法 、onLayout 方法被调用。在我们的例子中， RecyclerView 的 宽高是 match_parent，onMeasure 方法没有再去做测量，我们可以忽略。

RecyclerView 的 onLayout 方法内部主要调用了 dispatchLayout 方法。精简逻辑后的 dispatchLayout 方法如下：

```java
/**
 * 该方法可以看做是layoutChildren()方法的一个包装，处理由于布局造成的动画改变。动画的工作机制基于有5中不同类型的动画的假设：
 * PERSISTENT: 在布局前后，items一直可见。
 * REMOVED: 在布局之前items可见，在布局之后，items被移除。
 * ADDED: 在布局之前items不存在，items是被添加到RecyclerView的。
 * DISAPPEARING: 在布局前后items存在于数据集中，但是在布局过程中可见性由可见变为不可见。（这些items是由于其他变化的副作用而被移动到屏幕之外了）
 * APPEARING: 在布局前后items存在于数据集中，但是在布局过程中可见性由不可见变为可见。（这些items是由于其他变化的副作用而被移动到屏幕之中了）
 *
 * 方法的大体逻辑就是计算每个item在布局前后是否存在，并推断出它们处于上述五种状态的哪一种，然后设置不同的动画。
 * PERSISTENT类型的Views 通过 ItemAnimator 的 animatePersistence(ViewHolder, ItemHolderInfo, ItemHolderInfo) 方法执行动画
 * DISAPPEARING类型的Views 通过 ItemAnimator 的 animateDisappearance(ViewHolder, ItemHolderInfo, ItemHolderInfo) 方法执行动画
 * APPEARING类型的Views 通过 ItemAnimator 的 animateAppearance(ViewHolder, ItemHolderInfo, ItemHolderInfo) 方法执行动画
 * REMOVED和ADDED类型 （notifyItemChange 的时候，其实是把老的 itemView 移除了，然后新添加了一个itemView。这个过程就是REMOVED和ADDED类型 ）的Views 通过 ItemAnimator 的 animateChange(ViewHolder, ViewHolder, ItemHolderInfo, ItemHolderInfo) 执行动画。
 */
void dispatchLayout() {
    //...
    mState.mIsMeasuring = false;
    if(mState.mLayoutStep == State.STEP_START) {
        //调用dispatchLayoutStep1方法
        dispatchLayoutStep1();
        mLayout.setExactMeasureSpecsFrom(this);
        //调用dispatchLayoutStep2方法
        dispatchLayoutStep2();
    } else if(mAdapterHelper.hasUpdates() || mLayout.getWidth() != getWidth()
        //...
    } else {
        //...
    }
    //调用dispatchLayoutStep3方法。
    dispatchLayoutStep3();
}
```

## 预布局阶段 dispatchLayoutStep1

```java
/**
 * layout的第一步，在这个步骤会执行以下操作；
 * - 处理适配器更新
 * - 决定要运行哪种动画
 * - 保存当前views的信息
 * - 如果必要的话，运行预布局（layout）并保存相应的信息
 */
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


注释0处，处理动画标志位，RecyclerView 的 processAdapterUpdatesAndSetAnimationFlags 方法


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
        //注释1处，条件满足，调用AdapterHelper的preProcess方法
        mAdapterHelper.preProcess();
    } else {
        mAdapterHelper.consumeUpdatesInOnePass();
    }
    //这个时候，mItemsChanged 为true
    boolean animationTypeSupported = mItemsAddedOrRemoved || mItemsChanged;
    // mState.mRunSimpleAnimations = true
    mState.mRunSimpleAnimations = mFirstLayoutComplete && mItemAnimator != null && (mDataSetHasChangedAfterLayout || animationTypeSupported || mLayout.mRequestedSimpleAnimations) && (!mDataSetHasChangedAfterLayout || mAdapter.hasStableIds());
    // mState.mRunPredictiveAnimations = true
    mState.mRunPredictiveAnimations = mState.mRunSimpleAnimations && animationTypeSupported && !mDataSetHasChangedAfterLayout && predictiveItemAnimationsEnabled();
}
```

注释1处，条件满足，调用AdapterHelper的preProcess方法，内部会调用 RecyclerView 的  viewRangeUpdate 方法， 给 holder 添加了一个标志位 `ViewHolder.FLAG_UPDATE`，表示需要更新。并将 RecyclerView 的 mItemsChanged 设置为true。然后 AdapterHelper 清除 mPendingUpdates。

```java
holder.addFlags(ViewHolder.FLAG_UPDATE);
```

回到 dispatchLayoutStep1 方法注释1处，记录动画之前的所有ViewHolder信息 animationInfo，保存到 mViewInfoStore 中。**mViewInfoStore.addToPreLayout(holder, animationInfo);**。


dispatchLayoutStep1 方法注释2处，注意这里只会添加变化的viewHolder 到 oldChangeHolders 中。`mViewInfoStore.addToOldChangeHolders(key, holder);`。

注释3处，预布局的时候调用 `mLayout.onLayoutChildren(mRecycler, mState);` 进行布局

在这个过程中：

1. 先执行一次 detachAndScrapAttachedViews 方法，会把所有的 itemView detachViewFromParent，同时回收itemView对应的 ViewHolder。没有变化的 ViewHolder 添加到 **Recycler.mAttachedScrap** 中，变化的（notifyItemChanged 的 item）ViewHolder 添加到 **Recycler.mChangedScrap** 中。


2. 然后会执行 fill 方法，会把所有的 itemView attachViewToParent（mChildHelper.attachViewToParent(child, index, child.getLayoutParams(), false);）。没有变化的 ViewHolder 是从 mAttachedScrap 中取出来复用的，变化的 ViewHolder 是从 mChangedScrap 中取出来复用的。 然后把取出来的 ViewHolder 从  mAttachedScrap  或者 mChangedScrap 移除。

## 疑问：为啥要在预布局的时候，做 mLayout.onLayoutChildren(mRecycler, mState); 这个操作呢？

看下来，感觉对 onItemChanged，onItemInserted 来说应该没有用。对 onItemRemoved 有用。在预布局的时候，layout 把屏幕之外的 ViewHolder。这样在后面动画的时候才能执行，屏幕外的 ViewHolder 进入屏幕的动画效果。可以看一看这篇文章的分析。[RecyclerView notifyItemRemoved 之后的源码分析](https://blog.csdn.net/leilifengxingmw/article/details/136983490)

## 布局第二阶段 dispatchLayoutStep2

```java
/**
 * The second layout step where we do the actual layout of the views for the final state.
 * This step might be run multiple times if necessary (e.g. measure).
 */
private void dispatchLayoutStep2() {
    startInterceptRequestLayout();
    onEnterLayoutOrScroll();
    mState.assertLayoutStep(State.STEP_LAYOUT | State.STEP_ANIMATIONS);
    mAdapterHelper.consumeUpdatesInOnePass();
    mState.mItemCount = mAdapter.getItemCount();
    mState.mDeletedInvisibleItemCountSincePreviousLayout = 0;
    if(mPendingSavedState != null && mAdapter.canRestoreState()) {
        if(mPendingSavedState.mLayoutState != null) {
            mLayout.onRestoreInstanceState(mPendingSavedState.mLayoutState);
        }
        mPendingSavedState = null;
    }
    // Step 2: Run layout
    //注释1处，将 mState.mInPreLayout 设置为 false    
    mState.mInPreLayout = false;
    //注释2处，再次布局，调用 mLayout.onLayoutChildren(mRecycler, mState);
    mLayout.onLayoutChildren(mRecycler, mState);

    mState.mStructureChanged = false;

    // onLayoutChildren may have caused client code to disable item animations; re-check
    mState.mRunSimpleAnimations = mState.mRunSimpleAnimations && mItemAnimator != null;
    mState.mLayoutStep = State.STEP_ANIMATIONS;
    onExitLayoutOrScroll();
    stopInterceptRequestLayout(false);
}
```

注释1处，将 **mState.mInPreLayout** 设置为 false 。标记不是处于预布局阶段了。

注释2处，再次布局，调用 `mLayout.onLayoutChildren(mRecycler, mState);` 布局， 在这个过程中：

1. 会执行一次 detachAndScrapAttachedViews 方法，会把所有的 itemView detachViewFromParent，同时回收itemView对应的 ViewHolder。没有变化的 ViewHolder 添加到 mAttachedScrap 中，变化的（notifyItemChanged 的 item）ViewHolder 添加到 mChangedScrap 中。

2. 然后又会执行 fill 方法，会把所有没有变化的 itemView 重新  attachViewToParent（mChildHelper.attachViewToParent(child, index, child.getLayoutParams(), false);）。没有变化的 ViewHolder 是从 mAttachedScrap 中取出来复用的。

注意：这个时候，不是预布局阶段了，是不会从 mChangedScrap 中 查找ViewHolder 来复用的。 在我们这里例子中，是创建了一个新的 ViewHolder，然后 调用 bindViewHolder 方法。然后返回新创建的ViewHolder。

另外，这个时候，新创建的 ViewHolder 对应的View是添加到 RecyclerView 中的 mChildHelper.addView(child, index, false);。 这个时候老的View还没有移除，只是 detachViewFromParent 了。 mChangedScrap 也还存在改变了的老的ViewHolder。

## 最后的布局阶段 dispatchLayoutStep3


```java
private void dispatchLayoutStep3() {
    mState.assertLayoutStep(State.STEP_ANIMATIONS);
    startInterceptRequestLayout();
    onEnterLayoutOrScroll();
    mState.mLayoutStep = State.STEP_START;
    if(mState.mRunSimpleAnimations) {
        for(int i = mChildHelper.getChildCount() - 1; i >= 0; i--) {
            ViewHolder holder = getChildViewHolderInt(mChildHelper.getChildAt(i));
            if(holder.shouldIgnore()) {
                continue;
            }
            long key = getChangedHolderKey(holder);
            //注释1处，记录动画之后的所有ViewHolder信息 animationInfo，保存到 mViewInfoStore 中
            final ItemHolderInfo animationInfo = mItemAnimator
                .recordPostLayoutInformation(mState, holder);
            //注释2处，之前变化的ViewHolder的动画信息。
            ViewHolder oldChangeViewHolder = mViewInfoStore.getFromOldChangeHolders(key);
            if(oldChangeViewHolder != null && !oldChangeViewHolder.shouldIgnore()) {
                final boolean oldDisappearing = mViewInfoStore.isDisappearing(
                    oldChangeViewHolder);
                final boolean newDisappearing = mViewInfoStore.isDisappearing(holder);
                if(oldDisappearing && oldChangeViewHolder == holder) {
                    // 保存到 mViewInfoStore 中
                    mViewInfoStore.addToPostLayout(holder, animationInfo);
                } else {
                    //变化的ViewHolder，之前的动画信息
                    final ItemHolderInfo preInfo = mViewInfoStore.popFromPreLayout(
                        oldChangeViewHolder);
                    //注释3处，变化的ViewHolder之后的动画信息添加到 mViewInfoStore 中
                    mViewInfoStore.addToPostLayout(holder, animationInfo);
                    //注释4处，再从 mViewInfoStore 获取 变化之后的 ViewHolder 信息
                    ItemHolderInfo postInfo = mViewInfoStore.popFromPostLayout(holder);
                    if(preInfo == null) {
                        handleMissingPreInfoForChangeError(key, holder, oldChangeViewHolder);
                    } else {
                        //注释5处，处理新老ViewHolder的动画
                        animateChange(oldChangeViewHolder, holder, preInfo, postInfo,
                            oldDisappearing, newDisappearing);
                    }
                }
            } else {
                //其他没有变化的ViewHolder动画信息保存到 mViewInfoStore 中
                mViewInfoStore.addToPostLayout(holder, animationInfo);
            }
        }
        // Step 4: Process view info lists and trigger animations
        mViewInfoStore.process(mViewInfoProcessCallback);
    }

    mLayout.removeAndRecycleScrapInt(mRecycler);
    //...
}
```

注释1处，记录动画之后的所有ViewHolder信息 animationInfo，保存到 mViewInfoStore 中。

注释2处，之前变化的ViewHolder的动画信息。

注释3处，变化的ViewHolder之后的动画信息添加到 mViewInfoStore 中。

注释5处，处理新老ViewHolder的动画。

```java
private void animateChange(@NonNull ViewHolder oldHolder, @NonNull ViewHolder newHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo,
    boolean oldHolderDisappearing, boolean newHolderDisappearing) {
    oldHolder.setIsRecyclable(false);
    if(oldHolderDisappearing) {
        addAnimatingView(oldHolder);
    }
    //注释1处，不同的的ViewHolder，会有动画效果
    if(oldHolder != newHolder) {
        if(newHolderDisappearing) {
            addAnimatingView(newHolder);
        }
        oldHolder.mShadowedHolder = newHolder;
        // 注释2处，动画开始前，这里会把 mChangedScrap 中存储的ViewHolder 从 mChangedScrap 中移除，然后重新把对应的View  attachViewToParent ，并添加到 ChildHelper.mHiddenViews 中
        addAnimatingView(oldHolder);
        //注释3处，这里虽然还会再调用一遍，但是 mChangedScrap 中已经没有了，mAttachedScrap 也没有了。调用没有作用
        mRecycler.unscrapView(oldHolder);
        newHolder.setIsRecyclable(false);
        newHolder.mShadowingHolder = oldHolder;
    }
    //注释4处，调用 DefaultItemAnimator 的 animateChange 方法，在父类 SimpleItemAnimator 中实现
    if(mItemAnimator.animateChange(oldHolder, newHolder, preInfo, postInfo)) {
        //条件满足，调用postAnimationRunner方法，执行 mItemAnimatorRunner 的run 方法
        postAnimationRunner();
    }
}
```


注释1处，新旧不同的ViewHolder，会有动画效果。

注释2处，**动画开始前**，这里会把 mChangedScrap 中存储的ViewHolder 从 mChangedScrap 中移除，然后重新把对应的View attach 到 RecyclerView, `mChildHelper.attachViewToParent(child, index, child.getLayoutParams(), false);` ，并添加到 ChildHelper.mHiddenViews 中。

注释4处，调用 DefaultItemAnimator 的 animateChange 方法，在父类 SimpleItemAnimator 中实现。


### SimpleItemAnimator 的 animateChange 方法

```java
@Override
public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull RecyclerView.ViewHolder newHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
    //获取老的左上角坐标
    final int fromLeft = preLayoutInfo.left;
    final int fromTop = preLayoutInfo.top;
    final int toLeft, toTop;
    if(newHolder.shouldIgnore()) {
        toLeft = preLayoutInfo.left;
        toTop = preLayoutInfo.top;
    } else {
        //获取新的左上角坐标
        toLeft = postLayoutInfo.left;
        toTop = postLayoutInfo.top;
    }
    // 调用 DefaultItemAnimator 的 animateChange 方法
    return animateChange(oldHolder, newHolder, fromLeft, fromTop, toLeft, toTop);
}
```


```java
@Override
public boolean animateChange(RecyclerView.ViewHolder oldHolder,
    RecyclerView.ViewHolder newHolder, int fromLeft, int fromTop, int toLeft, int toTop) {
    if(oldHolder == newHolder) {
        //注释1处，不支持动画的时候，新老的ViewHolder是一样的，animateMove 也没有改变 translationX 或者 translationY 直接返回false
        // Don't know how to run change animations when the same view holder is re-used.
        // run a move animation to handle position changes.
        return animateMove(oldHolder, fromLeft, fromTop, toLeft, toTop);
    }
    final float prevTranslationX = oldHolder.itemView.getTranslationX();
    final float prevTranslationY = oldHolder.itemView.getTranslationY();
    //老的ViewHolder透明度是1
    final float prevAlpha = oldHolder.itemView.getAlpha();
    resetAnimation(oldHolder);
    int deltaX = (int)(toLeft - fromLeft - prevTranslationX);
    int deltaY = (int)(toTop - fromTop - prevTranslationY);
    // recover prev translation state after ending animation
    oldHolder.itemView.setTranslationX(prevTranslationX);
    oldHolder.itemView.setTranslationY(prevTranslationY);
    //注释1处，将老的 ViewHolder alpha 设置为 1
    oldHolder.itemView.setAlpha(prevAlpha);
    if(newHolder != null) {
        // carry over translation values
        resetAnimation(newHolder);
        newHolder.itemView.setTranslationX(-deltaX);
        newHolder.itemView.setTranslationY(-deltaY);
        //translationX 和 translationY 都没有发生变化
        //注释2处，新ViewHolder 将 itemView 动画将透明度设置为0，所以新的ViewHolder 是从不可见到可见的，透明度从0到1
        newHolder.itemView.setAlpha(0);
    }
    //添加一个变化信息
    mPendingChanges.add(new ChangeInfo(oldHolder, newHolder, fromLeft, fromTop, toLeft, toTop));
    return true;
}
```



注释1处，将老的 ViewHolder itemView 透明度 alpha 设置为 1。

注释2处，新ViewHolder 将 itemView 动画将透明度设置为0，所以新的ViewHolder 是从不可见到可见的，透明度从0到1。

animateChange方法返回 true的时候，会post一个 mItemAnimatorRunner 来执行动画。

RecyclerView 的 mItemAnimatorRunner 变量。

```java

private Runnable mItemAnimatorRunner = new Runnable() {@
    Override
    public void run() {
        if(mItemAnimator != null) {
            mItemAnimator.runPendingAnimations();
        }
        mPostedAnimatorRunner = false;
    }
};
```

DefaultItemAnimator 的 runPendingAnimations 方法

```java
@Override
public void runPendingAnimations() {
    boolean removalsPending = !mPendingRemovals.isEmpty();
    boolean movesPending = !mPendingMoves.isEmpty();
    boolean changesPending = !mPendingChanges.isEmpty();
    boolean additionsPending = !mPendingAdditions.isEmpty();
    if(!removalsPending && !movesPending && !additionsPending && !changesPending) {
        // nothing to animate
        return;
    }
    // First, remove stuff
    // Next, move stuff
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
                    //注释1处，执行change动画
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
    // Next, add stuff
}
```



注释1处，执行change动画，DefaultItemAnimator 的  void animateChangeImpl(final ChangeInfo changeInfo) 方法

```java
void animateChangeImpl(final ChangeInfo changeInfo) {
    final RecyclerView.ViewHolder holder = changeInfo.oldHolder;
    //老的View
    final View view = holder == null ? null : holder.itemView;
    final RecyclerView.ViewHolder newHolder = changeInfo.newHolder;
    final View newView = newHolder != null ? newHolder.itemView : null;
    if(view != null) {
        //老的View执行动画
        final ViewPropertyAnimator oldViewAnim = view.animate().setDuration(
            getChangeDuration());
        mChangeAnimations.add(changeInfo.oldHolder);
        oldViewAnim.translationX(changeInfo.toX - changeInfo.fromX);
        oldViewAnim.translationY(changeInfo.toY - changeInfo.fromY);
        //注释1处，老的 View 透明度开始是1，现在要变化到0，就是从可见到不可见
        oldViewAnim.alpha(0).setListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animator) {
                dispatchChangeStarting(changeInfo.oldHolder, true);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                oldViewAnim.setListener(null);
                //动画结束的时候，又把老的View设置为可见了，那得有地方把老的View移除吧。
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setTranslationY(0);
                //注释3处，老动画结束之后，会调用到 RecyclerView.ItemAnimatorRestoreListener的 onAnimationFinished 方法，内部会把 oldView 从RecyclerView中移除
                dispatchChangeFinished(changeInfo.oldHolder, true);
                mChangeAnimations.remove(changeInfo.oldHolder);
                dispatchFinishedWhenDone();
            }
        }).start();
    }
    if(newView != null) {
        final ViewPropertyAnimator newViewAnimation = newView.animate();
        mChangeAnimations.add(changeInfo.newHolder);
        //注释2处，新的View开始设置的透明度是0，现在要变化到1，就是从不可见到可见
        newViewAnimation.translationX(0).translationY(0).setDuration(getChangeDuration())
            .alpha(1).setListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animator) {
                    dispatchChangeStarting(changeInfo.newHolder, false);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    newViewAnimation.setListener(null);
                    newView.setAlpha(1);
                    newView.setTranslationX(0);
                    newView.setTranslationY(0);
                    //注释4处，动画结束，是哪里把View添加上了呢？到这里执行动画的时候，View已经添加上了，是在什么时候把View添加上的呢？
                    dispatchChangeFinished(changeInfo.newHolder, false);
                    mChangeAnimations.remove(changeInfo.newHolder);
                    dispatchFinishedWhenDone();
                }
            }).start();
    }
}
```




注释1处，老的 View 透明度开始是1，现在要变化到0，就是从可见到不可见。

注释2处，新的View开始设置的透明度是0，现在要变化到1，就是从不可见到可见。

！！！ 这个过程，就是交叉淡入淡出的动画效果。

注释3处，老动画结束之后，会调用到 RecyclerView 会把 oldView 从RecyclerView 中移除。并把老的 ViewHolder 回收到 RecycledViewPool 中。

```java
private class ItemAnimatorRestoreListener implements ItemAnimator.ItemAnimatorListener {

        ItemAnimatorRestoreListener() {}

        @Override
        public void onAnimationFinished(ViewHolder item) {
            item.setIsRecyclable(true);
            if (item.mShadowedHolder != null && item.mShadowingHolder == null) { // old vh
                item.mShadowedHolder = null;
            }
            // always null this because an OldViewHolder can never become NewViewHolder w/o being
            // recycled.
            item.mShadowingHolder = null;
            if (!item.shouldBeKeptAsChild()) {
                //注释1处，这里会把 oldView 从RecyclerView中移除，能从 mBucket 中获取到老的ViewHolder的数据
                if (!removeAnimatingView(item.itemView) && item.isTmpDetached()) {
                    removeDetachedView(item.itemView, false);
                }
            }
        }
    }

```



注释1处，这里会把 oldView 从RecyclerView中移除，能从 mBucket 中获取到老的ViewHolder的数据。对于新的ViewHolder，无法从mBucket中获取到数据，所以在动画结束之后，不会把新的ViewHolder从RecyclerView中移除。

RecyclerView 的 removeAnimatingView 方法

```java
boolean removeAnimatingView(View view) {
    startInterceptRequestLayout();
    //注释1处，会从 mHiddenViews 把老的 View 删除。也会从RecyclerView 移除。`RecyclerView.this.removeViewAt(index);`。
    final boolean removed = mChildHelper.removeViewIfHidden(view);
    if(removed) {
        final ViewHolder viewHolder = getChildViewHolderInt(view);
        //注释2处，这里再次调用，但，mChangedScrap 中已经没有了
        mRecycler.unscrapView(viewHolder);
        //注释3处，
        mRecycler.recycleViewHolderInternal(viewHolder);
        if(sVerboseLoggingEnabled) {
            Log.d(TAG, "after removing animated view: " + view + ", " + this);
        }
    }
    // only clear request eaten flag if we removed the view.
    stopInterceptRequestLayout(!removed);
    return removed;
}
```

注释1处，会从 mHiddenViews 把老的 View 删除。也会从RecyclerView 移除。`RecyclerView.this.removeViewAt(index);`。

注释2处，这里注意一下，这里再次调用 unscrapView。但 mChangedScrap 中已经没有了老的ViewHolder了。

注释3处，会把 老的 ViewHolder ，根据 ItemViewType，回收到 RecycledViewPool 中，一种 ItemViewType 默认缓存5个。最后也会从 mViewInfoStore 删除老的 ViewHolder 信息。

**为什么不回收到 mCachedViews呢？**

因为 有标志位，`ViewHolder.FLAG_UPDATE` 会被设置，所以不会回收到 mCachedViews 中。

animateChangeImpl 方法注释4处，动画结束，是哪里把View添加上了呢？到这里执行动画的时候，View已经添加上了，是在什么时候把View添加上的呢？在 fill 的时候，就添加上了。

### 小结：

在布局时候，mViewInfoStore 用来保存布局开始到结束的ViewHolder的信息。

1. 预布局 dispatchLayoutStep1 的时候，记录一次所有 ViewHolder 的信息  RecyclerView.ItemAnimator.ItemHolderInfo preInfo 。
2. 布局的最后阶段 dispatchLayoutStep3 的时候，记录一次所有 ViewHolder 的信息 RecyclerView.ItemAnimator.ItemHolderInfo postInfo 。
3. 对比两个阶段的信息，对比同一个位置上前后ViewHolder 的信息，来决定是否执行动画，以及执行什么动画。


其他的点


上面问题解答


1.RecyclerView 怎么 去掉 notifyItemChanged 时候的闪一下的动画？

有三种方法。

1.设置 `ItemAnimator` 为 `null`
2.如果是使用默认的SimpleItemAnimator，可以通过设置`mRecyclerView.getItemAnimator().setChangeDuration(0);`来去掉`notifyItemChanged`的动画效果。
3.设置默认的 SimpleItemAnimator不支持动画。

```java
val animator = rv.itemAnimator
if (animator is SimpleItemAnimator) {
    animator.supportsChangeAnimations = false
    //animator.changeDuration = 0
}
```

2.如上代码所示，position=1的位置上，在 notifyItemChanged 之前和之后，使用的同一个ViewHolder 吗？

1.支持动画的时候（验证的是上面的第三种方法），无论数据变没变，都是新的ViewHolder。老的 ViewHolder 是放在 mChangedScrap 中的。但是在 dispatchLayoutStep2 方法中，不会从 mChangedScrap 找，所以会创建新的 ViewHolder。
2.不支持动画的时候，无论数据变没变，是同一个ViewHolder，都是从 mAttachedScrap 中取出来的，会重新绑定数据。



参考链接

* [Android RecyclerView内部机制](https://www.jianshu.com/p/5284d6066a38)













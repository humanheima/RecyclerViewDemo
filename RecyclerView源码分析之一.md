
写在前面，看RecyclerView源码有点"老虎吃天，无从下口"的感觉。多次提笔想写关于RecyclerView的源码相关文章，最终也是作罢。最大的原因还是感觉RecyclerView的源码太过复杂，怕自己不能胜任。
也是走马观花的看了一些网上的博客文章，有的文章看了也不止一遍。自己也就照虎画猫，来记录一下阅读源码的过程。

我们就以RecyclerView最简单的使用方式为例进行分析。

```java
LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
recyclerView.setLayoutManager(linearLayoutManager);
recyclerView.setAdapter(...);
```

使用RecyclerView的两部曲。`setLayoutManager()`和`setAdapter`。

RecyclerView的setLayoutManager方法。

```java
public void setLayoutManager(@Nullable LayoutManager layout) {
    if (layout == mLayout) {
        return;
    }
    //停止滚动
    stopScroll();
    
    //改变LayoutManager依然会重用View。
    if (mLayout != null) {
        //...
    } else {
        //清除缓存
        mRecycler.clear();
    }
    // this is just a defensive measure for faulty item animators.
    mChildHelper.removeAllViewsUnfiltered();
    mLayout = layout;
    if (layout != null) {
        //...
        //LayoutManager关联RecyclerView
        mLayout.setRecyclerView(this);
        if (mIsAttached) {
            mLayout.dispatchAttachedToWindow(this);
        }
    }
    mRecycler.updateViewCacheSize();
    //注释1处，请求measure、layout、draw。
    requestLayout();
}
```

注释1处，调用requestLayout方法请求measure、layout、draw。

RecyclerView的setAdapter方法。

```java
public void setAdapter(@Nullable Adapter adapter) {
    // bail out if layout is frozen
    setLayoutFrozen(false);
    //注释1处
    setAdapterInternal(adapter, false, true);
    processDataSetCompletelyChanged(false);
    //注释2处，请求measure、layout、draw。
    requestLayout();
}
```

注释1处，调用setAdapterInternal方法。

```java
private void setAdapterInternal(@Nullable Adapter adapter, boolean compatibleWithPrevious,
            boolean removeAndRecycleViews) {
    if (mAdapter != null) {
        mAdapter.unregisterAdapterDataObserver(mObserver);
        mAdapter.onDetachedFromRecyclerView(this);
    }
    if (!compatibleWithPrevious || removeAndRecycleViews) {
        removeAndRecycleViews();
    }
    mAdapterHelper.reset();
    final Adapter oldAdapter = mAdapter;
    mAdapter = adapter;
    if (adapter != null) {
        //注册观察适配器，当适配器数据发生变化的时候，重新requestLayout
        adapter.registerAdapterDataObserver(mObserver);
        adapter.onAttachedToRecyclerView(this);
    }
    if (mLayout != null) {
        mLayout.onAdapterChanged(oldAdapter, mAdapter);
    }
    mRecycler.onAdapterChanged(oldAdapter, mAdapter, compatibleWithPrevious);
    mState.mStructureChanged = true;
}
```

RecyclerView的`setLayoutManager()`和`setAdapter`方法内部都会调用requestLayout方法，请求measure、layout、draw。

RecyclerView的onMeasure方法。

```java
@Override
protected void onMeasure(int widthSpec, int heightSpec) {
    if (mLayout == null) {
        //注释1处
        defaultOnMeasure(widthSpec, heightSpec);
        return;
    }
    //注释2处，是否开启自动测量，我们以LinearLayoutManager来分析，LinearLayoutManager默认是true。
    if (mLayout.isAutoMeasureEnabled()) {
        final int widthMode = MeasureSpec.getMode(widthSpec);
        final int heightMode = MeasureSpec.getMode(heightSpec);

        //LayoutManager的onMeasure方法默认还是调用了RecyclerView的defaultOnMeasure方法
        mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec);
        //宽高的测量模式都是EXACTLY或者mAdapter为null直接return，就是使用defaultOnMeasure的测量结果
        final boolean measureSpecModeIsExactly =
                widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY;
        if (measureSpecModeIsExactly || mAdapter == null) {
            return;
        }
        //注释3处，当前布局阶段，默认是State.STEP_START
        if (mState.mLayoutStep == State.STEP_START) {
            //注释4处，
            dispatchLayoutStep1();
        }
        //为LayoutManager设置测量模式
        mLayout.setMeasureSpecs(widthSpec, heightSpec);
        //将RecyclerView的状态置为正在测量
        mState.mIsMeasuring = true;
        //注释5处，第二步布局
        dispatchLayoutStep2();

        //在dispatchLayoutStep2之后，重新设置LayoutManager的宽高信息
        mLayout.setMeasuredDimensionFromChildren(widthSpec, heightSpec);

        // 如果RecyclerView没有准确的宽高信息，或者RecyclerView至少有一个子View没有确定的宽高则重新测量。
        if (mLayout.shouldMeasureTwice()) {
            mLayout.setMeasureSpecs(
                    MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
            mState.mIsMeasuring = true;
            dispatchLayoutStep2();
            // now we can get the width and height from the children.
            mLayout.setMeasuredDimensionFromChildren(widthSpec, heightSpec);
        }
    } else {//自动测量为false，这种场景我们就不看了，一刀切是狠，哈哈。
        //...
    }
}
```

注释1处，如果没有设置LayoutManager，就调用defaultOnMeasure，然后直接return。

```java
void defaultOnMeasure(int widthSpec, int heightSpec) {
    // calling LayoutManager here is not pretty but that API is already public and it is better
    // than creating another method since this is internal.
    final int width = LayoutManager.chooseSize(widthSpec,
            getPaddingLeft() + getPaddingRight(),
            ViewCompat.getMinimumWidth(this));
    final int height = LayoutManager.chooseSize(heightSpec,
            getPaddingTop() + getPaddingBottom(),
            ViewCompat.getMinimumHeight(this));

    setMeasuredDimension(width, height);
}
```

就是根据测量模式获取RecyclerView的宽高信息，然后保存一下。

注释2处，是否开启自动测量，我们以LinearLayoutManager来分析，LinearLayoutManager默认是true。

注释3处，当前布局阶段，默认是State.STEP_START，条件满足，会调用注释4处，dispatchLayoutStep1方法。

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
    //开始拦截requestLayout的请求，避免多次响应requestLayout的调用，造成多次布局
    startInterceptRequestLayout();
    //mViewInfoStore中存储的是要执行动画的Views的相关信息，这里清除
    mViewInfoStore.clear();
    onEnterLayoutOrScroll();
    //注释1处，处理适配器更新和设置动画的标志位。
    processAdapterUpdatesAndSetAnimationFlags();
    saveFocusInfo();
    mState.mTrackOldChangeHolders = mState.mRunSimpleAnimations && mItemsChanged;
    mItemsAddedOrRemoved = mItemsChanged = false;
    mState.mInPreLayout = mState.mRunPredictiveAnimations;
    //这里保存了适配器中数据的数量
    mState.mItemCount = mAdapter.getItemCount();
    findMinMaxChildLayoutPositions(mMinMaxLayoutPositions);

    if (mState.mRunSimpleAnimations) {
        //第一次条件不满足...
    }
    if (mState.mRunPredictiveAnimations) {
        //第一次条件不满足
    } else {
        clearOldPositions();
    }
    onExitLayoutOrScroll();
    //停止拦截requestLayout的请求，
    stopInterceptRequestLayout(false);
    //将当前布局步骤赋值为State.STEP_LAYOUT
    mState.mLayoutStep = State.STEP_LAYOUT;
}
```

注释1处，处理适配器更新和设置动画的标志位。

```java
private void processAdapterUpdatesAndSetAnimationFlags() {
    //第一次设置适配器的时候mDataSetHasChangedAfterLayout为true，mDispatchItemsChangedEvent为false
    if (mDataSetHasChangedAfterLayout) {
        // Processing these items have no value since data set changed unexpectedly.
        // Instead, we just reset it.
        mAdapterHelper.reset();
        if (mDispatchItemsChangedEvent) {
            mLayout.onItemsChanged(this);
        }
    }
    // simple animations are a subset of advanced animations (which will cause a
    // pre-layout step)
    // If layout supports predictive animations, pre-process to decide if we want to run them
    //注释1处，对于LinearLayoutManager来说条件满足，调用预处理方法，但是此时mPendingUpdates是empty的，没有什么可处理的
    if (predictiveItemAnimationsEnabled()) {
        mAdapterHelper.preProcess();
    } else {
        mAdapterHelper.consumeUpdatesInOnePass();
    }
    //第一次设置适配器，animationTypeSupported为false
    boolean animationTypeSupported = mItemsAddedOrRemoved || mItemsChanged;
    //第一次设置适配器mState.mRunSimpleAnimations为false
    mState.mRunSimpleAnimations = mFirstLayoutComplete
            && mItemAnimator != null
            && (mDataSetHasChangedAfterLayout
            || animationTypeSupported
            || mLayout.mRequestedSimpleAnimations)
            && (!mDataSetHasChangedAfterLayout
            || mAdapter.hasStableIds());
    //第一次设置适配器mState.mRunPredictiveAnimations为false
    mState.mRunPredictiveAnimations = mState.mRunSimpleAnimations
            && animationTypeSupported
            && !mDataSetHasChangedAfterLayout
            && predictiveItemAnimationsEnabled();
}
```

第一次调用dispatchLayoutStep1的时候，此时RecyclerView还没有子View所以不会有什么动画执行。方法最后将`mState.mLayoutStep`置为了`State.STEP_LAYOUT`。


RecyclerView的onMeasure方法的注释5处，调用dispatchLayoutStep2方法进行第二步布局。

```java
/**
 * layout的第二步，在这里我们为最终状态执行View的真正的布局操作。如果必要的话，这个步骤可能会执行多次。
 */
private void dispatchLayoutStep2() {
    startInterceptRequestLayout();
    onEnterLayoutOrScroll();
    mState.assertLayoutStep(State.STEP_LAYOUT | State.STEP_ANIMATIONS);
    //消耗所有的延迟更新，这里以后再看
    mAdapterHelper.consumeUpdatesInOnePass();
    //获取适配器中数据的数量
    mState.mItemCount = mAdapter.getItemCount();
    mState.mDeletedInvisibleItemCountSincePreviousLayout = 0;

    // Step 2: Run layout
    mState.mInPreLayout = false;
    //注释1处，布局子View
    mLayout.onLayoutChildren(mRecycler, mState);

    mState.mStructureChanged = false;
    mPendingSavedState = null;

    //为false
    mState.mRunSimpleAnimations = mState.mRunSimpleAnimations && mItemAnimator != null;
    //这里将状态置为了State.STEP_ANIMATIONS
    mState.mLayoutStep = State.STEP_ANIMATIONS;
    onExitLayoutOrScroll();
    stopInterceptRequestLayout(false);
}
```

注释1处，调用LayoutManager的onLayoutChildren方法。我们直接看LinearLayoutManager的onLayoutChildren方法。

```java
@Override
public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    // layout的算法：
    // 1) 通过检查children和其他变量，找到一个锚点坐标和锚点的位置（我认为应该是在adapter中数据对应的位置）。 
    // 2) 从锚点向上填充RecyclerView。
    // 3) 从锚点向下填充RecyclerView。
    // 4) 滚动RecyclerView，做一些显示上的调整。
    if (mPendingSavedState != null || mPendingScrollPosition != RecyclerView.NO_POSITION) {
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }
    }
    if (mPendingSavedState != null && mPendingSavedState.hasValidAnchor()) {
        mPendingScrollPosition = mPendingSavedState.mAnchorPosition;
    }
    //如果mLayoutState为null的话，则创建。
    ensureLayoutState();
    mLayoutState.mRecycle = false;
    //决定布局顺序，是否要倒着布局。LinearLayoutManager默认是从上到下布局。
    resolveShouldLayoutReverse();

    final View focused = getFocusedChild();
    //注释1处，找到锚点，为了简单，我们只看注释2处查找锚点的逻辑。
    if (!mAnchorInfo.mValid || mPendingScrollPosition != RecyclerView.NO_POSITION
            || mPendingSavedState != null) {
        mAnchorInfo.reset();
        //默认是false
        mAnchorInfo.mLayoutFromEnd = mShouldReverseLayout ^ mStackFromEnd;
        //注释2处，计算锚点位置和坐标
        updateAnchorInfoForLayout(recycler, state, mAnchorInfo);
        mAnchorInfo.mValid = true;
    } else if (focused != null && (mOrientationHelper.getDecoratedStart(focused)
                    >= mOrientationHelper.getEndAfterPadding()
            || mOrientationHelper.getDecoratedEnd(focused)
            <= mOrientationHelper.getStartAfterPadding())) {
        //以获取焦点的子View为锚点
        mAnchorInfo.assignFromViewAndKeepVisibleRect(focused, getPosition(focused));
    }
        

    //...
    //这里做了精简，extraForStart和extraForEnd我们都认为是0
    int extraForStart = 0;
    int extraForEnd = 0;
    //...
    int startOffset;
    int endOffset;
    final int firstLayoutDirection;
    if (mAnchorInfo.mLayoutFromEnd) {
        firstLayoutDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_TAIL
                : LayoutState.ITEM_DIRECTION_HEAD;
    } else {
        //正常情况下，firstLayoutDirection为LayoutState.ITEM_DIRECTION_TAIL
        firstLayoutDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_HEAD
                : LayoutState.ITEM_DIRECTION_TAIL;
    }

    onAnchorReady(recycler, state, mAnchorInfo, firstLayoutDirection);
    //注释3处，如果当前存在attach到RecyclerView的View，则临时detach，后面再复用。
    detachAndScrapAttachedViews(recycler);
    mLayoutState.mInfinite = resolveIsInfinite();
    mLayoutState.mIsPreLayout = state.isPreLayout();
    // noRecycleSpace not needed: recycling doesn't happen in below's fill
    // invocations because mScrollingOffset is set to SCROLLING_OFFSET_NaN
    mLayoutState.mNoRecycleSpace = 0;
    if (mAnchorInfo.mLayoutFromEnd) {//正常情况为该条件不满足。我们分析else的情况。
        //...
    } else {
        //注释4处，向end方向填充的时候，先计算一些信息。
        updateLayoutStateToFillEnd(mAnchorInfo);
        mLayoutState.mExtraFillSpace = extraForEnd;
        //注释5处，从锚点开始向end方向填充
        fill(recycler, mLayoutState, state, false);
        endOffset = mLayoutState.mOffset;
        final int lastElement = mLayoutState.mCurrentPosition;
        if (mLayoutState.mAvailable > 0) {
            extraForStart += mLayoutState.mAvailable;
        }
        //注释6处，向start方向填充的时候，计算一些信息。
        updateLayoutStateToFillStart(mAnchorInfo);
        mLayoutState.mExtraFillSpace = extraForStart;
        mLayoutState.mCurrentPosition += mLayoutState.mItemDirection;
        //注释7处，填充
        fill(recycler, mLayoutState, state, false);
        startOffset = mLayoutState.mOffset;

        if (mLayoutState.mAvailable > 0) {//什么时候回满足这个条件呢？暂时不清楚
            extraForEnd = mLayoutState.mAvailable;
            // start could not consume all it should. add more items towards end
            updateLayoutStateToFillEnd(lastElement, endOffset);
            mLayoutState.mExtraFillSpace = extraForEnd;
            fill(recycler, mLayoutState, state, false);
            endOffset = mLayoutState.mOffset;
        }
    }

    // changes may cause gaps on the UI, try to fix them.
    // TODO we can probably avoid this if neither stackFromEnd/reverseLayout/RTL values have
    // changed
    if (getChildCount() > 0) {
        // because layout from end may be changed by scroll to position
        // we re-calculate it.
        // find which side we should check for gaps.
        if (mShouldReverseLayout ^ mStackFromEnd) {//默认情况下为false，我们看else分支
            //...
        } else {
            //注释8处，
            int fixOffset = fixLayoutStartGap(startOffset, recycler, state, true);
            startOffset += fixOffset;
            endOffset += fixOffset;
            //注释9处，
            fixOffset = fixLayoutEndGap(endOffset, recycler, state, false);
            startOffset += fixOffset;
            endOffset += fixOffset;
        }
    }
    //如果必要的话，为预执行动画布局子View。
    layoutForPredictiveAnimations(recycler, state, startOffset, endOffset);
    if (!state.isPreLayout()) {
        //注释10处，如果不是处于预布局状态，标记布局结束。
        mOrientationHelper.onLayoutComplete();
    } else {
        //注释11处，否则重置mAnchorInfo
        mAnchorInfo.reset();
    }
    mLastStackFromEnd = mStackFromEnd;
    
}
```

注释1处，找到锚点，为了简单，我们只看注释2处查找锚点的逻辑。

注释2处，计算锚点位置和坐标。我们直接看LinearLayoutManager的updateAnchorInfoForLayout方法。

```java
private void updateAnchorInfoForLayout(RecyclerView.Recycler recycler, RecyclerView.State state,
            AnchorInfo anchorInfo) {
    if (updateAnchorFromPendingData(state, anchorInfo)) {
        if (DEBUG) {
            Log.d(TAG, "updated anchor info from pending information");
        }
        return;
    }

    if (updateAnchorFromChildren(recycler, state, anchorInfo)) {
        if (DEBUG) {
            Log.d(TAG, "updated anchor info from existing children");
        }
        return;
    }
    if (DEBUG) {
        Log.d(TAG, "deciding anchor info for fresh state");
    }
    anchorInfo.assignCoordinateFromPadding();
    //注释1处
    anchorInfo.mPosition = mStackFromEnd ? state.getItemCount() - 1 : 0;
}
```

第一次布局的时候会走到注释1处，如果mStackFromEnd为false，锚点anchorInfo.mPosition就是0。

注释3处，如果当前存在attach到RecyclerView的View，则临时detach，后面再复用。

注释4处，向end方向填充的时候，先计算一些信息。


```java
private void updateLayoutStateToFillEnd(AnchorInfo anchorInfo) {
    updateLayoutStateToFillEnd(anchorInfo.mPosition, anchorInfo.mCoordinate);
}

private void updateLayoutStateToFillEnd(int itemPosition, int offset) {
    //需要填充的像素数
    mLayoutState.mAvailable = mOrientationHelper.getEndAfterPadding() - offset;
    mLayoutState.mItemDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_HEAD :
            LayoutState.ITEM_DIRECTION_TAIL;
    //从前面的分析我们知道，默认itemPosition是0
    mLayoutState.mCurrentPosition = itemPosition;
    mLayoutState.mLayoutDirection = LayoutState.LAYOUT_END;
    mLayoutState.mOffset = offset;
    //mLayoutState将mScrollingOffset置为LayoutState.SCROLLING_OFFSET_NaN
    mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
}
```

LayoutManager的onLayoutChildren方法的注释5处，调用fill方法开始从锚点开始向end方向（对于默认的LinearLayoutManager，就是从锚点向下）填充RecyclerView，传入的最后一个参数为false注意一下。

```java
fill(recycler, mLayoutState, state, false);
```

LinearLayoutManager的fill方法。

```java
/**
 *
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
        /**
         * Consume the available space if:
         * * layoutChunk did not request to be ignored
         * * OR we are laying out scrap children
         * * OR we are not doing pre-layout
         */
        if (!layoutChunkResult.mIgnoreConsumed || layoutState.mScrapList != null
                || !state.isPreLayout()) {
            layoutState.mAvailable -= layoutChunkResult.mConsumed;
            // we keep a separate remaining space because mAvailable is important for recycling
            remainingSpace -= layoutChunkResult.mConsumed;
        }

        if (stopOnFocusable && layoutChunkResult.mFocusable) {
            break;
        }
    }
    
    return start - layoutState.mAvailable;
}
```

注释1处，LinearLayoutManager的layoutChunk方法。

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
    } else {
        if (mShouldReverseLayout == (layoutState.mLayoutDirection
                == LayoutState.LAYOUT_START)) {
            addDisappearingView(view);
        } else {
            addDisappearingView(view, 0);
        }
    }
    //注释5处，测量子View的大小
    measureChildWithMargins(view, 0, 0);
    result.mConsumed = mOrientationHelper.getDecoratedMeasurement(view);
    int left, top, right, bottom;
    if (mOrientation == VERTICAL) {
        if (isLayoutRTL()) {
            right = getWidth() - getPaddingRight();
            left = right - mOrientationHelper.getDecoratedMeasurementInOther(view);
        } else {
            left = getPaddingLeft();
            right = left + mOrientationHelper.getDecoratedMeasurementInOther(view);
        }
        if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
            bottom = layoutState.mOffset;
            top = layoutState.mOffset - result.mConsumed;
        } else {
            top = layoutState.mOffset;
            bottom = layoutState.mOffset + result.mConsumed;
        }
    } else {
        top = getPaddingTop();
        bottom = top + mOrientationHelper.getDecoratedMeasurementInOther(view);

        if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
            right = layoutState.mOffset;
            left = layoutState.mOffset - result.mConsumed;
        } else {
            left = layoutState.mOffset;
            right = layoutState.mOffset + result.mConsumed;
        }
    }
    //注释6处，布局子View，并将margin和分割线也考虑在内。
    layoutDecoratedWithMargins(view, left, top, right, bottom);
    // Consume the available space if the view is not removed OR changed
    if (params.isItemRemoved() || params.isItemChanged()) {
        result.mIgnoreConsumed = true;
    }
    result.mFocusable = view.hasFocusable();
}
```

注释1处，获取子View，可能是从缓存中或者新创建的View。后面分析缓存相关的点的时候再看。

注释2处，如果获取到的子View为null，将LayoutChunkResult的mFinished置为true，用于跳出循环然后直接return。

注释3处和注释4处根据填充方向添加子View。

注释5处，测量子View的大小。

注释6处，布局子View，并将margin和分割线也考虑在内。

我们回到LayoutManager的onLayoutChildren方法的注释6处，向start方向填充的时候，计算一些信息，逻辑和updateLayoutStateToFillEnd类似，不再赘述。

LayoutManager的onLayoutChildren方法的注释7处，调用fill方法继续填充。

注释8处，做一些滚动调整。注意传入的canOffsetChildren参数为true。

```java
private int fixLayoutStartGap(int startOffset, RecyclerView.Recycler recycler,
        RecyclerView.State state, boolean canOffsetChildren) {
    int gap = startOffset - mOrientationHelper.getStartAfterPadding();
    int fixOffset = 0;
    if (gap > 0) {
        //注释1处，check if we should fix this gap.
        fixOffset = -scrollBy(gap, recycler, state);
    } else {
        return 0; // nothing to fix
    }
    startOffset += fixOffset;
    if (canOffsetChildren) {
        // re-calculate gap, see if we could fix it
        gap = startOffset - mOrientationHelper.getStartAfterPadding();
        if (gap > 0) {
            //注释2处，如果gap大于0，偏移所有的子View。
            mOrientationHelper.offsetChildren(-gap);
            return fixOffset - gap;
        }
    }
    return fixOffset;
}
```

注释1处，调用LinearLayoutManager的scrollBy方法。

```java
int scrollBy(int delta, RecyclerView.Recycler recycler, RecyclerView.State state) {
    if (getChildCount() == 0 || delta == 0) {
        return 0;
    }
    ensureLayoutState();
    mLayoutState.mRecycle = true;
    final int layoutDirection = delta > 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
    final int absDelta = Math.abs(delta);
    updateLayoutState(layoutDirection, absDelta, true, state);
    //注释1处，注意，这里继续调用fill方法填充子View。
    final int consumed = mLayoutState.mScrollingOffset
            + fill(recycler, mLayoutState, state, false);
    if (consumed < 0) {
        if (DEBUG) {
            Log.d(TAG, "Don't have any more elements to scroll");
        }
        return 0;
    }
    final int scrolled = absDelta > consumed ? layoutDirection * consumed : delta;
    //注释2处，偏移所有的子View
    mOrientationHelper.offsetChildren(-scrolled);
    if (DEBUG) {
        Log.d(TAG, "scroll req: " + delta + " scrolled: " + scrolled);
    }
    mLayoutState.mLastScrollDelta = scrolled;
    return scrolled;
}
```
注释1处，注意，这里继续调用fill方法填充子View。

注释2处，偏移所有的子View。

LayoutManager的onLayoutChildren方法的注释9处，调用fixLayoutEndGap方法，和fixLayoutStartGap类似的逻辑。

LayoutManager的onLayoutChildren方法的注释10处，如果不是处于预布局阶段，标记布局结束，不用运行动画。

LayoutManager的onLayoutChildren方法的注释11处，重置mAnchorInfo，后面需要运行动画。

到这里，dispatchLayoutStep2算是分析完了，onMeasure方法的分析也到此为止。（这也算是分析，不就是把源码贴出来，加了点注释吗？这。。。）

接下来我们看看RecyclerView的onLayout方法。

```java
@Override
protected void onLayout(boolean changed, int l, int t, int r, int b) {
    TraceCompat.beginSection(TRACE_ON_LAYOUT_TAG);
    //注释1处，调用dispatchLayout方法。
    dispatchLayout();
    TraceCompat.endSection();
    //注释2处。标记第一次布局完成。
    mFirstLayoutComplete = true;
}
```

注释2处，标记第一次布局完成。

注释1处，调用dispatchLayout方法。

```java
/**
 * 该方法可以看做是layoutChildren()方法的一个包装，处理由于布局造成的动画改变。动画的工作机制基于有5中不同类型的动画的假设：
 * PERSISTENT: 在布局前后，items一直可见。
 * REMOVED: 在布局之前items可见，在布局之后，items被应用移除。
 * ADDED: 在布局之前items不存在，items是应用添加到RecyclerView的。
 * DISAPPEARING: 在布局前后items存在于数据集中，但是在布局过程中可见性由可见变为不可见。（这些items是由于其他变化的副作用而被移动到屏幕之外了）
 * APPEARING: 在布局前后items存在于数据集中，但是在布局过程中可见性由不可见变为可见。（这些items是由于其他变化的副作用而被移动到屏幕之中了）
 *
 * 方法的大体逻辑就是计算每个item在布局前后是否存在，并推断出它们处于上述五种状态的哪一种，然后设置不同的动画。
 * PERSISTENT类型的Views会运行animatePersistence动画
 * DISAPPEARING类型的Views运行animateDisappearance动画。
 * APPEARING类型的Views运行animateAppearance动画 
 * REMOVED和ADDED类型的Views执行animateChange动画。 
 */
void dispatchLayout() {
    if (mAdapter == null) {
        Log.e(TAG, "No adapter attached; skipping layout");
        // leave the state in START
        return;
    }
    if (mLayout == null) {
        Log.e(TAG, "No layout manager attached; skipping layout");
        // leave the state in START
        return;
    }
    mState.mIsMeasuring = false;
    if (mState.mLayoutStep == State.STEP_START) {
        dispatchLayoutStep1();
        mLayout.setExactMeasureSpecsFrom(this);
        dispatchLayoutStep2();
    } else if (mAdapterHelper.hasUpdates() || mLayout.getWidth() != getWidth()
            || mLayout.getHeight() != getHeight()) {
        // First 2 steps are done in onMeasure but looks like we have to run again due to
        // changed size.
        mLayout.setExactMeasureSpecsFrom(this);
        dispatchLayoutStep2();
    } else {
        //正常情况下，会走到这里，将RecyclerView的测量信息同步到LayoutManager
        mLayout.setExactMeasureSpecsFrom(this);
    }
    //调用dispatchLayoutStep3方法。
    dispatchLayoutStep3();
}
```

RecyclerView的dispatchLayoutStep3方法。

```java
/**
 * 布局的最后一步，在这里我们会保存和动画相关的Views的信息，触发动画并执行必要的清理工作。
 */
private void dispatchLayoutStep3() {
    mState.assertLayoutStep(State.STEP_ANIMATIONS);
    startInterceptRequestLayout();
    onEnterLayoutOrScroll();
    //将状态重置为State.STEP_START
    mState.mLayoutStep = State.STEP_START;
    //是否执行动画
    if (mState.mRunSimpleAnimations) {
        // Step 3: Find out where things are now, and process change animations.
        // traverse list in reverse because we may call animateChange in the loop which may
        // remove the target view holder.
        for (int i = mChildHelper.getChildCount() - 1; i >= 0; i--) {
            ViewHolder holder = getChildViewHolderInt(mChildHelper.getChildAt(i));
            if (holder.shouldIgnore()) {
                continue;
            }
            long key = getChangedHolderKey(holder);
            final ItemHolderInfo animationInfo = mItemAnimator
                    .recordPostLayoutInformation(mState, holder);
            ViewHolder oldChangeViewHolder = mViewInfoStore.getFromOldChangeHolders(key);
            if (oldChangeViewHolder != null && !oldChangeViewHolder.shouldIgnore()) {
                // 运行一个change动画

                // If an Item is CHANGED but the updated version is disappearing, it creates
                // a conflicting case.
                // Since a view that is marked as disappearing is likely to be going out of
                // bounds, we run a change animation. Both views will be cleaned automatically
                // once their animations finish.
                // On the other hand, if it is the same view holder instance, we run a
                // disappearing animation instead because we are not going to rebind the updated
                // VH unless it is enforced by the layout manager.
                final boolean oldDisappearing = mViewInfoStore.isDisappearing(
                        oldChangeViewHolder);
                final boolean newDisappearing = mViewInfoStore.isDisappearing(holder);
                if (oldDisappearing && oldChangeViewHolder == holder) {
                    // run disappear animation instead of change
                    mViewInfoStore.addToPostLayout(holder, animationInfo);
                } else {
                    final ItemHolderInfo preInfo = mViewInfoStore.popFromPreLayout(
                            oldChangeViewHolder);
                    // we add and remove so that any post info is merged.
                    mViewInfoStore.addToPostLayout(holder, animationInfo);
                    ItemHolderInfo postInfo = mViewInfoStore.popFromPostLayout(holder);
                    if (preInfo == null) {
                        handleMissingPreInfoForChangeError(key, holder, oldChangeViewHolder);
                    } else {
                        //运行change动画
                        animateChange(oldChangeViewHolder, holder, preInfo, postInfo,
                                oldDisappearing, newDisappearing);
                    }
                }
            } else {
                //保存动画信息
                mViewInfoStore.addToPostLayout(holder, animationInfo);
            }
        }

        //处理view info lists 并触发动画
        mViewInfoStore.process(mViewInfoProcessCallback);
    }

    mLayout.removeAndRecycleScrapInt(mRecycler);
    mState.mPreviousLayoutItemCount = mState.mItemCount;
    mDataSetHasChangedAfterLayout = false;
    mDispatchItemsChangedEvent = false;
    mState.mRunSimpleAnimations = false;

    mState.mRunPredictiveAnimations = false;
    mLayout.mRequestedSimpleAnimations = false;
    if (mRecycler.mChangedScrap != null) {
        mRecycler.mChangedScrap.clear();
    }
    if (mLayout.mPrefetchMaxObservedInInitialPrefetch) {
        // Initial prefetch has expanded cache, so reset until next prefetch.
        // This prevents initial prefetches from expanding the cache permanently.
        mLayout.mPrefetchMaxCountObserved = 0;
        mLayout.mPrefetchMaxObservedInInitialPrefetch = false;
        mRecycler.updateViewCacheSize();
    }
    //标记布局完成。
    mLayout.onLayoutCompleted(mState);
    onExitLayoutOrScroll();
    stopInterceptRequestLayout(false);
    //清除mViewInfoStore
    mViewInfoStore.clear();
    if (didChildRangeChange(mMinMaxLayoutPositions[0], mMinMaxLayoutPositions[1])) {
        dispatchOnScrolled(0, 0);
    }
    recoverFocusFromState();
    resetFocusInfo();
}
```

动画相关信息我们先不看，到这里layout过程结束，下面继续看绘制过程。

```java
@Override
public void onDraw(Canvas c) {
    super.onDraw(c);

    final int count = mItemDecorations.size();
    for (int i = 0; i < count; i++) {
        mItemDecorations.get(i).onDraw(c, this, mState);
    }
}
```
onDraw方法里先绘制了分割线。然后在drawChild方法中绘制子View。没啥可说的。

接下来我们看一看在滑动和fling的时候，RecyclerView的一些逻辑。

RecyclerView的onTouchEvent方法。

```java
@Override
public boolean onTouchEvent(MotionEvent e) {
    //...

    switch (action) {
        case MotionEvent.ACTION_DOWN: {
           //...
        } break;

       //...
        case MotionEvent.ACTION_MOVE: {
            final int index = e.findPointerIndex(mScrollPointerId);
            if (index < 0) {
                Log.e(TAG, "Error processing scroll; pointer index for id "
                        + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                return false;
            }

            final int x = (int) (e.getX(index) + 0.5f);
            final int y = (int) (e.getY(index) + 0.5f);
            int dx = mLastTouchX - x;
            int dy = mLastTouchY - y;

            if (mScrollState != SCROLL_STATE_DRAGGING) {
                boolean startScroll = false;
                if (canScrollHorizontally) {
                   //横向滑动的忽略...
                }
                if (canScrollVertically) {
                    if (dy > 0) {
                        dy = Math.max(0, dy - mTouchSlop);
                    } else {
                        dy = Math.min(0, dy + mTouchSlop);
                    }
                    if (dy != 0) {
                        startScroll = true;
                    }
                }
                if (startScroll) {//到达了滑动的条件，将滑动状态置为SCROLL_STATE_DRAGGING
                    setScrollState(SCROLL_STATE_DRAGGING);
                }
            }

            if (mScrollState == SCROLL_STATE_DRAGGING) {
                //...
                //注释1处，
                if (scrollByInternal(
                        canScrollHorizontally ? dx : 0,
                        canScrollVertically ? dy : 0,
                        e)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                //注释2处，这个先忽略，应该是处理多个RecyclerView的时候，预先获取ViewHolder的操作。
                if (mGapWorker != null && (dx != 0 || dy != 0)) {
                    mGapWorker.postFromTraversal(this, dx, dy);
                }
            }
        } break;

        case MotionEvent.ACTION_UP: {
            mVelocityTracker.addMovement(vtev);
            eventAddedToVelocityTracker = true;
            mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
            final float xvel = canScrollHorizontally
                    ? -mVelocityTracker.getXVelocity(mScrollPointerId) : 0;
            final float yvel = canScrollVertically
                    ? -mVelocityTracker.getYVelocity(mScrollPointerId) : 0;
            //注释3处，如果速度够了，fling
            if (!((xvel != 0 || yvel != 0) && fling((int) xvel, (int) yvel))) {
                    setScrollState(SCROLL_STATE_IDLE);
            }
            resetScroll();
        } break;
    }

    //...

    return true;
}
```

注释1处，调用RecyclerView的scrollByInternal方法。

```java
 boolean scrollByInternal(int x, int y, MotionEvent ev) {
    //...
    if (mAdapter != null) {
        scrollStep(x, y, mReusableIntPair);
    }
    //...    
    return consumedNestedScroll || consumedX != 0 || consumedY != 0;
}
```

在scrollByInternal的方法内部，调用了调用RecyclerView的scrollStep方法。

```java
void scrollStep(int dx, int dy, @Nullable int[] consumed) {
        
    //...
    //我们只看竖直方向上的滚动
    if (dy != 0) {
        consumedY = mLayout.scrollVerticallyBy(dy, mRecycler, mState);
    }
}
```

调用LinearLayoutManager的scrollVerticallyBy方法。

```java
@Override
public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
        RecyclerView.State state) {
    //...
    return scrollBy(dy, recycler, state);
}
```

LinearLayoutManager的scrollBy方法我们上面已经分析过了，内部会调用fill方法填充子View，并将所有的子View偏移。

注释3处，看fling操作。

```java
public boolean fling(int velocityX, int velocityY) {
    
     //精简大量代码
     
     mViewFlinger.fling(velocityX, velocityY);
     return true;
}
```

fling方法调用了mViewFlinger的fling方法来实现fling操作。


```java
class ViewFlinger implements Runnable { 

    ViewFlinger() {
        mOverScroller = new OverScroller(getContext(), sQuinticInterpolator);
    }

    public void fling(int velocityX, int velocityY) {
        //设置滚动状态为SCROLL_STATE_SETTLING
        setScrollState(SCROLL_STATE_SETTLING);
        mLastFlingX = mLastFlingY = 0;
        //注释1处，计算坐标
        mOverScroller.fling(0, 0, velocityX, velocityY,
                Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        //注释2处，
        postOnAnimation();
    }
}

```

ViewFlinger实现了Runnable接口。fling方法的注释1处，计算坐标，然后在注释2处调用postOnAnimation方法。postOnAnimation方法内部会将ViewFlinger作为一个Runnable对象post到一个消息队列中，然后在下一帧到来的时候，执行
ViewFlinger的run方法。



```java
@Override
public void run() {
           
    mReSchedulePostAnimationCallback = false;
    mEatRunOnAnimationRequest = true;

    // Keep a local reference so that if it is changed during onAnimation method, it won't
    // cause unexpected behaviors
    final OverScroller scroller = mOverScroller;
    //注释1处，如果滚动没有结束
    if (scroller.computeScrollOffset()) {
        final int x = scroller.getCurrX();
        final int y = scroller.getCurrY();
        int unconsumedX = x - mLastFlingX;
        int unconsumedY = y - mLastFlingY;
        mLastFlingX = x;
        mLastFlingY = y;
        int consumedX = 0;
        int consumedY = 0;

        //...
        // Local Scroll
        if (mAdapter != null) {
            mReusableIntPair[0] = 0;
            mReusableIntPair[1] = 0;
            //注释2处，重点
            scrollStep(unconsumedX, unconsumedY, mReusableIntPair);
            consumedX = mReusableIntPair[0];
            consumedY = mReusableIntPair[1];
            unconsumedX -= consumedX;
            unconsumedY -= consumedY;
        }
                
        if (!mItemDecorations.isEmpty()) {
            invalidate();
        }

        boolean scrollerFinishedX = scroller.getCurrX() == scroller.getFinalX();
        boolean scrollerFinishedY = scroller.getCurrY() == scroller.getFinalY();
        //注释3处，是否已经结束了
        final boolean doneScrolling = scroller.isFinished()
                || ((scrollerFinishedX || unconsumedX != 0)
                && (scrollerFinishedY || unconsumedY != 0));

        SmoothScroller smoothScroller = mLayout.mSmoothScroller;
        boolean smoothScrollerPending =
                 smoothScroller != null && smoothScroller.isPendingInitialRun();

        if (!smoothScrollerPending && doneScrolling) {
                    //注释4处，滚动结束了...
                    
        } else {
            //注释5处，滚动没有结束，继续post。
            postOnAnimation();
            //...
        }
    }

    mEatRunOnAnimationRequest = false;
    //注释6处，mReSchedulePostAnimationCallback为true，
    if (mReSchedulePostAnimationCallback) {
        internalPostOnAnimation();
    } else {
        //注释7处，滚动结束，不需要post了。
        setScrollState(SCROLL_STATE_IDLE);
        stopNestedScroll(TYPE_NON_TOUCH);
    }
}

```

注释1处，如果滚动没有结束。

注释2处，重点，调用scrollStep方法，内部会最终调用fill方法填充子View，并将所有的子View偏移。

注释3处，判断是否已经结束了（这里有点不明白，不是滚动没结束才进入这里吗，为什么还要重复判断，这里猜测是因为嵌套滑动的问题，我们忽略）。

注释5处，滚动没有结束，继续post，在下一帧到来的时候继续执行ViewFlinger的run方法。

```java
void postOnAnimation() {
    if (mEatRunOnAnimationRequest) {
        mReSchedulePostAnimationCallback = true;
    } else {
        internalPostOnAnimation();
    }
}
```

注意，因为这时候mEatRunOnAnimationRequest为true，所以postOnAnimation只是将mReSchedulePostAnimationCallback置为了true，并没有调用internalPostOnAnimation方法真正post。

注释6处，mReSchedulePostAnimationCallback为true，调用internalPostOnAnimation方法真正post。



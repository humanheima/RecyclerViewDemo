# 草稿

LayoutManager 的作用就是负责摆放 ViewHolder 对应的 ItemView。 无外乎三点：

1. 从哪里开始摆放？LinearLayoutManager.LayoutState#mPosition：适配器中下一个要填充的item的位置。LayoutState#mOffset ：开始填充的位置。
2. 怎么摆放？LayoutManager#layoutDecoratedWithMargins(View, left, top, right, bottom)：摆放子View。
3. 摆放到哪里结束？LayoutState#mAvailable < 0 或者没有更多数据了。

涉及到的类：

* LinearLayoutManager.AnchorInfo，保存锚点信息。
* LinearLayoutManager.LayoutState，在布局过程中，保存临时的布局状态。
* LinearLayoutManager.LayoutChunkResult，保存布局结果。


## 分析场景：

1. RecyclerView 使用 LinearLayoutManager ，从上到下布局，RecyclerView 的 宽高都是 MATCH_PARENT。
2. 正常设置了LayoutManager和Adapter以后，第一次是如何摆放 ItemView 的。
3. 在滚动过程中（move和fling）的时候，是如何摆放 ItemView 的。


先说下分析的结论：

1. RecyclerView 的 宽高都是 MATCH_PARENT。那么在 onMeasure的时候，是不会调用 dispatchLayoutStep1 和 dispatchLayoutStep2 的。
2. 第一次设置LayoutManager和Adapter是没有动画效果的，可以认为 dispatchLayoutStep1 和 dispatchLayoutStep3  这两个方法都没起作用。
3. dispatchLayoutStep2 方法，真正起布局作用的方法。内部调用 fill 方法，获取ViewHolder(新创建的，或者从缓存中获取的)，测量、布局、添加到 RecyclerView。
4. 第一次布局的时候，RecyclerView 的 childCount 还是0，是没有ViewHolder的回收和复用的。
5. 就是在滑动和fling的时候，LayoutManager会调用 `fill` 方法，获取ViewHolder填充的RecyclerView，并把滑出RecyclerView的ViewHolder回收。


#### 示例代码

```java
LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
recyclerView.setLayoutManager(linearLayoutManager);
recyclerView.setAdapter(...);
```

使用RecyclerView的两部曲。`setLayoutManager()`和`setAdapter`。 RecyclerView的`setLayoutManager()`和`setAdapter`方法内部都会调用requestLayout方法，请求measure、layout、draw。

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
           //注释2.1处，Note: 注意，当我们在布局里设置 RecyclerView 的宽高为 match_parent 的时候，这里的 widthMode 和 heightMode 都是 MeasureSpec.EXACTLY，会直接return
            return;
        }
        //...
    } 
}
```

注释2处，是否开启自动测量，我们以LinearLayoutManager来分析，LinearLayoutManager默认是true。

注释2.1处，Note: 注意，当我们在布局里设置 RecyclerView 的宽高为 match_parent 的时候， 这里的 widthMode 和 heightMode 都是 MeasureSpec.EXACTLY，会直接return。

在我们的测试设备中，RecyclerView的宽是1080，高是 2255。

接下来我们看看RecyclerView的onLayout方法。

```java
@Override
protected void onLayout(boolean changed, int l, int t, int r, int b) {
    TraceCompat.beginSection(TRACE_ON_LAYOUT_TAG);
    //注释1处，调用dispatchLayout方法。
    dispatchLayout();
    TraceCompat.endSection();
    mFirstLayoutComplete = true;
}
```

注释1处，调用dispatchLayout方法。

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

注释1处，调用dispatchLayoutStep1方法。

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
    //注释1处，处理适配器更新和设置动画的标志位。RecyclerView第一次布局，是没有什么动画效果的。
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


第一次调用dispatchLayoutStep1的时候，此时RecyclerView还没有子View所以不会有什么动画执行。在我们的分析场景中可以认为没有作用。

dispatchLayout方法注释2处，调用dispatchLayoutStep2方法。

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

    // 注释1处，将预布局状态置为false。
    // mInPreLayout 为 false 的时候，不会从 mChangedScrap 中获取 ViewHolder
    mState.mInPreLayout = false;
    //注释2处，布局子View
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

注释1处，将预布局状态置为false。mInPreLayout 为 false 的时候，不会从 Recycler.mChangedScrap 中查找缓存的 ViewHolder。

注释2处，调用LayoutManager的onLayoutChildren方法。我们直接看 LinearLayoutManager 的 onLayoutChildren 方法。

```java
public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    //...    
    if(mPendingSavedState != null || mPendingScrollPosition != RecyclerView.NO_POSITION) {
        if(state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }
    }
    if(mPendingSavedState != null && mPendingSavedState.hasValidAnchor()) {
        mPendingScrollPosition = mPendingSavedState.mAnchorPosition;
    }
    //注释1处，如果mLayoutState为null的话，则创建。
    ensureLayoutState();
    mLayoutState.mRecycle = false;
    //决定布局顺序，是否要倒着布局。LinearLayoutManager默认是从上到下布局。    
    resolveShouldLayoutReverse();
    final View focused = getFocusedChild();
    if(!mAnchorInfo.mValid || mPendingScrollPosition != RecyclerView.NO_POSITION || mPendingSavedState != null) {
        mAnchorInfo.reset();
        mAnchorInfo.mLayoutFromEnd = mShouldReverseLayout ^ mStackFromEnd;
        // calculate anchor position and coordinate
        //注释2处，计算锚点的位置和坐标
        updateAnchorInfoForLayout(recycler, state, mAnchorInfo);
        //此时评估锚点信息 AnchorInfo{mPosition=0, mCoordinate=0, mLayoutFromEnd=false, mValid=true}
        mAnchorInfo.mValid = true;
    } else if(focused != null && (mOrientationHelper.getDecoratedStart(focused) >= mOrientationHelper.getEndAfterPadding() || mOrientationHelper.getDecoratedEnd(focused) <= mOrientationHelper.getStartAfterPadding())) {
        mAnchorInfo.assignFromViewAndKeepVisibleRect(focused, getPosition(focused));
    }
    
    // LLM may decide to layout items for "extra" pixels to account for scrolling target,
    // caching or predictive animations.
    // 开始方向是 LayoutState.LAYOUT_END 向end方向填充    
    mLayoutState.mLayoutDirection = mLayoutState.mLastScrollDelta >= 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
    mReusableIntPair[0] = 0;
    mReusableIntPair[1] = 0;
    calculateExtraLayoutSpace(state, mReusableIntPair);
    int extraForStart = Math.max(0, mReusableIntPair[0]) + mOrientationHelper.getStartAfterPadding();
    int extraForEnd = Math.max(0, mReusableIntPair[1]) + mOrientationHelper.getEndPadding();
    if(state.isPreLayout() && mPendingScrollPosition != RecyclerView.NO_POSITION && mPendingScrollPositionOffset != INVALID_OFFSET) {
        // if the child is visible and we are going to move it around, we should layout
        // extra items in the opposite direction to make sure new items animate nicely
        // instead of just fading in
        final View existing = findViewByPosition(mPendingScrollPosition);
        if(existing != null) {
            final int current;
            final int upcomingOffset;
            if(mShouldReverseLayout) {
                current = mOrientationHelper.getEndAfterPadding() - mOrientationHelper.getDecoratedEnd(existing);
                upcomingOffset = current - mPendingScrollPositionOffset;
            } else {
                current = mOrientationHelper.getDecoratedStart(existing) - mOrientationHelper.getStartAfterPadding();
                upcomingOffset = mPendingScrollPositionOffset - current;
            }
            if(upcomingOffset > 0) {
                extraForStart += upcomingOffset;
            } else {
                extraForEnd -= upcomingOffset;
            }
        }
    }
    int startOffset;
    int endOffset;
    final int firstLayoutDirection;
    if(mAnchorInfo.mLayoutFromEnd) {
        firstLayoutDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_TAIL : LayoutState.ITEM_DIRECTION_HEAD;
    } else {
        //默认是 LayoutState.ITEM_DIRECTION_TAIL
        firstLayoutDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_HEAD : LayoutState.ITEM_DIRECTION_TAIL;
    }
    onAnchorReady(recycler, state, mAnchorInfo, firstLayoutDirection);
    //注释3处，回收ViewHolder，第一次进来，RecyclerView是没有子View的，没有回收动作。
    detachAndScrapAttachedViews(recycler);
    //默认不是无限
    mLayoutState.mInfinite = resolveIsInfinite();
    //是否是预布局状态
    mLayoutState.mIsPreLayout = state.isPreLayout();
    // noRecycleSpace not needed: recycling doesn't happen in below's fill
    // invocations because mScrollingOffset is set to SCROLLING_OFFSET_NaN
    mLayoutState.mNoRecycleSpace = 0;
    if(mAnchorInfo.mLayoutFromEnd) {
        //...
    } else {
        // 注释4处，向end方向填充的时候，先计算一些信息。
        updateLayoutStateToFillEnd(mAnchorInfo);
        mLayoutState.mExtraFillSpace = extraForEnd;
        //注释5处，调用fill方法从锚点开始向end方向（对于默认的LinearLayoutManager，就是从锚点向下）填充RecyclerView，传入的最后一个参数为false注意一下。
        fill(recycler, mLayoutState, state, false);
        endOffset = mLayoutState.mOffset;
        final int lastElement = mLayoutState.mCurrentPosition;
        if(mLayoutState.mAvailable > 0) {
            extraForStart += mLayoutState.mAvailable;
        }
        //注释6处，向start方向填充的时候，先计算一些信息。
        updateLayoutStateToFillStart(mAnchorInfo);
        mLayoutState.mExtraFillSpace = extraForStart;
        //注释7处，这里会把 mCurrentPosition = 锚点的位置减去1。
        mLayoutState.mCurrentPosition += mLayoutState.mItemDirection;
        //注释8处，调用fill方法向start方向填充
        fill(recycler, mLayoutState, state, false);
        startOffset = mLayoutState.mOffset;
        if(mLayoutState.mAvailable > 0) {
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
    if(getChildCount() > 0) {
        // because layout from end may be changed by scroll to position
        // we re-calculate it.
        // find which side we should check for gaps.
        if(mShouldReverseLayout ^ mStackFromEnd) {
            int fixOffset = fixLayoutEndGap(endOffset, recycler, state, true);
            startOffset += fixOffset;
            endOffset += fixOffset;
            fixOffset = fixLayoutStartGap(startOffset, recycler, state, false);
            startOffset += fixOffset;
            endOffset += fixOffset;
        } else {
            int fixOffset = fixLayoutStartGap(startOffset, recycler, state, true);
            startOffset += fixOffset;
            endOffset += fixOffset;
            fixOffset = fixLayoutEndGap(endOffset, recycler, state, false);
            startOffset += fixOffset;
            endOffset += fixOffset;
        }
    }
    layoutForPredictiveAnimations(recycler, state, startOffset, endOffset);
    if(!state.isPreLayout()) {
        mOrientationHelper.onLayoutComplete();
    } else {
        mAnchorInfo.reset();
    }
    mLastStackFromEnd = mStackFromEnd;
    if(DEBUG) {
        validateChildOrder();
    }
}
```

注释1处，如果 mLayoutState 为null 的话，则创建。布局过程中用来保存布局状态，在布局结束的时候，状态就被重置了。

注释2处，计算锚点位置和坐标。第一次布局，`AnchorInfo.mCoordinate = ` RecyclerView的 paddingTop，默认为0。
`AnchorInfo.mPosition = 0`。 此时评估锚点信息 `AnchorInfo{mPosition=0, mCoordinate=0, mLayoutFromEnd=false, mValid=true}`。

注释3处，回收ViewHolder，第一次进来，RecyclerView是没有子View的，没有回收动作。后面再看。

注释4处，向end方向填充的时候，根据锚点先计算一些信息，保存在 mLayoutState 中。

包括：
* mLayoutState.mAvailable ：可用空间
* mLayoutState.mItemDirection：定义遍历数据适配器的方向。Defines the direction in which the data adapter is traversed. Should be one of LayoutState#ITEM_DIRECTION_HEAD or LayoutState#ITEM_DIRECTION_TAIL.
* mLayoutState.mCurrentPosition：适配器获取下一个 item 的位置。Current position on the adapter to get the next item.
* mLayoutState.mLayoutDirection：布局方向，Layout direction. Should be one of LayoutState#LAYOUT_START or LayoutState#LAYOUT_END.
* mLayoutState.mOffset: 偏移量，Pixel offset where layout should start
* mLayoutState.mScrollingOffset：在滚动状态下构造LayoutState时使用。它应该设置为我们在不创建新视图的情况下可以进行的滚动量。为了高效视图回收，这个设置是必需的。

```java
private void updateLayoutStateToFillEnd(AnchorInfo anchorInfo) {
    //默认情况下，anchorInfo.mPosition 是0，anchorInfo.mCoordinate 是0，
    //在我们这个默认的例子中，anchorInfo.mCoordinate 其实就是 RecyclerView 的 paddingTop，默认是0
    updateLayoutStateToFillEnd(anchorInfo.mPosition, anchorInfo.mCoordinate);
}

private void updateLayoutStateToFillEnd(int itemPosition, int offset) {
    //可填充的空间，默认就是RecyclerView的高度 -paddingBottom - paddingBottom，也就是RecyclerView的可用空间
    mLayoutState.mAvailable = mOrientationHelper.getEndAfterPadding() - offset;
    //默认是  LayoutState.ITEM_DIRECTION_TAIL;
    mLayoutState.mItemDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_HEAD :
            LayoutState.ITEM_DIRECTION_TAIL;
    //从前面的分析我们知道，默认itemPosition是0
    mLayoutState.mCurrentPosition = itemPosition;
    mLayoutState.mLayoutDirection = LayoutState.LAYOUT_END;
    mLayoutState.mOffset = offset;
    //注释1处，这里注意一下，mLayoutState将mScrollingOffset置为LayoutState.SCROLLING_OFFSET_NaN
    mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
}
```

注释1处，这里注意一下，mLayoutState将mScrollingOffset置为LayoutState.SCROLLING_OFFSET_NaN，后面会用到。

LayoutManager的onLayoutChildren方法的注释5处，调用 `fill` 方法从锚点开始向end方向（对于默认的LinearLayoutManager，就是从锚点向下）填充RecyclerView，传入的最后一个参数为false注意一下。

```java
fill(recycler, mLayoutState, state, false);
```

LinearLayoutManager的fill方法。

```java
int fill(RecyclerView.Recycler recycler, LayoutState layoutState,
        RecyclerView.State state, boolean stopOnFocusable) {
    //记录开始填充的时候，可用的空间
    final int start = layoutState.mAvailable;
    if (layoutState.mScrollingOffset != LayoutState.SCROLLING_OFFSET_NaN) {
        // TODO ugly bug fix. should not happen
        if (layoutState.mAvailable < 0) {
            //注释1处，这里重新为mScrollingOffset赋值
            layoutState.mScrollingOffset += layoutState.mAvailable;
        }
        recycleByLayoutState(recycler, layoutState);
    }
    // 注释0处，记录剩余的可填充的空间
    int remainingSpace = layoutState.mAvailable + layoutState.mExtraFillSpace;
    LayoutChunkResult layoutChunkResult = mLayoutChunkResult;
    //注释1处，只要还有剩余空间remainingSpace并且还有数据，调用layoutChunk方法，获取ViewHolder 填充RecyclerView。
    while ((layoutState.mInfinite || remainingSpace > 0) && layoutState.hasMore(state)) {
        layoutChunkResult.resetInternal();
        //注释2处，获取并添加子View，然后测量、布局子View并将分割线考虑在内。
        layoutChunk(recycler, state, layoutState, layoutChunkResult);
        //如果没有更多View了，布局结束，跳出循环
        if (layoutChunkResult.mFinished) {
            break;
        }
        //注释3处，增加偏移量，加上已经填充的像素，下一个View的top坐标就是layoutState.mOffset
        layoutState.mOffset += layoutChunkResult.mConsumed * layoutState.mLayoutDirection;
        if (!layoutChunkResult.mIgnoreConsumed || layoutState.mScrapList != null
                || !state.isPreLayout()) {
            //注释4处，可用空间减去已经填充的像素        
            layoutState.mAvailable -= layoutChunkResult.mConsumed;
            // 我们维护一个 单独的 remainingSpace，mAvailable 对回收来说很重要，我们不去修改它。
            //注释5处，剩余空间，减去已经填充的像素
            remainingSpace -= layoutChunkResult.mConsumed;
        }

        if (stopOnFocusable && layoutChunkResult.mFocusable) {
            break;
        }
    }
    //注释6处，返回已经填充的空间，比如开始可用空间 start 是2255，填充完毕，可用空间 layoutState.mAvailable 是 455，就返回 1800 。填充了1800像素。
    //返回结果有可能大于start，因为最后一个填充的View有一部分在屏幕外面。
    return start - layoutState.mAvailable;
}
```
注释0处，记录剩余的可填充的空间。

注释1处，只要还有剩余空间remainingSpace并且还有数据，调用layoutChunk方法，获取ViewHolder 填充RecyclerView。

注释2处，获取并添加子View，然后测量、布局子View并将分割线考虑在内。LinearLayoutManager的layoutChunk方法。
这里就是摆放的核心逻辑了。

```java
void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state,
        LayoutState layoutState, LayoutChunkResult result) {
    //注释1处，获取子View，可能是从缓存中或者新创建的View。
    //取数据的顺序 mScrapList -> mRecycler(Recycler#mAttachedScrap或者Recycler#mChangedScrap ->ChildHelper#mHiddenViews -> Recycler#mCachedViews Recycler#mViewCacheExtension -> Recycler#mRecyclerPool ) -> (createViewHolder)
    View view = layoutState.next(recycler);
    if (view == null) {
        //注释2处，如果获取到的子View为null，将LayoutChunkResult的mFinished置为true，没有更多数据了，用于跳出循环然后直接return。
        result.mFinished = true;
        return;
    }
    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
    if (layoutState.mScrapList == null) {
        //mShouldReverseLayout == (layoutState.mLayoutDirection== LayoutState.LAYOUT_START) ，都等于false，判断两个 false 是否相等，返回true
        if (mShouldReverseLayout == (layoutState.mLayoutDirection
                == LayoutState.LAYOUT_START)) {
            //注释3处，默认是从上到下布局的时候，添加子View
            addView(view);  
        } else {
            //注释4处
            addView(view, 0);
        }
    } 
    //...
    //注释5处，测量子View的大小，包括margin和分割线。
    measureChildWithMargins(view, 0, 0);
    //注释6处，记录该View消耗的高度，包括margin和分割线。
    result.mConsumed = mOrientationHelper.getDecoratedMeasurement(view);
    int left, top, right, bottom;
    if (mOrientation == VERTICAL) {
        if (isLayoutRTL()) {
            right = getWidth() - getPaddingRight();
            left = right - mOrientationHelper.getDecoratedMeasurementInOther(view);
        } else {
            //注释6.1处，获取子View的左右坐标
            left = getPaddingLeft();
            right = left + mOrientationHelper.getDecoratedMeasurementInOther(view);
        }
        if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
            bottom = layoutState.mOffset;
            top = layoutState.mOffset - result.mConsumed;
        } else {
            //注释6.2处，获取子View的上下坐标
            top = layoutState.mOffset;
            bottom = layoutState.mOffset + result.mConsumed;
        }
    } 
    //...
    //注释7处，摆放子View
    layoutDecoratedWithMargins(view, left, top, right, bottom);
    // Consume the available space if the view is not removed OR changed
    if (params.isItemRemoved() || params.isItemChanged()) {
        result.mIgnoreConsumed = true;
    }
    result.mFocusable = view.hasFocusable();
}
```

注释1处，获取子View，可能是从缓存中或者新创建的View。

注释2处，如果获取到的子View为null，将LayoutChunkResult的mFinished置为true，用于跳出循环然后直接return。

注释3处，默认是从上到下布局的时候，添加子View。

注释5处，测量子View的大小，包括margin和分割线。

注释6处，记录该View消耗的高度，包括margin和分割线。赋值给 LayoutChunkResult#mConsumed。

注释6.1处，获取子View的左右坐标。

注释6.2处，获取子View的上下坐标。
top = layoutState.mOffset; bottom = layoutState.mOffset + result.mConsumed。


注释7处，摆放子View。


**回到 fill 方法**：

注释3处，增加偏移量，加上已经填充的像素，下一个View的top坐标就是layoutState.mOffset。

注释4处，可用空间减去已经填充的像素。

注释5处，剩余空间，减去已经填充的像素。如果没有剩余空间了或者没有更多View了，fill方法结束。

注释6处，返回已经填充的空间，比如开始可用空间 start 是2255，填充完毕，可用空间 layoutState.mAvailable 是 455，就返回 1800 。填充了1800像素。
注意：返回结果有可能大于start，因为最后一个填充的View有一部分在屏幕外面。在我们的例子中，填充了3个ItemView。每个ItemView的高度是 900。
比如开始可用空间 start 是2255，填充完毕，可用空间 layoutState.mAvailable 是 -445，start - layoutState.mAvailable = 2700，就返回 2700 。填充了2700像素。


**回到LayoutManager的onLayoutChildren方法**：

注释6处， 向start方向填充的时候，计算一些信息，逻辑和updateLayoutStateToFillEnd类似，不再赘述。

注释7处，这里会把 mCurrentPosition = 锚点的位置减去1。

注释7处，调用fill方法继续填充。注意：第一次布局的时候，正常来说我们是从0开始向下填充的，所以就不会有向上填充的情况。我们先忽略。

到这里，dispatchLayoutStep2算是分析完了。接下来是 dispatchLayoutStep3方法。

在我们的分析场景中，RecyclerView的dispatchLayoutStep3方法。可以认为没有作用，忽略。

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
        //第一次布局，这个条件不满足
        //...
    }
    //...
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

对于第一次布局来说，可以认为 dispatchLayoutStep3 只是标记布局完成，清除 mViewInfoStore(里面保存了动画信息)，动画相关信息我们先不看，到这里layout过程结束，下面继续看绘制过程。


### 接下来我们看一看在滑动和fling的时候，RecyclerView的一些逻辑。

一句话概括这个过程：就是在滑动和fling的时候，会获取ViewHolder填充的RecyclerView，并把滑出RecyclerView的ViewHolder被回收。

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
                if (startScroll) {
                    //到达了滑动的条件，将滑动状态置为SCROLL_STATE_DRAGGING
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

无论是滚动还是fling，最终都会调用LinearLayoutManager的scrollVerticallyBy方法。

```java
@Override
public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
        RecyclerView.State state) {
    //...
    return scrollBy(dy, recycler, state);
}
```

```java
int scrollBy(int delta, RecyclerView.Recycler recycler, RecyclerView.State state) {
    if (getChildCount() == 0 || delta == 0) {
        return 0;
    }
    ensureLayoutState();
    mLayoutState.mRecycle = true;
    //手指从下向上滑动，delta > 0
    //delta > 0，向下填充，layoutDirection为LayoutState.LAYOUT_END
    final int layoutDirection = delta > 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
    final int absDelta = Math.abs(delta);
    //注释0处，更新布局信息。
    updateLayoutState(layoutDirection, absDelta, true, state);
    //注释1处，注意，这里继续调用fill方法填充子View。
    final int consumed = mLayoutState.mScrollingOffset
            + fill(recycler, mLayoutState, state, false);
    if (consumed < 0) {
        //
        return 0;
    }
    // 注释2处，如果absDelta > consumed，应该是没有更多的数据了，无法消耗那么多的delta，我们只能滚动consumed的距离。
    final int scrolled = absDelta > consumed ? layoutDirection * consumed : delta;
    //注释3处，偏移所有的子View
    mOrientationHelper.offsetChildren(-scrolled);
    if (DEBUG) {
        Log.d(TAG, "scroll req: " + delta + " scrolled: " + scrolled);
    }
    //注释4处，更新mLastScrollDelta，最终滚动的距离。
    mLayoutState.mLastScrollDelta = scrolled;
    return scrolled;
}
```

注释0处，更新布局信息。

```java
private void updateLayoutState(int layoutDirection, int requiredSpace, boolean canUseExistingSpace, RecyclerView.State state) {
    // If parent provides a hint, don't measure unlimited.
    mLayoutState.mInfinite = resolveIsInfinite();
    mLayoutState.mLayoutDirection = layoutDirection;
    mReusableIntPair[0] = 0;
    mReusableIntPair[1] = 0;
    calculateExtraLayoutSpace(state, mReusableIntPair);
    int extraForStart = Math.max(0, mReusableIntPair[0]);
    int extraForEnd = Math.max(0, mReusableIntPair[1]);
    boolean layoutToEnd = layoutDirection == LayoutState.LAYOUT_END;
    mLayoutState.mExtraFillSpace = layoutToEnd ? extraForEnd : extraForStart;
    mLayoutState.mNoRecycleSpace = layoutToEnd ? extraForStart : extraForEnd;
    int scrollingOffset;
    if(layoutToEnd) {
        mLayoutState.mExtraFillSpace += mOrientationHelper.getEndPadding();
        // get the first child in the direction we are going
        //注释1处，找到最靠近底部的子View
        final View child = getChildClosestToEnd();
        // the direction in which we are traversing children
        mLayoutState.mItemDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_HEAD : LayoutState.ITEM_DIRECTION_TAIL;
        //注释2处，找到下一个要填充的位置。
        mLayoutState.mCurrentPosition = getPosition(child) + mLayoutState.mItemDirection;
        //注释3处，下一个要填充的位置的top坐标。
        mLayoutState.mOffset = mOrientationHelper.getDecoratedEnd(child);
        // calculate how much we can scroll without adding new children (independent of layout)
        //注释4处，计算在不添加新的子View的情况下我们可以滚动的距离
        scrollingOffset = mOrientationHelper.getDecoratedEnd(child) - mOrientationHelper.getEndAfterPadding();
    } else {
        //...
    }
    //注释5处，可填充的区域，这里就是滚动的距离。
    mLayoutState.mAvailable = requiredSpace;
    if(canUseExistingSpace) {
        //可用空间减去 在不添加新的子View的情况下我们可以滚动的距离
        mLayoutState.mAvailable -= scrollingOffset;
    }
    //注释6处，设置mScrollingOffset
    mLayoutState.mScrollingOffset = scrollingOffset;
}
```

注释1处，找到最靠近底部的子View。
注释2处，找到下一个要填充的位置。
注释3处，下一个要填充的位置的top坐标。mLayoutState.mOffset。
注释4处，计算在不添加新的子View的情况下我们可以滚动的距离。举个例子：
比如我们现在有3个子View，RecyclerView的高度是2255，第一个子View的top坐标是0，bottom坐标是900，
第二个子View的top坐标是900，bottom坐标是1800，第三个子View的top坐标是1800，bottom坐标是2700。

`2700 - 2255 = 445`，那么 我们可以滚动的距离是445，而不需要添加新的子View。

注释5处，可填充的区域，这里就是滚动的距离。例如：
我们在屏幕外还有 445 的空间，mLayoutState#scrollingOffset = 445。第一次滚动距离 requiredSpace = 15，
那么 mLayoutState.mAvailable = 15 - 445 = -430。

也就是说此时 mLayoutState.mAvailable < 0，不需要填充新的View。


### 第一次 只 滚动了 15
屏幕底部还有 445

滚动 15

这个时候，layoutState.mAvailable = -430

fill 方法注释1处，重新为 mScrollingOffset 赋值。

```java
if (layoutState.mAvailable < 0) {
    layoutState.mScrollingOffset += layoutState.mAvailable;
}
```
当 layoutState.mAvailable < 0 的时候，layoutState.mScrollingOffset = 445 + (-430) = 15。就是滚动的距离。

scrollBy 方法注释1处，consumed = 445

scrollBy 方法注释2处，此时 absDelta < consumed，所以 scrolled = 15。

这个时候，scrollBy 方法，只是  `mOrientationHelper.offsetChildren(-scrolled);`。
`mOrientationHelper.offsetChildren(-15);` 会把所有的子View向上偏移15像素。

不会填充新的View。


### 第2次 滚动了 638

updateLayoutState 方法，
此时  mLayoutState.mOffset = 2685(2700 - 上次滚动距离 15 = 2685)。
mLayoutState.mScrollingOffset = 430。

计算出来  mLayoutState.mAvailable = 208，也就是说最后一个子View的RecyclerView底部还有208的空间。
这个时候，如果有更多数据的话，就需要填充新的View。


此时 layotuState.mOffset = 2685，也就是说下一个View的top坐标就是2683，bottom = 3585。


scrolled = 638
scrollBy 方法，最后  `mOrientationHelper.offsetChildren(-scrolled);`。


### 第2次 滚动了 1552

updateLayoutState 方法，
此时  mLayoutState.mOffset = 2947
mLayoutState.mScrollingOffset = 692。

计算出来  mLayoutState.mAvailable = 860，也就是说最后一个子View的RecyclerView底部还有860的空间。
这个时候，如果有更多数据的话，就需要填充新的View。


此时 layotuState.mOffset = 2947，也就是说下一个View的top坐标就是 2947，bottom = 3847。


scrolled = 1552
scrollBy 方法，最后  `mOrientationHelper.offsetChildren(-scrolled);`。








scrollBy 方法的注释1处，注意，这里继续调用fill方法填充子View。 这里现在涉及到到了 ViewHolder的回收和复用哟。

注释3处，偏移所有的子View。



### 第一次 只 滚动了 17
屏幕底部还有 445

滚动 17

这个时候，layoutState.mAvailable = -428

这个时候，scrollBy 方法，只是  `mOrientationHelper.offsetChildren(-scrolled);`。

不会填充新的View。也不会回收View。


### 第2次 只 滚动了 874

updateLayoutState 方法，
此时  mLayoutState.mOffset = 2683(2700-17 = 2683)
计算出来  mLayoutState.mAvailable = 446，也就是说最后一个子View的RecyclerView底部还有446的空间。

mLayoutState.mScrollingOffset = 428。

此时 layotuState.mOffset = 2683，也就是说下一个View的top坐标就是2683，bottom = 3583。


scrolled = 874
scrollBy 方法，最后  `mOrientationHelper.offsetChildren(-scrolled);`。




### 第3次 只 滚动了 1999

mLayoutState.mOffset = 2709
mLayoutState.mCurrentPosition = 4
mLayoutState.mScrollingOffset = 454
mLayoutState.mAvailable = requiredSpace - scrollingOffset = 1999 - 454 = 1545

够填充2个数据，如果有的话。

下一个View的top坐标就是2709，bottom 3609。

layoutState.mAvailable = 645。此时没有更多数据了。

最终，只消耗了 1354像素。454 + 900 = 1354。







mLayoutState.mScrollingOffset 在不填充新的View的情况下，我们可以滚动的距离。

第一次滚动了26

mLayoutState.mOffset 2700

mLayoutState.mScrollingOffset 445




第2次滚动了756


mLayoutState.mOffset 2674 2700 - 26 = 2674

mLayoutState.mScrollingOffset 419

填充了  result.mConsumed = 900

mLayoutState.mScrollingOffset = 419 + 900 = 1319

此时 mAvailable = 437


第3次滚动了 3144

mLayoutState.mOffset 2818 2674 + 900 -756 = 2818

mLayoutState.mScrollingOffset = 563









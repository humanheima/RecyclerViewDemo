### 简书无法编辑保存了？什么bug？还得回CSDN来写。

写在前面，看RecyclerView源码有点"老虎吃天，无从下口"的感觉。多次提笔想写关于RecyclerView的源码相关文章，最终也是作罢。最大的原因还是感觉RecyclerView的源码太过复杂，怕自己不能胜任。也是走马观花的看了一些网上的博客文章，有的文章看了也不止一遍。自己也就照虎画猫，来记录一下阅读源码的过程。

* 2024.03.15更新，但是也不是太难下口，哈哈。

## 分析场景：

1. RecyclerView 使用 LinearLayoutManager ，从上到下布局，RecyclerView 的 宽高都是 MATCH_PARENT。
2. 正常设置了LayoutManager和Adapter以后，RecyclerView的measure、layout、draw流程。
4. 第一次是如何填充子View的。
5. 在滚动过程中（move和fling）的时候，是如何回收和填充子View的。我们这里不会看回收和填充子View的细节，只会看哪里发生了回收和填充子View调用操作。

先说下分析的结论：

1. RecyclerView 的 宽高都是 MATCH_PARENT。那么在 onMeasure的时候，是不会调用 dispatchLayoutStep1 和 dispatchLayoutStep2 的。
2. 第一次设置LayoutManager和Adapter是没有动画效果的，可以认为 dispatchLayoutStep1 和 dispatchLayoutStep3  这两个方法都没起作用。
3. dispatchLayoutStep2 方法，真正起布局作用的方法。内部调用 fill 方法，获取ViewHolder(新创建的，或者从缓存中获取的)，测量、布局、添加到 RecyclerView。
4. 第一次布局的时候，RecyclerView 的 childCount 还是0，是没有ViewHolder的回收和复用的。
5. 就是在滑动和fling的时候，LayoutManager会调用 `fill` 方法，会获取ViewHolder填充的RecyclerView，并把滑出RecyclerView的ViewHolder回收。


#### 示例代码

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
    //注释1处，调用requestLayout方法
    requestLayout();
}
```

注释1处，调用requestLayout方法。

RecyclerView的setAdapter方法。

```java
public void setAdapter(@Nullable Adapter adapter) {
    // bail out if layout is frozen
    setLayoutFrozen(false);
    //注释1处
    setAdapterInternal(adapter, false, true);
    processDataSetCompletelyChanged(false);
    //注释2处，调用 requestLayout 方法
    requestLayout();
}
```
注释2处，调用 requestLayout 方法。

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
           //注释2.1处，Note: 注意，当我们在布局里设置 RecyclerView 的宽高为 match_parent 的时候，这里的 widthMode 和 heightMode 都是 MeasureSpec.EXACTLY，会直接return
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

注释2处，是否开启自动测量，我们以LinearLayoutManager来分析，LinearLayoutManager默认是true。


注释2.1处，Note: 注意，当我们在布局里设置 RecyclerView 的宽高为 match_parent 的时候， 这里的 widthMode 和 heightMode 都是 MeasureSpec.EXACTLY，会直接return。

注释3处以及后面的内容先不看，这里留一个问题，为什么会在 onMeasure 的时候，调用 `dispatchLayoutStep1(); 和  dispatchLayoutStep2();` 呢???后面再研究，这里先关注主流程。

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
 * 该方法可以看做是layoutChildren()方法的一个包装，处理由于布局造成的动画改变。
 * 动画的工作机制基于有5中不同类型的动画的假设：
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

第一次调用dispatchLayoutStep1的时候，此时RecyclerView还没有子View所以不会有什么动画执行。方法最后将`mState.mLayoutStep`置为了`State.STEP_LAYOUT`。


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

注释2处，调用LayoutManager的onLayoutChildren方法。我们直接看LinearLayoutManager的onLayoutChildren方法。

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
    //注释1处，如果mLayoutState为null的话，则创建。
    ensureLayoutState();
    mLayoutState.mRecycle = false;
    //决定布局顺序，是否要倒着布局。LinearLayoutManager默认是从上到下布局。
    resolveShouldLayoutReverse();
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
    //注释3处，回收ViewHolder，第一次进来，RecyclerView是没有子View的，没有回收动作。后面再看。 
    detachAndScrapAttachedViews(recycler);
    mLayoutState.mInfinite = resolveIsInfinite();
    mLayoutState.mIsPreLayout = state.isPreLayout();
    // noRecycleSpace not needed: recycling doesn't happen in below's fill
    // invocations because mScrollingOffset is set to SCROLLING_OFFSET_NaN
    mLayoutState.mNoRecycleSpace = 0;
    if (mAnchorInfo.mLayoutFromEnd) {
        //正常情况为该条件不满足。我们分析else的情况。
        //...
    } else {
        //注释4处，向end方向填充的时候，先计算一些信息。
        updateLayoutStateToFillEnd(mAnchorInfo);
        mLayoutState.mExtraFillSpace = extraForEnd;
        //注释5处，从锚点开始向end方向填充
        fill(recycler, mLayoutState, state, false);
       //...
    }
    //...
    //如果必要的话，为预执行动画布局子View。第一次布局的时候，不会有预执行动画。
    layoutForPredictiveAnimations(recycler, state, startOffset, endOffset);
    //...
    
}
```
注释1处，如果 mLayoutState 为null 的话，则创建。布局过程中用来保存布局状态，在布局结束的时候，状态就被重置了。

注释2处，计算锚点位置和坐标。可以简单认为锚点就是RecyclerView的paddingTop，默认为0。

注释3处，回收ViewHolder，第一次进来，RecyclerView是没有子View的，没有回收动作。后面再看。

注释4处，向end方向填充的时候，先计算一些信息。

```java
private void updateLayoutStateToFillEnd(AnchorInfo anchorInfo) {
    //默认情况下，anchorInfo.mPosition 是0，anchorInfo.mCoordinate 是0，
    //在我们这个默认的例子中，anchorInfo.mCoordinate 其实就是 RecyclerView 的 paddingTop，默认是0
    updateLayoutStateToFillEnd(anchorInfo.mPosition, anchorInfo.mCoordinate);
}

private void updateLayoutStateToFillEnd(int itemPosition, int offset) {
    //可填充的像素数，默认就是RecyclerView的高度减去paddingBottom，也就是RecyclerView的可用空间
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
/**
 * 神奇的方法：Fills the given layout, defined by the layoutState. 
 * This is fairly independent from the rest of the {@link LinearLayoutManager} 
 * and with little change, can be made publicly available as a helper class.
 * @param recycler        当前关联到RecyclerView的recycler。
 * @param layoutState     关于我们应该如何填写可用空间的配置。
 * @param state           Context passed by the RecyclerView to control scroll steps.
 * @param stopOnFocusable 如果为true的话，遇到第一个可获取焦点的View则停止填充。
 * @return 返回添加的像素，对滚动方法有用。
 */
int fill(RecyclerView.Recycler recycler, LayoutState layoutState,
        RecyclerView.State state, boolean stopOnFocusable) {
    // max offset we should set is mFastScroll + available
    //记录开始填充的时候，可用的空间
    final int start = layoutState.mAvailable;
    //...
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
        //注释3处，增加偏移量，加上已经填充的像素
        layoutState.mOffset += layoutChunkResult.mConsumed * layoutState.mLayoutDirection;
        if (!layoutChunkResult.mIgnoreConsumed || layoutState.mScrapList != null
                || !state.isPreLayout()) {
            //注释4处，可用空间减去已经填充的像素        
            layoutState.mAvailable -= layoutChunkResult.mConsumed;
            // we keep a separate remaining space because mAvailable is important for recycling
            //注释5处，剩余空间，减去已经填充的像素
            remainingSpace -= layoutChunkResult.mConsumed;
        }

        if (stopOnFocusable && layoutChunkResult.mFocusable) {
            break;
        }
    }
    // 返回已经填充的空间，比如开始可用空间 start 是1920，填充完毕，可用空间 layoutState.mAvailable 是120，就返回 1800 。填充了1800像素。
    //返回结果有可能大于start，因为最后一个填充的View有一部分在屏幕外面。
    return start - layoutState.mAvailable;
}
```
注释0处，记录剩余的可填充的空间。
注释1处，只要还有剩余空间remainingSpace并且还有数据，调用layoutChunk方法，获取ViewHolder 填充RecyclerView。

注释2处，获取并添加子View，然后测量、布局子View并将分割线考虑在内。LinearLayoutManager的layoutChunk方法。

```java
void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state,
        LayoutState layoutState, LayoutChunkResult result) {
    //注释1处，获取子View，可能是从缓存中或者新创建的View。后面分析缓存相关的点的时候再看。
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
    //注释6处，记录该View消耗的高度
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
    //注释7处，布局子View，并将margin和分割线也考虑在内。
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

注释3处，默认是从上到下布局的时候，添加子View。

注释5处，测量子View的大小，包括margin和分割线。

注释6处，记录该View消耗的高度。

注释7处，布局子View，并将margin和分割线也考虑在内。

我们回到 fill 方法的注释3处，增加偏移量，加上已经填充的像素。

注释4处，可用空间减去已经填充的像素。

注释5处，剩余空间，减去已经填充的像素。如果没有剩余空间了或者没有更多View了，fill方法结束。


我们回到LayoutManager的onLayoutChildren方法的注释6处， 向start方向填充的时候，计算一些信息，逻辑和updateLayoutStateToFillEnd类似，不再赘述。

LayoutManager的onLayoutChildren方法的注释7处，调用fill方法继续填充。注意：第一次布局的时候，正常来说我们是从0开始向下填充的，所以就不会有向上填充的情况。我们先忽略。

到这里，dispatchLayoutStep2算是分析完了。接下来是 dispatchLayoutStep3方法。

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

### RecyclerView#draw(Canvas c)

```java
@Override
public void draw(Canvas c) {
    super.draw(c);
    //注释1处，在绘制结束后，调用 onDrawOver 绘制分割线
    final int count = mItemDecorations.size();
    for (int i = 0; i < count; i++) {
        //注释2处
        mItemDecorations.get(i).onDrawOver(c, this, mState);
    }
}

@Override
public void onDraw(Canvas c) {
    super.onDraw(c);
    //先绘制分割线
    final int count = mItemDecorations.size();
    for (int i = 0; i < count; i++) {
        mItemDecorations.get(i).onDraw(c, this, mState);
    }
}
```

注释1处，RecyclerView调用了父类的draw方法。其中会先调用onDraw方法，RecyclerView重写了onDraw方法绘制了分割线。然后就是调用dispatchDraw方法在drawChild方法中绘制子View。然后在注释2处，再次绘制分割线。这也是为什么说我们自定义分割线的时候，只要重写ItemDecoration的onDraw或者onDrawOver一个方法就够了。两个都重写的话会导致绘制两次分割线。

**第一次 measure，layout，draw 过程结束。平平无奇。**

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

scrollBy 方法的注释1处，注意，这里继续调用fill方法填充子View。这里现在涉及到到了 ViewHodler的回收和复用哟。关于回收和复用的细节，下篇文章分析。

注释2处，偏移所有的子View，保证RecyclerView的第一个子View的top坐标就是RecyclerView的top坐标减去RecyclerView的paddingTop。





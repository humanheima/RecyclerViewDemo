
有 header 的时候，是以 header 为锚点的，所以不会滚动到最后。

有 footer 的时候，会话列表滑动到底部的问题。感觉应该是 fill 方法锚点的问题。如果有 header，锚点就是 header，如果有 footer，锚点就是 footer，等待验证。



添加4个数据。

```kotlin
binding.btnNotifyItemChanged.setOnClickListener {

    val newArrayList = arrayListOf<CheckBoxModel>()
    for (i in 0 until 4) {
        newArrayList.add(CheckBoxModel("hi Hello$i", false))
    }
    testAnimatorAdapterAdapter.onDataSourceChanged(newArrayList)
    for (index in 0 until 4) {
        testAnimatorAdapterAdapter.notifyItemInserted(index)
    }
}
```

调用 Adapter#notifyItemInserted 方法以后，会调用 RecyclerView 的 dispatchLayout 方法。 

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


dispatchLayoutStep1 阶段

 没什么特殊的

mAnchorInfo 就是 FooterViewHolder 

FooterViewHolder 加到 mAttachedScrap，再添加的时候，position 就变成4了吗？是的。

```
FooterViewHolder{eb34b96 position=4 id=-1, oldPos=0, pLpos:0 scrap [attachedScrap] tmpDetached no parent}
```

预布局结束，还是只有1个 FooterViewHolder


dispatchLayoutStep2

这个时候 mAnchorInfo 信息

```
AnchorInfo{mPosition=4, mCoordinate=0, mLayoutFromEnd=false, mValid=true}
```


这个时候 mAnchorInfo.mPosition = 4 

detachAndScrapAttachedViews 回收 FooterViewHolder，然后重新布局。

先调用 updateLayoutStateToFillEnd(AnchorInfo anchorInfo) 方法。将 mLayoutState.mCurrentPosition 设置为 4。

然后调用 fill 方法。这时候，锚点是 FooterViewHolder，所以会先布局 FooterViewHolder。

FooterViewHolder ，布局位置是  top = 0，bottom = 144。

然后 FooterViewHolder后面没有数据了。此时 mLayoutState.mCurrentPosition = 5，mLayoutState.mAvailable = 0。Adapter没有没有更多数据了(4条数据加一个footer，position最大是4)。

这个时候，从锚点开始向下填充结束。



然后调用 updateLayoutStateToFillStart(AnchorInfo anchorInfo) 方法。更新一些信息。将
mLayoutState.mLayoutDirection 赋值为 LayoutState.LAYOUT_START(值是-1); 

紧接着调用了一行代码。向上填充的时候，mLayoutState.mItemDirection = -1。
```
 mLayoutState.mCurrentPosition += mLayoutState.mItemDirection;
```

这时候，mLayoutState.mCurrentPosition = 4 - 1 = 3。

然后开始fill 向上填充。

layoutChunk 方法中 ViewHolder3 的 布局 `layoutDecoratedWithMargins(view, left, top, right, bottom);` 位置是在 FootViewHolder 上面 top = -900，bottom = 0。

ViewHolder2 的 布局位置是 top = -1800，bottom = -900。

ViewHolder1 的 布局位置是 top = -2700，bottom = -1800。

布局完 ViewHolder1，以后，`remainingSpace < 0` ，结束向上填充。

这个时候，FooterViewHolder 的位置是 top = 0，bottom = 144。距离 RecyclerView 的底部还有很大的一段距离(在我们的例子中是 2111像素)。然后会走到 fixLayoutEndGap 方法。


```java
private int fixLayoutEndGap(int endOffset, RecyclerView.Recycler recycler,
    RecyclerView.State state, boolean canOffsetChildren) {
    //注释1处，这里大于0，表示end方向有空隙
    int gap = mOrientationHelper.getEndAfterPadding() - endOffset;
    int fixOffset = 0;
    if(gap > 0) {
        //注释2处，向下滚动
        fixOffset = -scrollBy(-gap, recycler, state);
    } else {
        return 0; // nothing to fix
    }
    // move offset according to scroll amount
    endOffset += fixOffset;
    if(canOffsetChildren) {
        // re-calculate gap, see if we could fix it
        gap = mOrientationHelper.getEndAfterPadding() - endOffset;
        if(gap > 0) {
            mOrientationHelper.offsetChildren(gap);
            return gap + fixOffset;
        }
    }
    return fixOffset;
}
```
注释1处，这里大于0，表示end方向有空隙。

注释2处，向下滚动。这个时候，最大滚动距离是 2111 像素。但是我们在上面分析中，ViewHolder1 的 top 是 -2700。


```java
int scrollBy(int delta, RecyclerView.Recycler recycler, RecyclerView.State state) {
    if(getChildCount() == 0 || delta == 0) {
        return 0;
    }
    ensureLayoutState();
    mLayoutState.mRecycle = true;
    final int layoutDirection = delta > 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
    final int absDelta = Math.abs(delta);
    updateLayoutState(layoutDirection, absDelta, true, state);
    final int consumed = mLayoutState.mScrollingOffset + fill(recycler, mLayoutState, state, false);
    if(consumed < 0) {
        if(DEBUG) {
            Log.d(TAG, "Don't have any more elements to scroll");
        }
        return 0;
    }
    final int scrolled = absDelta > consumed ? layoutDirection * consumed : delta;
    //注释1处，偏移子View
    mOrientationHelper.offsetChildren(-scrolled);
    if(DEBUG) {
        Log.d(TAG, "scroll req: " + delta + " scrolled: " + scrolled);
    }
    mLayoutState.mLastScrollDelta = scrolled;
    return scrolled;
}
```

注释1处，偏移所有的子View。也就是说所有的子View向下滚动了2111像素。

FootViewHolder会偏移到 RecyclerView 的底部。 FootViewHolder 的 top = 2111，bottom = 2255。

ViewHolder3 的 top 是 1211 ，bottom 是 2111。

ViewHolder2 的 top 是 311，bottom 是 1211。

dispatchLayoutStep2 结束了。

dispatchLayoutStep3 开始。


FooterViewHolder 会执行 move动画。此时 FootViewHolder 的 top 是 2111。

动画开始前，把 FootViewHolder 的 translationY 设置为 -2111。在动画过程中，变化到 translationY = 0 。 实现了从上滑动到底部的效果。

新增的 ViewHolder 会执行 透明度动画。动画开始前 alpha = 0，动画结束后 alpha = 1。

就结束了。


会先布局 postion 为 4 的 FooterViewHolder，然后向上填充。

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
        //注释6.1处，向start方向填充的时候，mLayoutState.mCurrentPosition减去1。
        mLayoutState.mCurrentPosition += mLayoutState.mItemDirection;
        //注释7处，向start方向填充
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


注释5处，从锚点开始向end方向填充。

注释6处，注释7处，向start方向填充。向start方向填充的View的top坐标都是负的。-900，-1800，-2700。

```java
layoutDecoratedWithMargins(view, left, top, right, bottom);
if (DEBUG) {
    Log.d(TAG, "laid out child at position " + getPosition(view) + ", with l:"
        + (left + params.leftMargin) + ", t:" + (top + params.topMargin) + ", r:"
        + (right - params.rightMargin) + ", b:" + (bottom - params.bottomMargin));
}
```



注释6.1处，向start方向填充的时候，mLayoutState.mCurrentPosition减去1。本来是4，现在是3。

然后会填充 ViewHolder3, ViewHolder2, 








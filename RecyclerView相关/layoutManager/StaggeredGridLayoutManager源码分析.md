# 草稿

StaggeredGridLayoutManager 的 `layoutChunk` 方法是一个重要的方法，它负责对子 View 进行布局。在这个方法中，主要完成了以下几个步骤：

1. 计算当前行或列的剩余空间，以及当前行或列的最大尺寸。
2. 为当前行或列的子 View 分配 Span。
3. 测量子 View 的尺寸。
4. 为子 View 布局。

```java
private void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state, boolean shouldCheckForGaps) {
    final AnchorInfo anchorInfo = mAnchorInfo;
    if(mPendingSavedState != null || mPendingScrollPosition != RecyclerViewNO_POSITION){
        if(state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            anchorInfo.reset();
            return;
        }
    }
    boolean recalculateAnchor = !anchorInfo.mValid || mPendingScrollPosition != RecyclerView.NO_POSITION ||
        mPendingSavedState != null;
    if(recalculateAnchor) {
        //重新计算锚点
        anchorInfo.reset();
        if(mPendingSavedState != null) {
            applyPendingSavedState(anchorInfo);
        } else {
            resolveShouldLayoutReverse();
            anchorInfo.mLayoutFromEnd = mShouldReverseLayout;
        }
        updateAnchorInfoForLayout(state, anchorInfo);
        anchorInfo.mValid = true;
    }
    if(mPendingSavedState == null && mPendingScrollPosition == RecyclerView.NO_POSITION) {
        if(anchorInfo.mLayoutFromEnd != mLastLayoutFromEnd || isLayoutRTL() != mLastLayoutRTL) {
            mLazySpanLookup.clear();
            anchorInfo.mInvalidateOffsets = true;
        }
    }
    if(getChildCount() > 0 && (mPendingSavedState == null || mPendingSavedState.mSpanOffsetsSize < 1)) {
        if(anchorInfo.mInvalidateOffsets) {
            for(int i = 0; i < mSpanCount; i++) {
                // Scroll to position is set, clear.
                mSpans[i].clear();
                if(anchorInfo.mOffset != INVALID_OFFSET) {
                    mSpans[i].setLine(anchorInfo.mOffset);
                }
            }
        } else {
            if(recalculateAnchor || mAnchorInfo.mSpanReferenceLines == null) {
                for(int i = 0; i < mSpanCount; i++) {
                    mSpans[i].cacheReferenceLineAndClear(mShouldReverseLayout, anchorInfo.mOffset);
                }
                mAnchorInfo.saveSpanReferenceLines(mSpans);
            } else {
                for(int i = 0; i < mSpanCount; i++) {
                    final Span span = mSpans[i];
                    span.clear();
                    span.setLine(mAnchorInfo.mSpanReferenceLines[i]);
                }
            }
        }
    }
    detachAndScrapAttachedViews(recycler);
    mLayoutState.mRecycle = false;
    mLaidOutInvalidFullSpan = false;
    //mSecondaryOrientation，计算非主轴方向的测量参数
    updateMeasureSpecs(mSecondaryOrientation.getTotalSpace());
    //计算mLayoutState 的各个参数，mStartLine:开始布局的位置 , mEndLine:结束布局的位置
    updateLayoutState(anchorInfo.mPosition, state);
    if(anchorInfo.mLayoutFromEnd) {
        // Layout start.
        setLayoutStateDirection(LayoutState.LAYOUT_START);
        fill(recycler, mLayoutState, state);
        // Layout end.
        setLayoutStateDirection(LayoutState.LAYOUT_END);
        mLayoutState.mCurrentPosition = anchorInfo.mPosition + mLayoutState.mItemDirection;
        fill(recycler, mLayoutState, state);
    } else {
        // Layout end.
        setLayoutStateDirection(LayoutState.LAYOUT_END);
        //向end方向填充
        fill(recycler, mLayoutState, state);
        // Layout start.
        setLayoutStateDirection(LayoutState.LAYOUT_START);
        mLayoutState.mCurrentPosition = anchorInfo.mPosition + mLayoutState.mItemDirection;
        //向start方向填充
        fill(recycler, mLayoutState, state);
    }
    repositionToWrapContentIfNecessary();
    if(getChildCount() > 0) {
        if(mShouldReverseLayout) {
            fixEndGap(recycler, state, true);
            fixStartGap(recycler, state, false);
        } else {
            fixStartGap(recycler, state, true);
            fixEndGap(recycler, state, false);
        }
    }
    boolean hasGaps = false;
    if(shouldCheckForGaps && !state.isPreLayout()) {
        final boolean needToCheckForGaps = mGapStrategy != GAP_HANDLING_NONE && getChildCount() > 0 && (
            mLaidOutInvalidFullSpan || hasGapsToFix() != null);
        if(needToCheckForGaps) {
            removeCallbacks(mCheckForGapsRunnable);
            if(checkForGaps()) {
                hasGaps = true;
            }
        }
    }
    if(state.isPreLayout()) {
        mAnchorInfo.reset();
    }
    mLastLayoutFromEnd = anchorInfo.mLayoutFromEnd;
    mLastLayoutRTL = isLayoutRTL();
    if(hasGaps) {
        mAnchorInfo.reset();
        onLayoutChildren(recycler, state, false);
    }
}
```





```java
private int fill(RecyclerView.Recycler recycler, LayoutState layoutState, RecyclerView.State state) {
    mRemainingSpans.set(0, mSpanCount, true);
    // The target position we are trying to reach.
    final int targetLine;
    // Line of the furthest row.
    if(mLayoutState.mInfinite) {
        if(layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
            targetLine = Integer.MAX_VALUE;
        } else { // LAYOUT_START
            targetLine = Integer.MIN_VALUE;
        }
    } else {
        if(layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
            //This is the target pixel closest to the end of the layout that we are trying to fill
            //这是我们试图填充的最接近布局末尾的目标像素
            targetLine = layoutState.mEndLine + layoutState.mAvailable;
        } else { // LAYOUT_START
            targetLine = layoutState.mStartLine - layoutState.mAvailable;
        }
    }
    updateAllRemainingSpans(layoutState.mLayoutDirection, targetLine);
    if(DEBUG) {
        Log.d(TAG, "FILLING targetLine: " + targetLine + "," + "remaining spans:" + mRemainingSpans + ", state: " +
            layoutState);
    }
    // the default coordinate to add new view.
    // 开始添加的行的默认坐标
    final int defaultNewViewLine = mShouldReverseLayout ? mPrimaryOrientation.getEndAfterPadding() :
        mPrimaryOrientation.getStartAfterPadding();
    boolean added = false;
    while(layoutState.hasMore(state) 
            && (mLayoutState.mInfinite || !mRemainingSpans.isEmpty())) {
        View view = layoutState.next(recycler);
        LayoutParams lp = ((LayoutParams) view.getLayoutParams());
        final int position = lp.getViewLayoutPosition();
        final int spanIndex = mLazySpanLookup.getSpan(position);
        Span currentSpan;
        final boolean assignSpan = spanIndex == LayoutParams.INVALID_SPAN_ID;
        if(assignSpan) {
            //SpanLookup没有指定span
            currentSpan = lp.mFullSpan ? mSpans[0] : getNextSpan(layoutState);
            mLazySpanLookup.setSpan(position, currentSpan);
            if(DEBUG) {
                Log.d(TAG, "assigned " + currentSpan.mIndex + " for " + position);
            }
        } else {
            if(DEBUG) {
                Log.d(TAG, "using " + spanIndex + " for pos " + position);
            }
            currentSpan = mSpans[spanIndex];
        }
        // assign span before measuring so that item decorators can get updated span index
        //注意，这里找到了span
        lp.mSpan = currentSpan;

        if(layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
            addView(view);
        } else {
            addView(view, 0);
        }
        measureChildWithDecorationsAndMargin(view, lp, false);
        final int start;
        final int end;
        if(layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
            //
            start = lp.mFullSpan ? getMaxEnd(defaultNewViewLine) 
                    : currentSpan.getEndLine(defaultNewViewLine);
            end = start + mPrimaryOrientation.getDecoratedMeasurement(view);
            if(assignSpan && lp.mFullSpan) {
                LazySpanLookup.FullSpanItem fullSpanItem;
                fullSpanItem = createFullSpanItemFromEnd(start);
                fullSpanItem.mGapDir = LayoutState.LAYOUT_START;
                fullSpanItem.mPosition = position;
                mLazySpanLookup.addFullSpanItem(fullSpanItem);
            }
        } else {
            end = lp.mFullSpan ? getMinStart(defaultNewViewLine) : currentSpan.getStartLine(defaultNewViewLine);
            start = end - mPrimaryOrientation.getDecoratedMeasurement(view);
            if(assignSpan && lp.mFullSpan) {
                LazySpanLookup.FullSpanItem fullSpanItem;
                fullSpanItem = createFullSpanItemFromStart(end);
                fullSpanItem.mGapDir = LayoutState.LAYOUT_END;
                fullSpanItem.mPosition = position;
                mLazySpanLookup.addFullSpanItem(fullSpanItem);
            }
        }
        // check if this item may create gaps in the future
        if(lp.mFullSpan && layoutState.mItemDirection == LayoutState.ITEM_DIRECTION_HEAD) {
            if(assignSpan) {
                mLaidOutInvalidFullSpan = true;
            } else {
                final boolean hasInvalidGap;
                if(layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
                    hasInvalidGap = !areAllEndsEqual();
                } else { // layoutState.mLayoutDirection == LAYOUT_START
                    hasInvalidGap = !areAllStartsEqual();
                }
                if(hasInvalidGap) {
                    final LazySpanLookup.FullSpanItem fullSpanItem = mLazySpanLookup.getFullSpanItem(position);
                    if(fullSpanItem != null) {
                        fullSpanItem.mHasUnwantedGapAfter = true;
                    }
                    mLaidOutInvalidFullSpan = true;
                }
            }
        }
        attachViewToSpans(view, lp, layoutState);
        final int otherStart;
        final int otherEnd;
        if(isLayoutRTL() && mOrientation == VERTICAL) {
            otherEnd = lp.mFullSpan ? mSecondaryOrientation.getEndAfterPadding() : mSecondaryOrientation.getEndAfterPadding() -
                (mSpanCount - 1 - currentSpan.mIndex) * mSizePerSpan;
            otherStart = otherEnd - mSecondaryOrientation.getDecoratedMeasurement(view);
        } else {
            //对于从上到下布局方向，otherStart 就是left，otherEnd 就是right
            otherStart = lp.mFullSpan ? mSecondaryOrientation.getStartAfterPadding() : currentSpan.mIndex *
                mSizePerSpan + mSecondaryOrientation.getStartAfterPadding();
            otherEnd = otherStart + mSecondaryOrientation.getDecoratedMeasurement(view);
        }
        if(mOrientation == VERTICAL) {
            //摆放子view，对于从上到下布局方向，otherStart 就是left，otherEnd 就是right
            layoutDecoratedWithMargins(view, otherStart, start, otherEnd, end);
        } else {
            layoutDecoratedWithMargins(view, start, otherStart, end, otherEnd);
        }
        if(lp.mFullSpan) {
            updateAllRemainingSpans(mLayoutState.mLayoutDirection, targetLine);
        } else {
            //更新剩余空间
            updateRemainingSpans(currentSpan, mLayoutState.mLayoutDirection, targetLine);
        }
        recycle(recycler, mLayoutState);
        if(mLayoutState.mStopInFocusable && view.hasFocusable()) {
            if(lp.mFullSpan) {
                mRemainingSpans.clear();
            } else {
                mRemainingSpans.set(currentSpan.mIndex, false);
            }
        }
        added = true;
    }
    if(!added) {
        recycle(recycler, mLayoutState);
    }
    final int diff;
    if(mLayoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
        final int minStart = getMinStart(mPrimaryOrientation.getStartAfterPadding());
        diff = mPrimaryOrientation.getStartAfterPadding() - minStart;
    } else {
        final int maxEnd = getMaxEnd(mPrimaryOrientation.getEndAfterPadding());
        diff = maxEnd - mPrimaryOrientation.getEndAfterPadding();
    }
    return diff > 0 ? Math.min(layoutState.mAvailable, diff) : 0;
}
```

想达到的效果
两列，按照奇偶排序。而不是找最小的bottom坐标。
就是这个方法，
```java
/**
 * Finds the span for the next view.
 */
private Span getNextSpan(LayoutState layoutState) {
    final boolean preferLastSpan = preferLastSpan(layoutState.mLayoutDirection);
    final int startIndex, endIndex, diff;
    if(preferLastSpan) {
        startIndex = mSpanCount - 1;
        endIndex = -1;
        diff = -1;
    } else {
        startIndex = 0;
        endIndex = mSpanCount;
        diff = 1;
    }
    if(layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
        Span min = null;
        int minLine = Integer.MAX_VALUE;
        final int defaultLine = mPrimaryOrientation.getStartAfterPadding();
        for(int i = startIndex; i != endIndex; i += diff) {
            final Span other = mSpans[i];
            int otherLine = other.getEndLine(defaultLine);
            if(otherLine < minLine) {
                min = other;
                minLine = otherLine;
            }
        }
        return min;
    } else {
        Span max = null;
        int maxLine = Integer.MIN_VALUE;
        final int defaultLine = mPrimaryOrientation.getEndAfterPadding();
        for(int i = startIndex; i != endIndex; i += diff) {
            final Span other = mSpans[i];
            int otherLine = other.getStartLine(defaultLine);
            if(otherLine > maxLine) {
                max = other;
                maxLine = otherLine;
            }
        }
        return max;
    }
}
```


17217



17225

第一次进来，两个 span getEndLine 都是0，最小的就选 mSpans[0]

currentSpan = mSpans[0]

lp.mSpan = currentSpan;


这里更新了 mSpans 
```java
private void attachViewToSpans(View view, LayoutParams lp, LayoutState layoutState) {
    if(layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
        if(lp.mFullSpan) {
            appendViewToAllSpans(view);
        } else {
            //调用 Span 的 appendToSpan 方法。
            lp.mSpan.appendToSpan(view);
        }
    } else {
        if(lp.mFullSpan) {
            prependViewToAllSpans(view);
        } else {
            lp.mSpan.prependToSpan(view);
        }
    }
}
```

```java
void appendToSpan(View view) {
    LayoutParams lp = getLayoutParams(view);
    lp.mSpan = this;
    //这里存储了宽高。
    mViews.add(view);
    mCachedEnd = INVALID_LINE;
    if(mViews.size() == 1) {
        mCachedStart = INVALID_LINE;
    }
    if(lp.isItemRemoved() || lp.isItemChanged()) {
        mDeletedSize += mPrimaryOrientation.getDecoratedMeasurement(view);
    }
}
```

StaggeredGridLayoutManager 的 updateRemainingSpans 方法。

```java
private void updateRemainingSpans(Span span, int layoutDir, int targetLine) {
    final int deletedSize = span.getDeletedSize();
    if(layoutDir == LayoutState.LAYOUT_START) {
        final int line = span.getStartLine();
        if(line + deletedSize <= targetLine) {
            mRemainingSpans.set(span.mIndex, false);
        }
    } else {
        //注释1处，调用 Span 的 getEndLine 方法
        final int line = span.getEndLine();
        if(line - deletedSize >= targetLine) {
            mRemainingSpans.set(span.mIndex, false);
        }
    }
}
```

注释1处，调用 Span 的 getEndLine 方法。

```java
int getEndLine() {
    if(mCachedEnd != INVALID_LINE) {
        return mCachedEnd;
    }
    //注释1处，这里计算了
    calculateCachedEnd();
    return mCachedEnd;
}
```

Span 的 calculateCachedEnd 方法。

```java
void calculateCachedEnd() {
    final View endView = mViews.get(mViews.size() - 1);
    final LayoutParams lp = getLayoutParams(endView);
    //mCachedEnd 
    mCachedEnd = mPrimaryOrientation.getDecoratedEnd(endView);
    if(lp.mFullSpan) {
        LazySpanLookup.FullSpanItem fsi = mLazySpanLookup.getFullSpanItem(lp.getViewLayoutPosition());
        if(fsi != null && fsi.mGapDir == LayoutState.LAYOUT_END) {
            mCachedEnd += fsi.getGapForSpan(mIndex);
        }
    }
}
```










### 改写方法。

/**
 * Finds the span for the next view.
 */
private Span getNextSpan(LayoutState layoutState) {
    final boolean preferLastSpan = preferLastSpan(layoutState.mLayoutDirection);
    final int startIndex, endIndex, diff;
    if(preferLastSpan) {
        startIndex = mSpanCount - 1;
        endIndex = -1;
        diff = -1;
    } else {
        startIndex = 0;
        endIndex = mSpanCount;
        diff = 1;
    }
    if(layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
        Span min = null;
        int minLine = Integer.MAX_VALUE;
        final int defaultLine = mPrimaryOrientation.getStartAfterPadding();
        for(int i = startIndex; i != endIndex; i += diff) {
            final Span other = mSpans[i];
            int otherLine = other.getEndLine(defaultLine);
            if(otherLine < minLine) {
                min = other;
                minLine = otherLine;
            }
        }
        return min;
    } else {
        Span max = null;
        int maxLine = Integer.MIN_VALUE;
        final int defaultLine = mPrimaryOrientation.getEndAfterPadding();
        for(int i = startIndex; i != endIndex; i += diff) {
            final Span other = mSpans[i];
            int otherLine = other.getStartLine(defaultLine);
            if(otherLine > maxLine) {
                max = other;
                maxLine = otherLine;
            }
        }
        return max;
    }
}
```